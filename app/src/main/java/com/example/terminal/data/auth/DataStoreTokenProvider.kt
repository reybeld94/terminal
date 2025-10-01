package com.example.terminal.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val DATA_STORE_NAME = "auth_tokens"
private val TOKEN_KEY = stringPreferencesKey("jwt")
private val TOKEN_EXPIRATION_KEY = longPreferencesKey("jwt_expiration")

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

/**
 * DataStore backed implementation for [TokenProvider].
 */
class DataStoreTokenProvider private constructor(private val appContext: Context) : TokenProvider {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _tokenState = MutableStateFlow<AuthToken?>(null)

    override val tokenFlow: StateFlow<AuthToken?> = _tokenState.asStateFlow()

    init {
        scope.launch {
            appContext.authDataStore.data
                .map { preferences ->
                    val token = preferences[TOKEN_KEY]
                    val expiration = preferences[TOKEN_EXPIRATION_KEY]
                    token?.let { AuthToken(it, expiration) }
                }
                .catch { exception ->
                    if (exception is IOException) {
                        _tokenState.value = null
                    } else {
                        throw exception
                    }
                }
                .collect { token ->
                    if (token?.isExpired() == true) {
                        clearTokenInternal()
                    } else {
                        _tokenState.value = token
                    }
                }
        }
    }

    override fun getCachedToken(): AuthToken? {
        val cached = _tokenState.value
        if (cached?.isExpired() == true) {
            scope.launch { clearTokenInternal() }
            return null
        }
        return cached
    }

    override suspend fun persistToken(token: AuthToken) {
        clearTokenInternal()
        appContext.authDataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token.value
            val expiration = token.expiresAtEpochSeconds
            if (expiration != null) {
                preferences[TOKEN_EXPIRATION_KEY] = expiration
            } else {
                preferences.remove(TOKEN_EXPIRATION_KEY)
            }
        }
        _tokenState.value = token
    }

    override suspend fun invalidateToken() {
        clearTokenInternal()
        _tokenState.value = null
    }

    private suspend fun clearTokenInternal() {
        appContext.authDataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(TOKEN_EXPIRATION_KEY)
        }
    }

    companion object {
        @Volatile
        private var instance: DataStoreTokenProvider? = null

        fun getInstance(context: Context): DataStoreTokenProvider {
            val appContext = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: DataStoreTokenProvider(appContext).also { instance = it }
            }
        }
    }
}
