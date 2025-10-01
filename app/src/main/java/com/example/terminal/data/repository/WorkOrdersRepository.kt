package com.example.terminal.data.repository

import android.os.Build
import com.example.terminal.data.local.UserPrefs
import com.example.terminal.data.network.ApiClient
import com.example.terminal.data.network.ApiResponse
import com.example.terminal.data.network.ClockInRequest
import com.example.terminal.data.network.ClockOutRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class WorkOrdersRepository(
    private val userPrefs: UserPrefs,
    private val gson: Gson = Gson()
) {
    suspend fun clockIn(
        workOrderAssemblyId: Int,
        userId: Int
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        val baseUrl = userPrefs.serverAddress.first()
        try {
            val apiService = ApiClient.getApiService(baseUrl)
            val response = apiService.clockIn(
                ClockInRequest(
                    workOrderAssemblyId = workOrderAssemblyId,
                    userId = userId,
                    divisionFK = DIVISION_FK,
                    deviceDate = currentIsoDateTime()
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                when {
                    body == null -> Result.failure(IllegalStateException("Respuesta vacía del servidor"))
                    body.status != "success" -> {
                        val message = body.message.takeIf { it.isNotBlank() }
                            ?: "Operación de Clock In rechazada por el servidor"
                        Result.failure(IllegalStateException(message))
                    }
                    else -> Result.success(body)
                }
            } else {
                val errorMessage = parseError(response.errorBody()?.string())
                Result.failure(IllegalStateException(errorMessage))
            }
        } catch (ex: Exception) {
            Result.failure(ex)
        }
    }

    suspend fun clockOut(
        workOrderCollectionId: Int,
        quantity: Int,
        complete: Boolean,
        quantityScrapped: Int = 0,
        scrapReasonPK: Int = 0,
        comment: String = ""
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        val baseUrl = userPrefs.serverAddress.first()
        try {
            val apiService = ApiClient.getApiService(baseUrl)
            val response = apiService.clockOut(
                ClockOutRequest(
                    workOrderCollectionId = workOrderCollectionId,
                    quantity = quantity,
                    quantityScrapped = quantityScrapped,
                    scrapReasonPK = scrapReasonPK,
                    complete = complete,
                    comment = comment,
                    deviceTime = currentIsoDateTime(),
                    divisionFK = DIVISION_FK
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                when {
                    body == null -> Result.failure(IllegalStateException("Respuesta vacía del servidor"))
                    body.status != "success" -> {
                        val message = body.message.takeIf { it.isNotBlank() }
                            ?: "Operación de Clock Out rechazada por el servidor"
                        Result.failure(IllegalStateException(message))
                    }
                    else -> Result.success(body)
                }
            } else {
                val errorMessage = parseError(response.errorBody()?.string())
                Result.failure(IllegalStateException(errorMessage))
            }
        } catch (ex: Exception) {
            Result.failure(ex)
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

    private fun currentIsoDateTime(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } else {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            formatter.timeZone = TimeZone.getDefault()
            formatter.format(Date())
        }
    }

    private companion object {
        const val DIVISION_FK = 1
    }
}
