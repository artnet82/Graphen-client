package com.graphene.antivirus.data

import android.content.Context
import com.graphene.antivirus.BuildConfig
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    val baseUrl: String = BuildConfig.SERVER_URL   // http://77.74.201.204:8000/

    private var appContext: Context? = null

    /** Обязательно вызвать в GrapheneApp.onCreate(). */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private class AuthInterceptor(private val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = runBlocking { ConsentStore(context).getApiToken() }
            val builder = chain.request().newBuilder()
                .header("Accept", "application/json")
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
            return chain.proceed(builder.build())
        }
    }

    private fun logging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

    /** Обычный клиент — с Bearer-токеном. */
    fun create(): GrapheneApi {
        val ctx = appContext ?: error("ApiClient.init(context) must be called in Application")

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(ctx))
            .addInterceptor(logging())
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GrapheneApi::class.java)
    }

    /** Для /api/v1/register — без Authorization. */
    fun createUnauthenticated(): GrapheneApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logging())
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GrapheneApi::class.java)
    }
}