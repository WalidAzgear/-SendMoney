package com.azgear.sendmoney.modules.sendmoney.ui

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.azgear.sendmoney.R
import com.azgear.sendmoney.core.base.BaseFragment
import com.azgear.sendmoney.core.extensions.gone
import com.azgear.sendmoney.core.extensions.visible
import com.azgear.sendmoney.core.utils.*
import com.azgear.sendmoney.databinding.FragmentSendMoneyBinding
import com.azgear.sendmoney.modules.sendmoney.data.*
import com.azgear.sendmoney.modules.sendmoney.viewmodel.SendMoneyViewModel
import com.google.android.material.snackbar.Snackbar
import android.widget.TextView

class SendMoneyFragment : BaseFragment<FragmentSendMoneyBinding, SendMoneyViewModel>() {
    
    override val layoutId: Int = R.layout.fragment_send_money
    override val viewModel: SendMoneyViewModel by viewModels()
    
    private lateinit var formBuilder: DynamicFormBuilder
    private var currentConfig: SendMoneyConfig? = null
    private var serviceAdapter: ArrayAdapter<String>? = null
    private var providerAdapter: ArrayAdapter<String>? = null
    
    override fun setupUI() {
        // Initialize LanguageManager
        LanguageManager.initialize(requireContext())
        
        // Initialize ViewModel with context
        viewModel.initialize(requireContext())
        
        // Initialize form builder
        formBuilder = DynamicFormBuilder(
            context = requireContext(),
            onFieldValueChanged = { fieldName, value ->
                viewModel.updateFieldValue(fieldName, value)
            },
            onValidationError = { fieldName, error ->
                viewModel.updateFieldError(fieldName, error)
                showFieldError(fieldName, error)
            }
        )
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Language toggle button
        binding.btnLanguageToggle.setOnClickListener {
            val currentLang = LanguageManager.currentLanguage.value
            val newLang = if (currentLang == Language.ENGLISH) Language.ARABIC else Language.ENGLISH
            viewModel.switchLanguage(newLang)
        }
        
        // Submit button
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                viewModel.submitDynamicForm()
            }
        }
        
        // Service spinner
        binding.spinnerService.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d("SendMoneyFragment", "Service selected at position: $position")
                if (position > 0) { // Skip first "Select service" item
                    val service = currentConfig?.services?.get(position - 1)
                    Log.d("SendMoneyFragment", "Selected service: ${service?.label}")
                    service?.let { 
                        viewModel.selectService(it)
                        Log.d("SendMoneyFragment", "Service has ${it.providers.size} providers")
                    }
                } else {
                    // Reset when "Select Service" is chosen
                    viewModel.selectService(null)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
        
        // Provider spinner
        binding.spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedService = viewModel.selectedService.value
                if (position > 0 && selectedService != null) { // Skip first "Select provider" item
                    val provider = selectedService.providers.get(position - 1)
                    viewModel.selectProvider(provider)
                } else {
                    // Reset provider selection
                    viewModel.selectProvider(null)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    override fun observeViewModel() {
        // Observe configuration loading
        collectLatestLifecycleFlow(viewModel.configState) { state ->
            state
                .onLoading {
                    binding.progressBar.visible()
                }
                .onSuccess { config ->
                    binding.progressBar.gone()
                    Log.d("SendMoneyFragment", "Configuration loaded successfully with ${config.services.size} services")
                    currentConfig = config
                    setupServiceSpinner(config)
                    updateTitle(config)
                }
                .onError { message, _ ->
                    binding.progressBar.gone()
                    showError("Failed to load configuration: $message")
                }
        }
        
        // Observe selected service
        collectLatestLifecycleFlow(viewModel.selectedService) { service ->
            if (service != null) {
                setupProviderSpinner(service)
                binding.tvProviderLabel.visible()
                binding.providerContainer.visible()
                
                // Clear the form when service is selected - don't show any fields until provider is selected
                binding.dynamicFormContainer.removeAllViews()
            } else {
                binding.tvProviderLabel.gone()
                binding.providerContainer.gone()
                binding.dynamicFormContainer.removeAllViews()
                
                // Reset provider spinner
                providerAdapter?.let { _ ->
                    binding.spinnerProvider.setSelection(0)
                }
            }
        }
        
        // Observe selected provider
        collectLatestLifecycleFlow(viewModel.selectedProvider) { provider ->
            val selectedService = viewModel.selectedService.value
            if (provider != null && selectedService != null) {
                // Build form with provider-specific fields only when provider is selected
                buildDynamicForm(provider)
            } else {
                // Clear form if no provider selected
                binding.dynamicFormContainer.removeAllViews()
            }
        }
        
        // Observe form state
        collectLatestLifecycleFlow(viewModel.formState) { state ->
            binding.btnSubmit.isEnabled = state.isValid && !state.isSubmitting
            
            if (state.isSubmitting) {
                binding.progressBar.visible()
            } else {
                binding.progressBar.gone()
            }
        }
        
        // Observe language changes
        collectLatestLifecycleFlow(viewModel.currentLanguage) { language ->
            updateLanguageUI(language)
        }
        
        // Observe send money result
        collectLatestLifecycleFlow(viewModel.sendMoneyState) { state ->
            state
                .onLoading {
                    binding.progressBar.visible()
                    binding.btnSubmit.isEnabled = false
                }
                .onSuccess { response ->
                    binding.progressBar.gone()
                    binding.btnSubmit.isEnabled = true
                    showSuccess("Form submitted successfully!")
                    
                    // Navigate to confirmation fragment
                    val action = SendMoneyFragmentDirections.actionSendMoneyToConfirmation(
                        recipientName = response.recipientName ?: "Unknown",
                        amount = (response.amount ?: 0.0).toFloat(),
                        note = null
                    )
                    findNavController().navigate(action)
                }
                .onError { message, _ ->
                    binding.progressBar.gone()
                    binding.btnSubmit.isEnabled = true
                    showError("Submission failed: $message")
                }
        }
    }
    
    private fun setupServiceSpinner(config: SendMoneyConfig) {
        val serviceNames = mutableListOf<String>()
        serviceNames.add("Select Service") // Default option
        
        config.services.forEach { service ->
            val serviceName = LanguageManager.getLocalizedText(service.label)
            Log.d("SendMoneyFragment", "Adding service: $serviceName from label: ${service.label}")
            serviceNames.add(serviceName)
        }
        
        Log.d("SendMoneyFragment", "Total services: ${serviceNames.size}, Services: $serviceNames")
        
        serviceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, serviceNames)
        serviceAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerService.adapter = serviceAdapter
    }
    
    private fun setupProviderSpinner(service: Service) {
        val providerNames = mutableListOf<String>()
        providerNames.add("Select Provider") // Default option
        
        service.providers.forEach { provider ->
            providerNames.add(provider.name)
        }
        
        providerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, providerNames)
        providerAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerProvider.adapter = providerAdapter
    }
    
    private fun buildDynamicForm(provider: Provider) {
        Log.d("SendMoneyFragment", "Building dynamic form for provider: ${provider.name}")
        
        val fields = provider.requiredFields
        formBuilder.buildFormFields(
            fields = fields,
            container = binding.dynamicFormContainer,
            initialData = viewModel.formState.value.formData
        )
    }
    
    private fun updateTitle(config: SendMoneyConfig) {
        binding.tvTitle.text = LanguageManager.getLocalizedText(config.title)
    }
    
    private fun updateLanguageUI(language: Language) {
        binding.btnLanguageToggle.text = if (language == Language.ENGLISH) "EN | AR" else "AR | EN"
        
        // Update title if config is available
        currentConfig?.let { updateTitle(it) }
        
        // Update service spinner
        currentConfig?.let { config ->
            setupServiceSpinner(config)
            // Preserve current selection
            val currentService = viewModel.selectedService.value
            if (currentService != null) {
                val serviceIndex = config.services.indexOf(currentService)
                if (serviceIndex >= 0) {
                    binding.spinnerService.setSelection(serviceIndex + 1) // +1 for "Select Service" option
                }
            }
        }
        
        // Update provider spinner
        viewModel.selectedService.value?.let { service ->
            setupProviderSpinner(service)
            // Preserve current provider selection
            val currentProvider = viewModel.selectedProvider.value
            if (currentProvider != null) {
                val providerIndex = service.providers.indexOf(currentProvider)
                if (providerIndex >= 0) {
                    binding.spinnerProvider.setSelection(providerIndex + 1) // +1 for "Select Provider" option
                }
            }
        }
        
        // Rebuild form with new language
        viewModel.selectedProvider.value?.let { provider ->
            buildDynamicForm(provider)
        } ?: run {
            // If no provider selected, clear the form
            binding.dynamicFormContainer.removeAllViews()
        }
    }
    
    private fun validateForm(): Boolean {
        val selectedProvider = viewModel.selectedProvider.value
        if (selectedProvider == null) {
            showError("Please select a provider first")
            return false
        }
        
        val fields = selectedProvider.requiredFields
        val formData = viewModel.formState.value.formData
        
        var isValid = true
        
        fields.forEach { field ->
            val value = formData[field.name] ?: ""
            val validationResult = FormValidationEngine.validateField(field, value)
            
            if (!validationResult.isValid) {
                isValid = false
                showFieldError(field.name, validationResult.errorMessage)
            } else {
                showFieldError(field.name, null)
            }
        }
        
        if (!isValid) {
            showError("Please fix the validation errors above")
        }
        
        return isValid
    }
    
    private fun showFieldError(fieldName: String, error: String?) {
        // Find the error TextView for this field
        val errorTextView = binding.dynamicFormContainer.findViewWithTag<TextView>("${fieldName}_error")
        
        if (errorTextView != null) {
            if (error != null) {
                errorTextView.text = error
                errorTextView.visibility = View.VISIBLE
            } else {
                errorTextView.text = ""
                errorTextView.visibility = View.GONE
            }
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}