package com.azgear.sendmoney.modules.sendmoney.viewmodel

import com.azgear.sendmoney.core.base.BaseViewModel
import com.azgear.sendmoney.core.utils.FormStateManager
import com.azgear.sendmoney.core.utils.LanguageManager
import com.azgear.sendmoney.core.utils.ResultState
import com.azgear.sendmoney.modules.sendmoney.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SendMoneyViewModel(
    private val repository: SendMoneyRepository? = null,
    private val formStateManager: FormStateManager = FormStateManager.getInstance()
) : BaseViewModel() {
    
    private lateinit var actualRepository: SendMoneyRepository
    
    // Dynamic form configuration
    private val _configState = MutableStateFlow<ResultState<SendMoneyConfig>>(ResultState.Idle)
    val configState: StateFlow<ResultState<SendMoneyConfig>> = _configState.asStateFlow()
    
    // Selected service and provider
    private val _selectedService = MutableStateFlow<Service?>(null)
    val selectedService: StateFlow<Service?> = _selectedService.asStateFlow()
    
    private val _selectedProvider = MutableStateFlow<Provider?>(null)
    val selectedProvider: StateFlow<Provider?> = _selectedProvider.asStateFlow()
    
    // Form state from FormStateManager
    val formState = formStateManager.formState
    
    // Send money result
    private val _sendMoneyState = MutableStateFlow<ResultState<SendMoneyResponse>>(ResultState.Idle)
    val sendMoneyState: StateFlow<ResultState<SendMoneyResponse>> = _sendMoneyState.asStateFlow()
    
    // Language state
    val currentLanguage = LanguageManager.currentLanguage
    
    fun initialize(context: android.content.Context) {
        actualRepository = repository ?: SendMoneyRepositoryImpl(context)
        loadSendMoneyConfig()
    }
    
    private fun loadSendMoneyConfig() {
        if (!::actualRepository.isInitialized) {
            _configState.value = ResultState.Error("Repository not initialized", null)
            return
        }
        
        launch {
            actualRepository.loadSendMoneyConfig().collect { result ->
                _configState.value = result
                setLoading(result is ResultState.Loading)
                
                if (result is ResultState.Error) {
                    setError(result.message)
                } else {
                    clearError()
                }
            }
        }
    }
    
    fun selectService(service: Service?) {
        _selectedService.value = service
        _selectedProvider.value = null // Clear provider when service changes
        if (service != null) {
            formStateManager.updateServiceAndProvider(service.name, "")
        } else {
            formStateManager.clearForm()
        }
    }
    
    fun selectProvider(provider: Provider?) {
        val service = _selectedService.value ?: return
        _selectedProvider.value = provider
        if (provider != null) {
            formStateManager.updateServiceAndProvider(service.name, provider.id)
        } else {
            formStateManager.updateServiceAndProvider(service.name, "")
        }
    }
    
    fun updateFieldValue(fieldName: String, value: String) {
        formStateManager.updateFieldValue(fieldName, value)
    }
    
    fun updateFieldError(fieldName: String, error: String?) {
        formStateManager.updateFieldError(fieldName, error)
    }
    
    fun switchLanguage(language: com.azgear.sendmoney.core.utils.Language) {
        LanguageManager.setLanguage(language)
    }
    
    fun getCurrentFormFields(): List<FormField> {
        return _selectedProvider.value?.requiredFields ?: emptyList()
    }
    
    fun submitDynamicForm() {
        val currentState = formState.value
        if (!currentState.isValid || currentState.isSubmitting) return
        
        val service = _selectedService.value ?: return
        val provider = _selectedProvider.value
        
        formStateManager.setSubmitting(true)
        
        launch {
            val dynamicFormData = FormData(
                serviceId = service.name,
                providerId = provider?.id ?: "",
                fields = currentState.formData.toMutableMap()
            )
            
            actualRepository.submitDynamicForm(dynamicFormData).collect { result ->
                _sendMoneyState.value = result
                formStateManager.setSubmitting(false)
                setLoading(result is ResultState.Loading)
                
                if (result is ResultState.Error) {
                    setError(result.message)
                } else {
                    clearError()
                }
            }
        }
    }
    
    fun getFormData(): FormData? {
        val currentState = formState.value
        val service = _selectedService.value ?: return null
        val provider = _selectedProvider.value
        
        return FormData(
            serviceId = service.name,
            providerId = provider?.id ?: "",
            fields = currentState.formData.toMutableMap()
        )
    }
    
    fun clearForm() {
        formStateManager.clearForm()
        _selectedService.value = null
        _selectedProvider.value = null
    }
}