package com.example.terminal.data.auth

/**
 * Represents an authentication token along with its optional expiration date expressed in
 * epoch seconds.
 */
data class AuthToken(
    val value: String,
    val expiresAtEpochSeconds: Long? = null
) {
    fun isExpired(currentEpochSeconds: Long = System.currentTimeMillis() / 1000): Boolean {
        val expiresAt = expiresAtEpochSeconds ?: return false
        return currentEpochSeconds >= expiresAt
    }
}
