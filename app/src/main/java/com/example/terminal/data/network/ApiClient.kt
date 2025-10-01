package com.example.terminal.data.network

import com.example.terminal.data.auth.TokenProvider
import com.example.terminal.data.auth.TokenRefreshHandler
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        tokenProvider.getCachedToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer ${token.value}")
        }

        return chain.proceed(requestBuilder.build())
    }
}

private class TokenRefreshAuthenticator(
    private val tokenProvider: TokenProvider,
    private val refreshHandler: TokenRefreshHandler
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            runBlocking { tokenProvider.invalidateToken() }
            return null
        }

        val currentToken = tokenProvider.getCachedToken()
        val refreshed = runBlocking { refreshHandler.refreshToken(currentToken) }
        return if (refreshed != null && refreshed.value.isNotBlank()) {
            runBlocking { tokenProvider.persistToken(refreshed) }
            response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshed.value}")
                .build()
        } else {
            runBlocking { tokenProvider.invalidateToken() }
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var current: Response? = response
        var count = 1
        while (current?.priorResponse != null) {
            current = current.priorResponse
            count++
        }
        return count
    }
}

object ApiClient {
    const val DEFAULT_BASE_URL = "http://<IP-SERVER>:8080/"

    @Volatile
    private var apiService: ApiService? = null
    @Volatile
    private var currentBaseUrl: String = DEFAULT_BASE_URL
    @Volatile
    private var tokenProvider: TokenProvider? = null
    @Volatile
    private var tokenRefreshHandler: TokenRefreshHandler = TokenRefreshHandler { null }

    fun configure(
        provider: TokenProvider,
        refreshHandler: TokenRefreshHandler = TokenRefreshHandler { null }
    ) {
        tokenProvider = provider
        tokenRefreshHandler = refreshHandler
        synchronized(this) { apiService = null }
    }

    fun getApiService(baseUrl: String = currentBaseUrl): ApiService {
        val provider = tokenProvider
            ?: throw IllegalStateException("TokenProvider must be configured before creating ApiService")
        if (baseUrl != currentBaseUrl) {
            synchronized(this) {
                if (baseUrl != currentBaseUrl) {
                    currentBaseUrl = baseUrl
                    apiService = null
                }
            }
        }
        return apiService ?: synchronized(this) {
            apiService ?: buildRetrofit(currentBaseUrl, provider).also { apiService = it }
        }
    }

    fun updateBaseUrl(baseUrl: String) {
        synchronized(this) {
            currentBaseUrl = baseUrl
            apiService = null
        }
    }

    private fun buildRetrofit(baseUrl: String, provider: TokenProvider): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(provider))
            .authenticator(TokenRefreshAuthenticator(provider, tokenRefreshHandler))
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
