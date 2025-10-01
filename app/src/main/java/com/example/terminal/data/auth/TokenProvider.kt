package com.example.terminal.data.auth

import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for providing authentication tokens to the networking layer.
 */
interface TokenProvider {
    /** Emits updates for the currently persisted token. */
    val tokenFlow: StateFlow<AuthToken?>

    /** Returns the cached token if present and still valid. */
    fun getCachedToken(): AuthToken?

    /**
     * Persists the provided [token] replacing any previous session and notifying observers.
     */
    suspend fun persistToken(token: AuthToken)

    /** Clears the stored token and notifies observers. */
    suspend fun invalidateToken()
}
