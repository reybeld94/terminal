package com.example.terminal.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val DEFAULT_BASE_URL = "http://<IP-SERVER>:8080/"

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
    @Volatile
    private var apiService: ApiService? = null

    fun getApiService(baseUrl: String = DEFAULT_BASE_URL): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: buildRetrofit(baseUrl).also { apiService = it }
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
