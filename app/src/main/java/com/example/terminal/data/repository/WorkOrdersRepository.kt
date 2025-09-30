package com.example.terminal.data.repository

import com.example.terminal.data.network.ApiResponse
import com.example.terminal.data.network.ApiService
import com.example.terminal.data.network.ClockInRequest
import com.example.terminal.data.network.ClockOutRequest
import com.example.terminal.data.network.ClockOutStatus
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkOrdersRepository(
    private val apiService: ApiService,
    private val gson: Gson = Gson()
) {
    suspend fun clockIn(
        workOrderCollectionId: Int,
        userId: Int,
        qty: Int
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.clockIn(
                ClockInRequest(
                    workOrderCollectionId = workOrderCollectionId,
                    userId = userId,
                    qty = qty
                )
            )

            if (response.isSuccessful) {
                response.body() ?: throw IllegalStateException("Respuesta vacía del servidor")
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
        runCatching {
            val response = apiService.clockOut(
                ClockOutRequest(
                    workOrderCollectionId = workOrderCollectionId,
                    userId = userId,
                    qty = qty,
                    status = status.apiValue
                )
            )

            if (response.isSuccessful) {
                response.body() ?: throw IllegalStateException("Respuesta vacía del servidor")
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
