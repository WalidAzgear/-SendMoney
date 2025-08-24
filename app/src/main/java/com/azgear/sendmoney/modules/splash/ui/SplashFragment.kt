package com.azgear.sendmoney.modules.splash.ui

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.azgear.sendmoney.R
import com.azgear.sendmoney.core.base.BaseFragment
import com.azgear.sendmoney.databinding.FragmentSplashBinding
import com.azgear.sendmoney.modules.splash.viewmodel.SplashViewModel

class SplashFragment : BaseFragment<FragmentSplashBinding, SplashViewModel>() {
    
    override val layoutId: Int = R.layout.fragment_splash
    override val viewModel: SplashViewModel by viewModels()
    
    override fun setupUI() {
        binding.viewModel = viewModel
        
        // Start loading animation
        startLoadingAnimation()
    }
    
    override fun observeViewModel() {
        // Observe navigation trigger
        collectLatestLifecycleFlow(viewModel.navigateToLogin) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigate(R.id.action_splash_to_login)
                viewModel.onNavigationHandled()
            }
        }
        
        // Observe animation progress for any additional progress updates
        collectLatestLifecycleFlow(viewModel.animationProgress) { progress ->
        }
    }
    
    private fun startLoadingAnimation() {
        // Simple fade in animation for app elements
        binding.appLogo.animate()
            .alpha(1f)
            .setDuration(1000)
            .start()
            
        binding.appTitle.animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(500)
            .start()
            
        binding.loadingIndicator.animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(1000)
            .start()
    }
} 