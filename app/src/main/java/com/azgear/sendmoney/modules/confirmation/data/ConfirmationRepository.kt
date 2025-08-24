package com.azgear.sendmoney.modules.confirmation.data

import com.azgear.sendmoney.core.utils.ResultState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class TransactionConfirmation(
    val transactionId: String,
    val recipientName: String,
    val amount: Double,
    val note: String?,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

interface ConfirmationRepository {
    suspend fun confirmTransaction(
        recipientName: String,
        amount: Double,
        note: String?
    ): Flow<ResultState<TransactionConfirmation>>
}

class ConfirmationRepositoryImpl : ConfirmationRepository {
    
    override suspend fun confirmTransaction(
        recipientName: String,
        amount: Double,
        note: String?
    ): Flow<ResultState<TransactionConfirmation>> = flow {
        emit(ResultState.Loading)
        
        try {
            delay(2000) // Simulate API processing
            
            val confirmation = TransactionConfirmation(
                transactionId = "TXN${System.currentTimeMillis()}",
                recipientName = recipientName,
                amount = amount,
                note = note,
                status = "COMPLETED"
            )
            
            emit(ResultState.Success(confirmation))
            
        } catch (e: Exception) {
            emit(ResultState.Error("Transaction failed. Please try again.", e))
        }
    }
}