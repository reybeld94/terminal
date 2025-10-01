package com.example.terminal.data.auth

/**
 * Allows the networking layer to request a refreshed JWT when the server indicates that the
 * current token is no longer valid. Returning null indicates that a refresh could not be performed
 * and the request should not be retried.
 */
fun interface TokenRefreshHandler {
    suspend fun refreshToken(currentToken: AuthToken?): AuthToken?
}
