package com.azgear.sendmoney.modules.sendmoney.data

import android.content.Context
import com.azgear.sendmoney.core.utils.ResultState
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

data class SendMoneyRequest(
    val recipientName: String,
    val amount: Double,
    val note: String? = null
)

data class SendMoneyResponse(
    val transactionId: String,
    val status: String,
    val message: String,
    val recipientName: String? = null,
    val amount: Double? = null,
    val timestamp: Date = Date()
) {
    val formattedAmount: String
        get() = amount?.let { String.format("$%.2f", it) } ?: ""
    
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(timestamp)
}

interface SendMoneyRepository {
    suspend fun validateRecipient(name: String): Flow<ResultState<Boolean>>
    suspend fun sendMoney(request: SendMoneyRequest): Flow<ResultState<SendMoneyResponse>>
    suspend fun loadSendMoneyConfig(): Flow<ResultState<SendMoneyConfig>>
    suspend fun submitDynamicForm(formData: FormData): Flow<ResultState<SendMoneyResponse>>
}

class SendMoneyRepositoryImpl(private val context: Context? = null) : SendMoneyRepository {
    
    private val gson = Gson()
    
    override suspend fun validateRecipient(name: String): Flow<ResultState<Boolean>> = flow {
        emit(ResultState.Loading)
        
        try {
            delay(500) // Simulate API call
            
            // Simple validation - in real app this would check against user database
            val isValid = name.trim().length >= 2
            emit(ResultState.Success(isValid))
            
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to validate recipient", e))
        }
    }
    
    override suspend fun sendMoney(request: SendMoneyRequest): Flow<ResultState<SendMoneyResponse>> = flow {
        emit(ResultState.Loading)
        
        try {
            delay(2000) // Simulate API call
            
            val response = SendMoneyResponse(
                transactionId = "TXN${System.currentTimeMillis()}",
                status = "SUCCESS",
                message = "Money sent successfully",
                recipientName = request.recipientName,
                amount = request.amount
            )
            
            emit(ResultState.Success(response))
            
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to send money", e))
        }
    }
    
    override suspend fun loadSendMoneyConfig(): Flow<ResultState<SendMoneyConfig>> = flow {
        emit(ResultState.Loading)
        
        try {
            if (context == null) {
                throw IllegalStateException("Context is required to load configuration")
            }
            
            // Load JSON from assets
            val jsonString = context.assets.open("send_money_services.json").bufferedReader().use { it.readText() }
            
            // Parse JSON to data model
            val config = gson.fromJson(jsonString, SendMoneyConfig::class.java)
            
            emit(ResultState.Success(config))
        } catch (e: JsonSyntaxException) {
            emit(ResultState.Error("Failed to parse service configuration: ${e.message}", e))
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to load service configuration: ${e.message}", e))
        }
    }
    
    override suspend fun submitDynamicForm(formData: FormData): Flow<ResultState<SendMoneyResponse>> = flow {
        emit(ResultState.Loading)
        
        try {
            delay(2000) // Simulate API call
            
            // Extract recipient name from form data
            val recipientName = when {
                formData.getField("full_name")?.isNotEmpty() == true -> formData.getField("full_name")!!
                formData.getField("firstname")?.isNotEmpty() == true && formData.getField("lastname")?.isNotEmpty() == true -> 
                    "${formData.getField("firstname")} ${formData.getField("lastname")}"
                formData.getField("lastname")?.isNotEmpty() == true -> formData.getField("lastname")!!
                else -> "Unknown Recipient"
            }
            
            // Extract amount
            val amount = formData.getField("amount")?.toDoubleOrNull() ?: 0.0
            
            val response = SendMoneyResponse(
                transactionId = "TXN${System.currentTimeMillis()}",
                status = "SUCCESS",
                message = "Money sent successfully via dynamic form",
                recipientName = recipientName,
                amount = amount
            )
            
            emit(ResultState.Success(response))
            
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to process transaction: ${e.message}", e))
        }
    }
}