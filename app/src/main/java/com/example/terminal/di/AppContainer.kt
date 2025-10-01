package com.example.terminal.di

import android.content.Context
import com.example.terminal.data.auth.DataStoreTokenProvider
import com.example.terminal.data.auth.TokenProvider
import com.example.terminal.data.local.UserPrefs
import com.example.terminal.data.network.ApiClient
import com.example.terminal.data.repository.AuthRepository
import com.example.terminal.data.repository.WorkOrdersRepository

object AppContainer {
    @Volatile
    private var tokenProvider: TokenProvider? = null

    fun tokenProvider(context: Context): TokenProvider {
        val appContext = context.applicationContext
        return tokenProvider ?: synchronized(this) {
            tokenProvider ?: DataStoreTokenProvider.getInstance(appContext).also { provider ->
                tokenProvider = provider
                ApiClient.configure(provider)
            }
        }
    }

    fun authRepository(context: Context): AuthRepository {
        return AuthRepository(tokenProvider(context))
    }

    fun workOrdersRepository(
        context: Context,
        userPrefs: UserPrefs = UserPrefs.create(context.applicationContext)
    ): WorkOrdersRepository {
        return WorkOrdersRepository(userPrefs, tokenProvider(context))
    }
}
