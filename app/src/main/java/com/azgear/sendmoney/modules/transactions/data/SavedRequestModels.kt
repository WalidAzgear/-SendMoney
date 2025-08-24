package com.azgear.sendmoney.modules.transactions.data

import com.azgear.sendmoney.modules.sendmoney.data.FormData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Model representing a saved send money request
 */
data class SavedRequest(
    val id: String = UUID.randomUUID().toString(),
    val serviceName: String,
    val providerName: String,
    val amount: Double,
    val formData: FormData,
    val timestamp: Date = Date(),
    val status: RequestStatus = RequestStatus.SAVED
) {
    val formattedAmount: String
        get() = String.format("%.2f AED", amount)
    
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(timestamp)
    
    /**
     * Convert the request to formatted JSON string
     */
    fun toFormattedJson(): String {
        val jsonBuilder = StringBuilder()
        jsonBuilder.append("{\n")
        jsonBuilder.append("  \"requestId\": \"$id\",\n")
        jsonBuilder.append("  \"serviceName\": \"$serviceName\",\n")
        jsonBuilder.append("  \"providerName\": \"$providerName\",\n")
        jsonBuilder.append("  \"amount\": $amount,\n")
        jsonBuilder.append("  \"currency\": \"AED\",\n")
        jsonBuilder.append("  \"timestamp\": \"${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(timestamp)}\",\n")
        jsonBuilder.append("  \"status\": \"${status.name}\",\n")
        jsonBuilder.append("  \"formData\": {\n")
        jsonBuilder.append("    \"serviceId\": \"${formData.serviceId}\",\n")
        jsonBuilder.append("    \"providerId\": \"${formData.providerId}\",\n")
        jsonBuilder.append("    \"fields\": {\n")
        
        val fields = formData.fields.entries.toList()
        fields.forEachIndexed { index, (key, value) ->
            val isLast = index == fields.size - 1
            jsonBuilder.append("      \"$key\": \"$value\"${if (!isLast) "," else ""}\n")
        }
        
        jsonBuilder.append("    }\n")
        jsonBuilder.append("  }\n")
        jsonBuilder.append("}")
        
        return jsonBuilder.toString()
    }
}

/**
 * Status enum for saved requests
 */
enum class RequestStatus {
    SAVED,
    SUBMITTED,
    COMPLETED,
    FAILED
}

/**
 * Response model for saved requests list
 */
data class SavedRequestsResponse(
    val requests: List<SavedRequest>,
    val totalCount: Int
) 