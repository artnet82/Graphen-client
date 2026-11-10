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
import com.graphene.antivirus.data.SyncFolderStore
import com.graphene.antivirus.data.SyncRepository

class SyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val consent = ConsentStore(applicationContext)
        if (!consent.syncEnabled()) return Result.success()

        val registration = RegistrationManager(applicationContext)
        registration.ensureRegistered() ?: return Result.retry()

        try {
            setForeground(createForegroundInfo(applicationContext.getString(R.string.notif_text_sync)))

            val deviceId = consent.getOrCreateDeviceId()
            val folderStore = SyncFolderStore(applicationContext)
            val repo = SyncRepository(applicationContext, ApiClient.create(), folderStore)
            val result = repo.syncAll(deviceId)

            return if (result.error != null) Result.retry() else Result.success()
        } catch (_: Exception) {
            return Result.retry()
        }
    }

    private fun createForegroundInfo(text: String): ForegroundInfo {
        val ctx = applicationContext
        val nm = ctx.getSystemService(NotificationManager::class.java)

        nm?.createNotificationChannel(
            android.app.NotificationChannel(
                "graphene_sync",
                ctx.getString(R.string.notif_channel_sync_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = ctx.getString(R.string.notif_channel_sync_desc)
            }
        )

        val notif = NotificationCompat.Builder(ctx, "graphene_sync")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle(ctx.getString(R.string.notif_title))
            .setContentText(text)
            .setOngoing(true)
            .build()

        return ForegroundInfo(1002, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }
}