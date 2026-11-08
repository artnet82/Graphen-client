package com.graphene.antivirus.scanner

import android.content.Context
import android.os.Environment
import com.graphene.antivirus.data.FileTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class FilesCollector(private val context: Context) {

    private val suspiciousExtensions = setOf(
        "apk", "dex", "so", "zip", "rar", "7z", "tar", "gz", "exe", "scr", "bat", "cmd"
    )

    suspend fun collect(limit: Int = 300): List<FileTelemetry> = withContext(Dispatchers.IO) {
        val results = mutableListOf<FileTelemetry>()
        val root = Environment.getExternalStorageDirectory()
        if (root.exists() && root.canRead()) {
            walkForFiles(root, results, limit)
        }
        results
    }

    private fun walkForFiles(dir: File, out: MutableList<FileTelemetry>, limit: Int) {
        if (out.size >= limit) return
        val files = try { dir.listFiles() } catch (_: Exception) { return } ?: return
        for (f in files) {
            if (out.size >= limit) return
            if (f.isDirectory) {
                if (f.name !in setOf("Android", "LOST.DIR", ".thumbnails")) {
                    walkForFiles(f, out, limit)
                }
            } else {
                val ext = f.extension.lowercase()
                // Отправляем только потенциально исполняемые/архивные или APK
                if (ext in suspiciousExtensions && f.length() in 1..(100L * 1024 * 1024)) {
                    out += FileTelemetry(
                        path = f.absolutePath,
                        size = f.length(),
                        extension = ext,
                        sha256 = sha256(f),
                        modified = f.lastModified()
                    )
                }
            }
        }
    }

    private fun sha256(file: File): String? = try {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { s ->
            val buf = ByteArray(16 * 1024)
            var n: Int
            while (s.read(buf).also { n = it } > 0) digest.update(buf, 0, n)
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    } catch (_: Exception) { null }
}