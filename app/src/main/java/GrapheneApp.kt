package com.graphene.antivirus

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.*
import com.graphene.antivirus.data.ApiClient
import com.graphene.antivirus.worker.SyncWorker
import com.graphene.antivirus.worker.TelemetryWorker
import java.util.concurrent.TimeUnit

class GrapheneApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
        createChannels()
        scheduleTelemetry()
        scheduleSync()
    }

    private fun createChannels() {
        val nm = getSystemService(NotificationManager::class.java)

        // ID каналов НЕ меняем — иначе пользователи потеряют настройки уведомлений.
        // Меняем только отображаемые имена.
        nm.createNotificationChannel(
            NotificationChannel(
                "graphene_core",
                getString(R.string.notif_channel_core_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_core_desc)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                "graphene_vpn",
                getString(R.string.notif_channel_vpn_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_vpn_desc)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                "graphene_sync",
                getString(R.string.notif_channel_sync_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_sync_desc)
            }
        )
    }

    private fun scheduleTelemetry() {
        val req = PeriodicWorkRequestBuilder<TelemetryWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "graphene_telemetry", ExistingPeriodicWorkPolicy.KEEP, req
        )
    }

    private fun scheduleSync() {
        val req = PeriodicWorkRequestBuilder<SyncWorker>(30, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "graphene_sync", ExistingPeriodicWorkPolicy.KEEP, req
        )
    }
}