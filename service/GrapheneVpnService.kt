package com.graphene.antivirus.service

import android.app.*
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.graphene.antivirus.MainActivity
import com.graphene.antivirus.R
import com.graphene.antivirus.scanner.NetworkEventStore
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class GrapheneVpnService : VpnService() {

    private var tunFd: ParcelFileDescriptor? = null
    private var worker: Thread? = null
    @Volatile private var running = false

    companion object {
    const val ACTION_START = "com.graphene.antivirus.VPN_START"
    const val ACTION_STOP  = "com.graphene.antivirus.VPN_STOP"

    // Уникальные ID, не пересекаются с TelemetryWorker (1001) и SyncWorker (1002)
    private const val NOTIF_ID = 1003

    // 198.18.0.0/15 — IANA-reserved для benchmarking (RFC 2544).
    // Этот диапазон гарантированно не используется в реальных LAN.
    // По тому же принципу работают Clash, Sing-box, WireGuard.
    private const val VPN_ADDR = "198.18.0.1"     // адрес tun-интерфейса
    private const val FAKE_DNS = "198.18.0.2"     // DNS, на который Android отправит все запросы

    // Fallback DNS, если не удалось получить системный.
    // Два разных провайдера — на случай если один заблокирован.
    private const val FALLBACK_DNS_1 = "1.1.1.1"
    private const val FALLBACK_DNS_2 = "8.8.8.8"

    // Таймауты и лимиты
    private const val DNS_UPSTREAM_TIMEOUT_MS = 3_000
    private const val DNS_MAX_QUERY_BYTES      = 512
}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopVpn()
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIF_ID, buildNotification())
        if (!running) startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        val builder = Builder()
            .setSession("Графен")
            .setMtu(1500)
            .addAddress(VPN_ADDR, 32)
            .addDnsServer(FAKE_DNS)
            .addRoute(FAKE_DNS, 32)
            .setBlocking(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        tunFd = try { builder.establish() } catch (_: Exception) { null }
        if (tunFd == null) return

        running = true
        worker = thread(name = "graphene-vpn", isDaemon = true) { pumpLoop() }
    }

    /** Читает сырые пакеты из TUN. Ловит UDP:53 → DNS. */
    private fun pumpLoop() {
        val fd = tunFd ?: return
        val input = FileInputStream(fd.fileDescriptor)
        val output = FileOutputStream(fd.fileDescriptor)
        val buffer = ByteArray(32767)

        try {
            while (running) {
                val length = input.read(buffer)
                if (length <= 0) continue

                // Только IPv4
                val version = (buffer[0].toInt() shr 4) and 0x0F
                if (version != 4) continue

                val ihl = (buffer[0].toInt() and 0x0F) * 4
                if (length < ihl + 8) continue

                val protocol = buffer[9].toInt() and 0xFF
                if (protocol != 17) continue // только UDP

                // UDP header
                val srcPort = ((buffer[ihl].toInt() and 0xFF) shl 8) or (buffer[ihl + 1].toInt() and 0xFF)
                val dstPort = ((buffer[ihl + 2].toInt() and 0xFF) shl 8) or (buffer[ihl + 3].toInt() and 0xFF)
                if (dstPort != 53) continue

                val dnsStart = ihl + 8
                val dnsPayload = buffer.copyOfRange(dnsStart, length)
                val domain = parseDnsQuestion(dnsPayload)
                if (domain != null) NetworkEventStore.add(domain)

                // Пересылаем на реальный DNS
                try {
                    DatagramSocket().use { socket ->
                        protect(socket)
                        socket.soTimeout = 3000
                        val dnsServer = InetAddress.getByName(REAL_DNS)
                        socket.send(DatagramPacket(dnsPayload, dnsPayload.size, dnsServer, 53))

                        val respBuf = ByteArray(4096)
                        val respPacket = DatagramPacket(respBuf, respBuf.size)
                        socket.receive(respPacket)

                        val response = buildResponsePacket(buffer, length, ihl, srcPort, respPacket.data, respPacket.length)
                        output.write(response)
                    }
                } catch (_: Exception) { /* пропускаем ошибочные запросы */ }
            }
        } catch (_: Exception) { /* TUN закрыт */ }
    }

    /** Достаёт доменное имя из DNS-запроса. */
    private fun parseDnsQuestion(data: ByteArray): String? {
        if (data.size < 12) return null
        val qdCount = ((data[4].toInt() and 0xFF) shl 8) or (data[5].toInt() and 0xFF)
        if (qdCount == 0) return null
        val sb = StringBuilder()
        var pos = 12
        var safety = 0
        while (pos < data.size && safety < 255) {
            val len = data[pos].toInt() and 0xFF
            if (len == 0) break
            if ((len and 0xC0) == 0xC0) break // compression
            pos++
            if (pos + len > data.size) break
            if (sb.isNotEmpty()) sb.append('.')
            sb.append(String(data, pos, len, Charsets.US_ASCII))
            pos += len
            safety += len
        }
        return sb.toString().takeIf { it.isNotBlank() }
    }

    /** Собирает IP+UDP пакет с DNS-ответом обратно в TUN. */
    private fun buildResponsePacket(
        original: ByteArray, originalLen: Int, ihl: Int,
        originalSrcPort: Int, dnsResponse: ByteArray, dnsLen: Int
    ): ByteArray {
        val srcIp = byteArrayOf(10, 111, 0, 2)     // наш фейковый DNS
        val dstIp = original.copyOfRange(16, 20)   // клиент был source'ом

        val udpLen = 8 + dnsLen
        val totalLen = 20 + udpLen

        val buf = ByteBuffer.allocate(totalLen)

        // IPv4 header
        buf.put(0x45)
        buf.put(0)
        buf.putShort(totalLen.toShort())
        buf.putShort(0)                    // id
        buf.putShort(0x4000.toShort())     // DF
        buf.put(0x40)                      // TTL
        buf.put(17)                        // UDP
        buf.putShort(0)                    // checksum placeholder
        buf.put(srcIp)
        buf.put(dstIp)
        // IP checksum
        var sum = 0
        for (i in 0 until 20 step 2) {
            sum += ((buf.get(i).toInt() and 0xFF) shl 8) or (buf.get(i + 1).toInt() and 0xFF)
        }
        while (sum shr 16 != 0) sum = (sum and 0xFFFF) + (sum shr 16)
        buf.putShort(10, sum.inv().toShort())

        // UDP header
        buf.putShort(53)                          // src port
        buf.putShort(originalSrcPort.toShort())   // dst port
        buf.putShort(udpLen.toShort())
        buf.putShort(0)                           // checksum 0 — допустимо для IPv4

        buf.put(dnsResponse, 0, dnsLen)
        return buf.array()
    }

    private fun buildNotification(): Notification {
        val ch = "graphene_vpn"
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(ch, "Сетевой монитор", NotificationManager.IMPORTANCE_LOW)
        )
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, ch)
            .setSmallIcon(R.drawable.ic_launcher_shield)
            .setContentTitle("Сетевой монитор активен")
            .setContentText("Анализ DNS-трафика в реальном времени")
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun stopVpn() {
        running = false
        try { tunFd?.close() } catch (_: Exception) { }
        tunFd = null
        worker?.interrupt()
        worker = null
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopVpn()
        stopSelf()
        super.onRevoke()
    }
}