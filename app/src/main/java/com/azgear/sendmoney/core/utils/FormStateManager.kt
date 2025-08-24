package com.azgear.sendmoney.core.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FormState(
    val serviceId: String = "",
    val providerId: String = "",
    val formData: Map<String, String> = emptyMap(),
    val validationErrors: Map<String, String> = emptyMap(),
    val isValid: Boolean = false,
    val isSubmitting: Boolean = false
)

class FormStateManager {
    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()
    
    fun updateServiceAndProvider(serviceId: String, providerId: String) {
        _formState.value = _formState.value.copy(
            serviceId = serviceId,
            providerId = providerId,
            formData = emptyMap(), // Clear form data when service/provider changes
            validationErrors = emptyMap()
        )
    }
    
    fun updateFieldValue(fieldName: String, value: String) {
        val currentState = _formState.value
        val updatedFormData = currentState.formData.toMutableMap()
        updatedFormData[fieldName] = value
        
        _formState.value = currentState.copy(
            formData = updatedFormData,
            isValid = calculateIsValid(updatedFormData, currentState.validationErrors)
        )
    }
    
    fun updateFieldError(fieldName: String, error: String?) {
        val currentState = _formState.value
        val updatedErrors = currentState.validationErrors.toMutableMap()
        
        if (error.isNullOrEmpty()) {
            updatedErrors.remove(fieldName)
        } else {
            updatedErrors[fieldName] = error
        }
        
        _formState.value = currentState.copy(
            validationErrors = updatedErrors,
            isValid = calculateIsValid(currentState.formData, updatedErrors)
        )
    }
    
    fun setSubmitting(isSubmitting: Boolean) {
        _formState.value = _formState.value.copy(isSubmitting = isSubmitting)
    }
    
    fun clearForm() {
        _formState.value = FormState()
    }

    private fun calculateIsValid(formData: Map<String, String>, errors: Map<String, String>): Boolean {
        return errors.isEmpty() && formData.isNotEmpty()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: FormStateManager? = null
        
        fun getInstance(): FormStateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FormStateManager().also { INSTANCE = it }
            }
        }
    }
} 