package com.azgear.sendmoney.modules.auth.viewmodel

import com.azgear.sendmoney.core.base.BaseViewModel
import com.azgear.sendmoney.core.utils.ResultState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : BaseViewModel() {
    
    // Mock credentials
    private val validUsername = "testuser"
    private val validPassword = "password123"
    
    private val _loginState = MutableStateFlow<ResultState<Boolean>>(ResultState.Idle)
    val loginState: StateFlow<ResultState<Boolean>> = _loginState.asStateFlow()
    
    fun login(email: String, password: String) {
        launch {
            _loginState.value = ResultState.Loading
            setLoading(true)
            
            try {
                // Simulate network call delay
                delay(1500)
                
                // Mock authentication logic
                val isValidCredentials = (email == validUsername || email == "testuser@example.com") 
                        && password == validPassword
                
                if (isValidCredentials) {
                    _loginState.value = ResultState.Success(true)
                    clearError()
                } else {
                    val errorMessage = "Invalid credentials."
                    _loginState.value = ResultState.Error(errorMessage)
                    setError(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = "Login failed: ${e.message}"
                _loginState.value = ResultState.Error(errorMessage, e)
                setError(errorMessage)
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun clearLoginState() {
        _loginState.value = ResultState.Idle
        clearError()
    }
} 