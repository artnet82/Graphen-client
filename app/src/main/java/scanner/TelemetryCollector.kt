package com.graphene.antivirus.scanner

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.graphene.antivirus.data.AppTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class TelemetryCollector(private val context: Context) {
    suspend fun collectAll(): List<AppTelemetry> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val flags = PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA
        val packages = pm.getInstalledApplications(flags)
        val result = ArrayList<AppTelemetry>(packages.size)

        for (info in packages) {
            if ((info.flags and ApplicationInfo.FLAG_SYSTEM) != 0) continue
            try {
                val pkgInfo = pm.getPackageInfo(info.packageName, PackageManager.GET_PERMISSIONS)
                val perms = pkgInfo.requestedPermissions?.toList() ?: emptyList()
                result.add(AppTelemetry(
                    packageName = info.packageName,
                    appName = pm.getApplicationLabel(info).toString(),
                    versionName = pkgInfo.versionName ?: "—",
                    targetSdk = info.targetSdkVersion,
                    permissions = perms,
                    firstInstall = pkgInfo.firstInstallTime,
                    lastUpdate = pkgInfo.lastUpdateTime,
                    apkSha256 = info.sourceDir?.let { sha256(it) },
                    isSideloaded = isSideloaded(info.packageName)
                ))
            } catch (_: PackageManager.NameNotFoundException) { }
        }
        result
    }

    private fun isSideloaded(pkg: String): Boolean = try {
        val installer = context.packageManager.getInstallSourceInfo(pkg).installingPackageName
        installer == null || installer !in setOf("com.android.vending", "com.google.android.packageinstaller")
    } catch (_: Exception) { true }

    private fun sha256(path: String): String? = try {
        val digest = MessageDigest.getInstance("SHA-256")
        File(path).inputStream().use { s ->
            val buf = ByteArray(16 * 1024)
            var n: Int
            while (s.read(buf).also { n = it } > 0) digest.update(buf, 0, n)
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    } catch (_: Exception) { null }
}