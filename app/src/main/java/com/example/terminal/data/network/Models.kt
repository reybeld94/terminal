package com.example.terminal.data.network

import com.google.gson.annotations.SerializedName

data class ClockInRequest(
    @SerializedName("workOrderAssemblyId") val workOrderAssemblyId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("divisionFK") val divisionFK: Int,
    @SerializedName("deviceDate") val deviceDate: String
)

data class ClockOutRequest(
    @SerializedName("workOrderCollectionId") val workOrderCollectionId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("quantityScrapped") val quantityScrapped: Int,
    @SerializedName("scrapReasonPK") val scrapReasonPK: Int,
    @SerializedName("complete") val complete: Boolean,
    @SerializedName("comment") val comment: String,
    @SerializedName("deviceTime") val deviceTime: String,
    @SerializedName("divisionFK") val divisionFK: Int
)

data class ApiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

enum class ClockOutStatus(val isComplete: Boolean, val displayName: String) {
    COMPLETE(true, "Complete"),
    INCOMPLETE(false, "Incomplete");

    override fun toString(): String = displayName
}
