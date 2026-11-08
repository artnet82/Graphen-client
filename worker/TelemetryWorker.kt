package com.graphene.antivirus.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.graphene.antivirus.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TelemetryWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val consent = ConsentStore(applicationContext)
            val deviceId = consent.getOrCreateDeviceId()
            val repo = ScanRepository(ApiClient.create(), applicationContext)
            val res = repo.performScan(deviceId) { _, _ -> }
            if (res.isSuccess) Result.success() else Result.retry()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}