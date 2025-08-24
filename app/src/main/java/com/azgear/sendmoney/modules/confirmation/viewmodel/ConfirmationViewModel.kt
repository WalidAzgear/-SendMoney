package com.azgear.sendmoney.modules.confirmation.viewmodel

import androidx.lifecycle.viewModelScope
import com.azgear.sendmoney.core.base.BaseViewModel
import com.azgear.sendmoney.core.utils.ResultState
import com.azgear.sendmoney.modules.confirmation.data.ConfirmationRepository
import com.azgear.sendmoney.modules.confirmation.data.ConfirmationRepositoryImpl
import com.azgear.sendmoney.modules.confirmation.data.TransactionConfirmation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConfirmationUiState(
    val recipientName: String = "",
    val amount: Double = 0.0,
    val note: String? = null,
    val formattedAmount: String = "",
    val hasNote: Boolean = false
)

class ConfirmationViewModel(
    private val repository: ConfirmationRepository = ConfirmationRepositoryImpl()
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(ConfirmationUiState())
    val uiState: StateFlow<ConfirmationUiState> = _uiState.asStateFlow()
    
    private val _confirmationState = MutableStateFlow<ResultState<TransactionConfirmation>>(ResultState.Idle)
    val confirmationState: StateFlow<ResultState<TransactionConfirmation>> = _confirmationState.asStateFlow()
    
    fun setTransactionData(recipient: String, amount: Double, note: String?) {
        _uiState.value = ConfirmationUiState(
            recipientName = recipient,
            amount = amount,
            note = note,
            formattedAmount = String.format("$%.2f", amount),
            hasNote = !note.isNullOrBlank()
        )
    }
    
    fun confirmTransaction() {
        val currentState = _uiState.value
        
        viewModelScope.launch {
            repository.confirmTransaction(
                recipientName = currentState.recipientName,
                amount = currentState.amount,
                note = currentState.note
            ).collect { result ->
                _confirmationState.value = result
                setLoading(result is ResultState.Loading)
                
                if (result is ResultState.Error) {
                    setError(result.message)
                } else {
                    clearError()
                }
            }
        }
    }
}