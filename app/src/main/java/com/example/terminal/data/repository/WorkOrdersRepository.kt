package com.example.terminal.data.repository

import com.example.terminal.data.auth.TokenProvider
import com.example.terminal.data.auth.TokenRefreshHandler
import com.example.terminal.data.local.UserPrefs
import com.example.terminal.data.network.ApiClient
import com.example.terminal.data.network.ApiResponse
import com.example.terminal.data.network.ClockInRequest
import com.example.terminal.data.network.ClockOutRequest
import com.example.terminal.data.network.ClockOutStatus
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class WorkOrdersRepository(
    private val userPrefs: UserPrefs,
    private val tokenProvider: TokenProvider,
    private val refreshHandler: TokenRefreshHandler = TokenRefreshHandler { null },
    private val gson: Gson = Gson()
) {

    init {
        ApiClient.configure(tokenProvider, refreshHandler)
    }
    suspend fun clockIn(
        workOrderCollectionId: Int,
        userId: Int,
        qty: Int
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        val baseUrl = userPrefs.serverAddress.first()
        runCatching {
            val apiService = ApiClient.getApiService(baseUrl)
            val response = apiService.clockIn(
                ClockInRequest(
                    workOrderCollectionId = workOrderCollectionId,
                    userId = userId,
                    qty = qty
                )
            )

            if (response.isSuccessful) {
                val body = response.body() ?: throw IllegalStateException("Respuesta vacía del servidor")
                if (body.status == "success") {
                    body
                } else {
                    val message = body.message.takeIf { it.isNotBlank() }
                        ?: "La operación de Clock In no se completó correctamente"
                    throw IllegalStateException(message)
                }
            } else {
                val errorMessage = parseError(response.errorBody()?.string())
                throw IllegalStateException(errorMessage)
            }
        }
    }

    suspend fun clockOut(
        workOrderCollectionId: Int,
        userId: Int,
        qty: Int,
        status: ClockOutStatus
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        val baseUrl = userPrefs.serverAddress.first()
        runCatching {
            val apiService = ApiClient.getApiService(baseUrl)
            val response = apiService.clockOut(
                ClockOutRequest(
                    workOrderCollectionId = workOrderCollectionId,
                    userId = userId,
                    qty = qty,
                    status = status.apiValue
                )
            )

            if (response.isSuccessful) {
                val body = response.body() ?: throw IllegalStateException("Respuesta vacía del servidor")
                if (body.status == "success") {
                    body
                } else {
                    val message = body.message.takeIf { it.isNotBlank() }
                        ?: "La operación de Clock Out no se completó correctamente"
                    throw IllegalStateException(message)
                }
            } else {
                val errorMessage = parseError(response.errorBody()?.string())
                throw IllegalStateException(errorMessage)
            }
        }
    }

    private fun parseError(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) {
            return "Error desconocido"
        }

        return try {
            val parsed = gson.fromJson(errorBody, ApiResponse::class.java)
            parsed?.message?.takeIf { it.isNotBlank() } ?: errorBody
        } catch (ex: Exception) {
            errorBody
        }
    }
}
