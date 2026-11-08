package com.graphene.antivirus

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.*
import com.graphene.antivirus.worker.SyncWorker
import com.graphene.antivirus.worker.TelemetryWorker
import java.util.concurrent.TimeUnit

class GrapheneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createChannels()
        scheduleTelemetry()
        scheduleSync()
    }

    private fun createChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("graphene_core", "Ядро Графен", NotificationManager.IMPORTANCE_LOW))
        nm.createNotificationChannel(NotificationChannel("graphene_vpn", "Сетевой монитор", NotificationManager.IMPORTANCE_LOW))
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