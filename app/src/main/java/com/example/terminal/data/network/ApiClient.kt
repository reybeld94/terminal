package com.example.terminal.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = getToken()
        val requestBuilder = chain.request().newBuilder()
        if (token.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}

fun getToken(): String = "REPLACE_WITH_REAL_TOKEN" // TODO integrar con login.

object ApiClient {
    const val DEFAULT_BASE_URL = "http://<IP-SERVER>:8080/"

    @Volatile
    private var apiService: ApiService? = null
    @Volatile
    private var currentBaseUrl: String = DEFAULT_BASE_URL

    fun getApiService(baseUrl: String = currentBaseUrl): ApiService {
        if (baseUrl != currentBaseUrl) {
            synchronized(this) {
                if (baseUrl != currentBaseUrl) {
                    currentBaseUrl = baseUrl
                    apiService = null
                }
            }
        }
        return apiService ?: synchronized(this) {
            apiService ?: buildRetrofit(currentBaseUrl).also { apiService = it }
        }
    }

    fun updateBaseUrl(baseUrl: String) {
        synchronized(this) {
            currentBaseUrl = baseUrl
            apiService = null
        }
    }

    private fun buildRetrofit(baseUrl: String): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
