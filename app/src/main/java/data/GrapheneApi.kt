package com.graphene.antivirus.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface GrapheneApi {

    /** Единственный endpoint без Authorization. */
    @POST("api/v1/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("api/v1/scan")
    suspend fun uploadTelemetry(@Body request: ScanRequest): Response<ScanResponse>

    @Multipart
    @POST("api/v1/upload")
    suspend fun uploadFile(
        @Part("device_id") deviceId: RequestBody,
        @Part("request_id") requestId: RequestBody?,
        @Part("relative_path") relativePath: RequestBody?,
        @Part file: MultipartBody.Part
    ): Response<UploadFileResponse>

    @POST("api/v1/sync/folders")
    suspend fun registerFolders(@Body body: SyncFoldersRequest): Response<SimpleOk>

    @POST("api/v1/sync/files")
    suspend fun uploadFileList(@Body body: SyncFilesRequest): Response<SimpleOk>

    @GET("api/v1/sync/pending")
    suspend fun getPendingRequests(@Query("device_id") deviceId: String): Response<PendingRequestsResponse>
}