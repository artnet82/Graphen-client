package com.graphene.antivirus.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.graphene.antivirus.MainActivity
import com.graphene.antivirus.R

class GrapheneService : Service() {
    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotif())
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private fun buildNotif(): Notification {
        val ch = "graphene_core"
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(ch, "Ядро Графен", NotificationManager.IMPORTANCE_LOW)
        )
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, ch)
            .setSmallIcon(R.drawable.ic_launcher_shield)
            .setContentTitle("Графен активен")
            .setContentText("Непрерывный мониторинг безопасности")
            .setContentIntent(pi)
            .setOngoing(true).setSilent(true)
            .build()
    }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object { const val NOTIF_ID = 1001 }
}