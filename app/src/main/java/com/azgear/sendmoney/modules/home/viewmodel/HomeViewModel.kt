package com.azgear.sendmoney.modules.home.viewmodel

import androidx.lifecycle.viewModelScope
import com.azgear.sendmoney.core.base.BaseViewModel
import com.azgear.sendmoney.core.utils.ResultState
import com.azgear.sendmoney.modules.home.data.BalanceResponse
import com.azgear.sendmoney.modules.home.data.HomeRepository
import com.azgear.sendmoney.modules.home.data.HomeRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository = HomeRepositoryImpl()
) : BaseViewModel() {
    
    private val _balanceState = MutableStateFlow<ResultState<BalanceResponse>>(ResultState.Idle)
    val balanceState: StateFlow<ResultState<BalanceResponse>> = _balanceState.asStateFlow()
    
    init {
        loadBalance()
    }
    
    fun loadBalance() {
        viewModelScope.launch {
            repository.getBalance().collect { result ->
                _balanceState.value = result
                setLoading(result is ResultState.Loading)
                
                if (result is ResultState.Error) {
                    setError(result.message)
                } else {
                    clearError()
                }
            }
        }
    }
    
    fun refreshBalance() {
        loadBalance()
    }
}