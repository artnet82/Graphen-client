package com.graphene.antivirus.worker

import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.graphene.antivirus.R
import com.graphene.antivirus.data.ApiClient
import com.graphene.antivirus.data.ConsentStore
import com.graphene.antivirus.data.RegistrationManager
import com.graphene.antivirus.scanner.TelemetryCollector

class TelemetryWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val registration = RegistrationManager(applicationContext)
        registration.ensureRegistered() ?: return Result.retry()

        try {
            setForeground(createForegroundInfo(applicationContext.getString(R.string.notif_text_monitoring)))

            val deviceId = ConsentStore(applicationContext).getOrCreateDeviceId()
            val payload = TelemetryCollector(applicationContext).collect(deviceId)
            val resp = ApiClient.create().uploadTelemetry(payload)

            return when {
                resp.isSuccessful -> Result.success()

                resp.code() == 401 -> {
                    registration.forceReRegister()
                    Result.retry()
                }

                resp.code() in 500..599 -> Result.retry()
                else -> Result.failure()
            }
        } catch (_: Exception) {
            return Result.retry()
        }
    }

    private fun createForegroundInfo(text: String): ForegroundInfo {
        val ctx = applicationContext
        val nm = ctx.getSystemService(NotificationManager::class.java)

        // Канал уже создан в GrapheneApp — здесь только на случай холодного старта воркера.
        nm?.createNotificationChannel(
            android.app.NotificationChannel(
                "graphene_core",
                ctx.getString(R.string.notif_channel_core_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = ctx.getString(R.string.notif_channel_core_desc)
            }
        )

        val notif = NotificationCompat.Builder(ctx, "graphene_core")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(ctx.getString(R.string.notif_title))
            .setContentText(text)
            .setOngoing(true)
            .build()

        return ForegroundInfo(1001, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }
}