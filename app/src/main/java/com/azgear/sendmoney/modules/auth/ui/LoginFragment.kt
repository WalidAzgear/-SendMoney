package com.azgear.sendmoney.modules.auth.ui

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.azgear.sendmoney.R
import com.azgear.sendmoney.core.base.BaseFragment
import com.azgear.sendmoney.core.extensions.gone
import com.azgear.sendmoney.core.extensions.visible
import com.azgear.sendmoney.core.utils.onError
import com.azgear.sendmoney.core.utils.onLoading
import com.azgear.sendmoney.core.utils.onSuccess
import com.azgear.sendmoney.databinding.FragmentLoginBinding
import com.azgear.sendmoney.modules.auth.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar

class LoginFragment : BaseFragment<FragmentLoginBinding, LoginViewModel>() {
    
    override val layoutId: Int = R.layout.fragment_login
    override val viewModel: LoginViewModel by viewModels()
    
    override fun setupUI() {
        binding.viewModel = viewModel
        
        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            
            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }
        
        // Handle back button click
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    override fun observeViewModel() {
        // Observe login state
        collectLatestLifecycleFlow(viewModel.loginState) { state ->
            state
                .onLoading {
                    binding.signInButton.isEnabled = false
                    binding.progressBar.visible()
                }
                .onSuccess { success ->
                    binding.signInButton.isEnabled = true
                    binding.progressBar.gone()
                    if (success) {
                        // Navigate to main app
                        findNavController().navigate(R.id.action_login_to_main)
                    }
                }
                .onError { message, _ ->
                    binding.signInButton.isEnabled = true
                    binding.progressBar.gone()
                    showErrorMessage(message)
                }
        }
        
        // Observe global loading state for additional UI updates if needed
        collectLatestLifecycleFlow(viewModel.isLoading) { isLoading ->
            // Additional loading handling if needed
        }
        
        // Observe global error state
        collectLatestLifecycleFlow(viewModel.error) { error ->
            error?.let {
                // Handle global errors if needed
            }
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        
        if (email.isEmpty()) {
            binding.emailInputLayout.error = getString(R.string.email_required)
            isValid = false
        } else {
            binding.emailInputLayout.error = null
        }
        
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = getString(R.string.password_required)
            isValid = false
        } else {
            binding.passwordInputLayout.error = null
        }
        
        return isValid
    }
    
    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
} 