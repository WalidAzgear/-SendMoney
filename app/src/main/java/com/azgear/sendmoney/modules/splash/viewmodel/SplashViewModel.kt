package com.azgear.sendmoney.modules.splash.viewmodel

import com.azgear.sendmoney.core.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SplashViewModel : BaseViewModel() {
    
    private val splashDelay = 3000L // 3 seconds
    
    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()
    
    private val _animationProgress = MutableStateFlow(0f)
    val animationProgress: StateFlow<Float> = _animationProgress.asStateFlow()
    
    init {
        startSplashSequence()
    }
    
    private fun startSplashSequence() {
        launch {
            // Simulate loading progress
            for (i in 0..100 step 5) {
                _animationProgress.value = i / 100f
                delay(splashDelay / 20) // Update progress every 150ms
            }
            
            // Navigate to login after delay
            _navigateToLogin.value = true
        }
    }
    
    fun onNavigationHandled() {
        _navigateToLogin.value = false
    }
} 