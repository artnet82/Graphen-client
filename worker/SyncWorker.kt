package com.graphene.antivirus.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.graphene.antivirus.data.ApiClient
import com.graphene.antivirus.data.ConsentStore
import com.graphene.antivirus.data.SyncFolderStore
import com.graphene.antivirus.data.SyncRepository

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val consent = ConsentStore(applicationContext)
        if (!consent.syncEnabled()) return Result.success()

        return try {
            val deviceId = consent.getOrCreateDeviceId()
            val folderStore = SyncFolderStore(applicationContext)
            val repo = SyncRepository(applicationContext, ApiClient.create(), folderStore)
            repo.syncAll(deviceId)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}