package com.azgear.sendmoney.modules.transactions.data

import com.azgear.sendmoney.core.utils.ResultState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

// AI-Generated Code: Updated Transaction data class with formatted status
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val recipientName: String,
    val amount: Double,
    val note: String? = null,
    val date: Date = Date(),
    val status: TransactionStatus = TransactionStatus.COMPLETED
) {
    val formattedAmount: String
        get() = String.format("-$%.2f", amount)
    
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    
    val formattedStatus: String
        get() = status.name.replace("_", " ")
    
    val hasNote: Boolean
        get() = !note.isNullOrBlank()
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
}

data class TransactionsResponse(
    val transactions: List<Transaction>,
    val totalCount: Int
)

interface TransactionsRepository {
    suspend fun getTransactions(): Flow<ResultState<TransactionsResponse>>
    suspend fun addTransaction(transaction: Transaction): Flow<ResultState<Boolean>>
    suspend fun getSavedRequests(): Flow<ResultState<SavedRequestsResponse>>
    suspend fun saveRequest(request: SavedRequest): Flow<ResultState<Boolean>>
    suspend fun getRequestById(id: String): Flow<ResultState<SavedRequest>>
}

class TransactionsRepositoryImpl : TransactionsRepository {
    
    private val _transactions = mutableListOf<Transaction>()
    private val _savedRequests = mutableListOf<SavedRequest>()
    
    init {
        // Add some sample data
        _transactions.addAll(getSampleTransactions())
        _savedRequests.addAll(getSampleSavedRequests())
    }
    
    override suspend fun getTransactions(): Flow<ResultState<TransactionsResponse>> = flow {
        emit(ResultState.Loading)
        
        try {
            delay(1000) // Simulate API call
            
            val response = TransactionsResponse(
                transactions = _transactions.sortedByDescending { it.date },
                totalCount = _transactions.size
            )
            
            emit(ResultState.Success(response))
            
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to load transactions", e))
        }
    }
    
    override suspend fun addTransaction(transaction: Transaction): Flow<ResultState<Boolean>> = flow {
        emit(ResultState.Loading)
        
        try {
            delay(500)
            _transactions.add(0, transaction)
            emit(ResultState.Success(true))
            
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to add transaction", e))
        }
    }
    
    override suspend fun getSavedRequests(): Flow<ResultState<SavedRequestsResponse>> = flow {
        emit(ResultState.Loading)
        
        try {
            delay(800) // Simulate API call
            
            val response = SavedRequestsResponse(
                requests = _savedRequests.sortedByDescending { it.timestamp },
                totalCount = _savedRequests.size
            )
            
            emit(ResultState.Success(response))
            
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to load saved requests", e))
        }
    }
    
    override suspend fun saveRequest(request: SavedRequest): Flow<ResultState<Boolean>> = flow {
        emit(ResultState.Loading)
        
        try {
            delay(300)
            _savedRequests.add(0, request)
            emit(ResultState.Success(true))
            
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to save request", e))
        }
    }
    
    override suspend fun getRequestById(id: String): Flow<ResultState<SavedRequest>> = flow {
        emit(ResultState.Loading)
        
        try {
            delay(200)
            val request = _savedRequests.find { it.id == id }
            
            if (request != null) {
                emit(ResultState.Success(request))
            } else {
                emit(ResultState.Error("Request not found", null))
            }
            
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to load request details", e))
        }
    }
    
    private fun getSampleTransactions(): List<Transaction> {
        return listOf(
            Transaction(
                recipientName = "John Doe",
                amount = 100.0,
                note = "Payment for dinner",
                date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
                status = TransactionStatus.COMPLETED
            ),
            Transaction(
                recipientName = "Jane Smith",
                amount = 50.0,
                note = null,
                date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -3) }.time,
                status = TransactionStatus.COMPLETED
            ),
            Transaction(
                recipientName = "Bob Johnson",
                amount = 25.0,
                note = "Coffee money",
                date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }.time,
                status = TransactionStatus.COMPLETED
            )
        )
    }
    
    private fun getSampleSavedRequests(): List<SavedRequest> {
        return listOf(
            SavedRequest(
                id = "REQ001",
                serviceName = "Bank Transfer",
                providerName = "ABC Bank",
                amount = 250.0,
                formData = com.azgear.sendmoney.modules.sendmoney.data.FormData(
                    serviceId = "bank_transfer",
                    providerId = "101",
                    fields = mutableMapOf(
                        "amount" to "250.0",
                        "bank_account_number" to "1234567890",
                        "firstname" to "Ahmed",
                        "lastname" to "Al-Mansouri"
                    )
                ),
                timestamp = Calendar.getInstance().apply { add(Calendar.HOUR, -2) }.time,
                status = RequestStatus.SAVED
            ),
            SavedRequest(
                id = "REQ002",
                serviceName = "Wallet Transfer",
                providerName = "Quick Pay",
                amount = 75.5,
                formData = com.azgear.sendmoney.modules.sendmoney.data.FormData(
                    serviceId = "wallet_transfer",
                    providerId = "502",
                    fields = mutableMapOf(
                        "amount" to "75.5",
                        "msisdn" to "+971501234567",
                        "full_name" to "Sara Mohammed",
                        "gender" to "F"
                    )
                ),
                timestamp = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
                status = RequestStatus.SUBMITTED
            ),
            SavedRequest(
                id = "REQ003",
                serviceName = "Bank Transfer",
                providerName = "Global Bank",
                amount = 500.0,
                formData = com.azgear.sendmoney.modules.sendmoney.data.FormData(
                    serviceId = "bank_transfer",
                    providerId = "103",
                    fields = mutableMapOf(
                        "amount" to "500.0",
                        "bank_account_number" to "9876543210",
                        "date_of_birth" to "1990-05-15",
                        "gender" to "M"
                    )
                ),
                timestamp = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -3) }.time,
                status = RequestStatus.COMPLETED
            )
        )
    }
}