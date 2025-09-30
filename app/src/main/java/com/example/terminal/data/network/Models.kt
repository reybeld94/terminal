package com.example.terminal.data.network

import com.google.gson.annotations.SerializedName

data class ClockInRequest(
    @SerializedName("workOrderCollectionId") val workOrderCollectionId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("qty") val qty: Int
)

data class ClockOutRequest(
    @SerializedName("workOrderCollectionId") val workOrderCollectionId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("qty") val qty: Int,
    @SerializedName("status") val status: String
)

data class ApiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

enum class ClockOutStatus(val apiValue: String) {
    COMPLETE("Complete"),
    INCOMPLETE("Incomplete");

    override fun toString(): String = apiValue
}
