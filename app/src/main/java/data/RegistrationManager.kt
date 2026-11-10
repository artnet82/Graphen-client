package com.graphene.antivirus.data

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Гарантирует регистрацию устройства и наличие api_token в DataStore.
 * Идемпотентно: если токен уже есть — возвращает его без сети.
 * forceReRegister() используется при получении 401.
 */
class RegistrationManager(private val context: Context) {

    private val consent = ConsentStore(context)

    suspend fun ensureRegistered(): String? = withContext(Dispatchers.IO) {
        consent.getApiToken()?.let { return@withContext it }

        val deviceId = consent.getOrCreateDeviceId()
        val request = RegisterRequest(
            device = DeviceInfo(
                deviceId       = deviceId,
                manufacturer   = Build.MANUFACTURER ?: "unknown",
                model          = Build.MODEL ?: "unknown",
                androidVersion = Build.VERSION.RELEASE ?: "unknown",
                sdkInt         = Build.VERSION.SDK_INT,
                securityPatch  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                     (Build.VERSION.SECURITY_PATCH ?: "unknown")
                                 else "unknown",
                rooted         = isProbablyRooted(),
            )
        )

        val resp = try {
            ApiClient.createUnauthenticated().register(request)
        } catch (_: Exception) {
            return@withContext null
        }

        if (resp.isSuccessful) {
            val token = resp.body()?.apiToken
            if (!token.isNullOrBlank()) {
                consent.saveApiToken(token)
                return@withContext token
            }
        }
        null
    }

    suspend fun forceReRegister(): String? = withContext(Dispatchers.IO) {
        consent.clearApiToken()
        ensureRegistered()
    }

    private fun isProbablyRooted(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
            "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
            "/data/local/su", "/su/bin/su"
        )
        return paths.any { java.io.File(it).exists() }
    }
}