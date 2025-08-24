package com.azgear.sendmoney.modules.home.data

import com.azgear.sendmoney.core.utils.ResultState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class BalanceResponse(
    val balance: Double,
    val currency: String = "USD"
) {
    val formattedBalance: String
        get() = String.format("$%.2f", balance)
}

interface HomeRepository {
    suspend fun getBalance(): Flow<ResultState<BalanceResponse>>
}

class HomeRepositoryImpl : HomeRepository {
    
    override suspend fun getBalance(): Flow<ResultState<BalanceResponse>> = flow {
        emit(ResultState.Loading)
        
        try {
            // Simulate API call
            delay(1000)
            
            val balance = BalanceResponse(balance = 1250.0)
            emit(ResultState.Success(balance))
            
        } catch (e: Exception) {
            emit(ResultState.Error("Failed to load balance", e))
        }
    }
}