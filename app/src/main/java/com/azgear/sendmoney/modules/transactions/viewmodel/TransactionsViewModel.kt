package com.azgear.sendmoney.modules.transactions.viewmodel

import androidx.lifecycle.viewModelScope
import com.azgear.sendmoney.core.base.BaseViewModel
import com.azgear.sendmoney.core.utils.ResultState
import com.azgear.sendmoney.modules.transactions.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val repository: TransactionsRepository = TransactionsRepositoryImpl()
) : BaseViewModel() {
    
    private val _transactionsState = MutableStateFlow<ResultState<TransactionsResponse>>(ResultState.Idle)
    val transactionsState: StateFlow<ResultState<TransactionsResponse>> = _transactionsState.asStateFlow()
    
    private val _savedRequestsState = MutableStateFlow<ResultState<SavedRequestsResponse>>(ResultState.Idle)
    val savedRequestsState: StateFlow<ResultState<SavedRequestsResponse>> = _savedRequestsState.asStateFlow()
    
    private val _selectedRequestState = MutableStateFlow<ResultState<SavedRequest>>(ResultState.Idle)
    val selectedRequestState: StateFlow<ResultState<SavedRequest>> = _selectedRequestState.asStateFlow()
    
    init {
        loadTransactions()
        loadSavedRequests()
    }
    
    fun loadTransactions() {
        viewModelScope.launch {
            repository.getTransactions().collect { result ->
                _transactionsState.value = result
                setLoading(result is ResultState.Loading)
                
                if (result is ResultState.Error) {
                    setError(result.message)
                } else {
                    clearError()
                }
            }
        }
    }
    
    fun refreshTransactions() {
        loadTransactions()
    }
    
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction).collect { result ->
                if (result is ResultState.Success) {
                    // Refresh the list after adding
                    loadTransactions()
                }
            }
        }
    }
    
    fun loadSavedRequests() {
        viewModelScope.launch {
            repository.getSavedRequests().collect { result ->
                _savedRequestsState.value = result
                setLoading(result is ResultState.Loading)
                
                if (result is ResultState.Error) {
                    setError(result.message)
                } else {
                    clearError()
                }
            }
        }
    }
    
    fun refreshSavedRequests() {
        loadSavedRequests()
    }
    
    fun getRequestDetails(requestId: String) {
        viewModelScope.launch {
            repository.getRequestById(requestId).collect { result ->
                _selectedRequestState.value = result
                
                if (result is ResultState.Error) {
                    setError(result.message)
                } else {
                    clearError()
                }
            }
        }
    }
    
    fun clearSelectedRequest() {
        _selectedRequestState.value = ResultState.Idle
    }
}