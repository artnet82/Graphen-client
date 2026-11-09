package com.graphene.antivirus.data

import android.content.Context
import android.net.Uri
import com.graphene.antivirus.scanner.FolderScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

data class SyncResult(
    val folders: Int,
    val filesIndexed: Int,
    val filesSent: Int,
    val error: String? = null
)

class SyncRepository(
    private val context: Context,
    private val api: GrapheneApi,
    private val folderStore: SyncFolderStore
) {

    suspend fun syncAll(deviceId: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            val folders = folderStore.getFolders()
            if (folders.isEmpty()) {
                return@withContext SyncResult(0, 0, 0)
            }

            // 1. Регистрируем папки на сервере
            try {
                api.registerFolders(
                    SyncFoldersRequest(
                        deviceId = deviceId,
                        folders = folders.map { FolderPayload(it.uri, it.name) }
                    )
                )
            } catch (_: Exception) { }

            // 2. Сканируем
            val scanner = FolderScanner(context)
            val files = scanner.scanAll(folders)

            // 3. Отправляем список
            try {
                api.uploadFileList(
                    SyncFilesRequest(
                        deviceId = deviceId,
                        files = files.map {
                            SyncedFilePayload(
                                folderUri = it.folderUri,
                                relativePath = it.relativePath,
                                name = it.name,
                                size = it.size,
                                modified = it.modified,
                                mime = it.mime,
                                sha256 = it.sha256
                            )
                        }
                    )
                )
            } catch (_: Exception) { }

            // 4. Забираем pending-запросы и отправляем файлы
            var sent = 0
            try {
                val pending = api.getPendingRequests(deviceId)
                for (req in pending.items) {
                    val file = files.find { it.relativePath == req.relativePath } ?: continue
                    if (uploadFile(file, req.requestId, deviceId)) sent++
                }
            } catch (_: Exception) { }

            SyncResult(folders.size, files.size, sent)
        } catch (e: Exception) {
            SyncResult(0, 0, 0, error = e.message)
        }
    }

    private suspend fun uploadFile(
        file: SyncedFileMeta,
        requestId: String,
        deviceId: String
    ): Boolean = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        try {
            tempFile = File(context.cacheDir, "sync_${System.currentTimeMillis()}_${file.name}")
            context.contentResolver.openInputStream(Uri.parse(file.documentUri))?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext false

            val mediaType = "application/octet-stream".toMediaType()
            val part = MultipartBody.Part.createFormData("file", file.name, tempFile.asRequestBody(mediaType))

            val resp = api.uploadFile(
                deviceId.toRequestBody("text/plain".toMediaType()),
                requestId.toRequestBody("text/plain".toMediaType()),
                file.relativePath.toRequestBody("text/plain".toMediaType()),
                part
            )
            resp.isSuccessful
        } catch (_: Exception) {
            false
        } finally {
            try { tempFile?.delete() } catch (_: Exception) { }
        }
    }
}