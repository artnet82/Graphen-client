package com.graphene.antivirus.scanner

import android.os.Build
import com.graphene.antivirus.data.DeviceInfo
import java.io.File

class DeviceInfoCollector {
    fun collect(deviceId: String): DeviceInfo = DeviceInfo(
        deviceId = deviceId,
        manufacturer = Build.MANUFACTURER,
        model = Build.MODEL,
        androidVersion = Build.VERSION.RELEASE,
        sdkInt = Build.VERSION.SDK_INT,
        securityPatch = if (Build.VERSION.SDK_INT >= 23) Build.VERSION.SECURITY_PATCH else "unknown",
        rooted = isRooted()
    )

    private fun isRooted(): Boolean = listOf(
        "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
        "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
        "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su"
    ).any { File(it).exists() }
}