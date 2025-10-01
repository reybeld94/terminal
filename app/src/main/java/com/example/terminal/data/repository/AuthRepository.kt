package com.example.terminal.data.repository

import com.example.terminal.data.auth.AuthToken
import com.example.terminal.data.auth.TokenProvider
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val tokenProvider: TokenProvider
) {

    val activeToken: StateFlow<AuthToken?> = tokenProvider.tokenFlow

    suspend fun persistSession(token: String, expiresAtEpochSeconds: Long? = null) {
        tokenProvider.persistToken(AuthToken(token, expiresAtEpochSeconds))
    }

    suspend fun persistSession(token: AuthToken) {
        tokenProvider.persistToken(token)
    }

    suspend fun invalidateSession() {
        tokenProvider.invalidateToken()
    }
}
