package com.azgear.sendmoney.core.utils

import com.azgear.sendmoney.modules.sendmoney.data.FormField
import com.azgear.sendmoney.modules.sendmoney.data.ValidationResult

object FormValidationEngine {
    
    fun validateField(field: FormField, value: String): ValidationResult {
        // Check if field is required and not empty
        if (value.isBlank()) {
            val errorMessage = field.getLocalizedErrorMessage(LanguageManager.isArabic())
            return ValidationResult(false, errorMessage ?: "This field is required")
        }
        
        // Validate based on field type
        when (field.type) {
            "number" -> {
                if (!validateNumber(value)) {
                    return ValidationResult(false, "Please enter a valid number")
                }
            }
            "msisdn" -> {
                if (!validatePhoneNumber(value)) {
                    return ValidationResult(false, "Please enter a valid phone number")
                }
            }
            "email" -> {
                if (!validateEmail(value)) {
                    return ValidationResult(false, "Please enter a valid email address")
                }
            }
            "date" -> {
                if (!validateDate(value)) {
                    return ValidationResult(false, "Please enter a valid date (YYYY-MM-DD)")
                }
            }
            "option" -> {
                if (!validateOption(value)) {
                    return ValidationResult(false, "Please select a valid option")
                }
            }
        }
        
        // Check max length
        val maxLength = field.getMaxLengthInt()
        if (maxLength != null && maxLength > 0 && value.length > maxLength) {
            return ValidationResult(false, "Maximum length is $maxLength characters")
        }
        
        // Check regex validation if specified
        field.validation?.let { regex ->
            if (regex.isNotEmpty() && !value.matches(Regex(regex))) {
                val errorMessage = field.getLocalizedErrorMessage(LanguageManager.isArabic())
                return ValidationResult(false, errorMessage ?: "Invalid format")
            }
        }
        
        return ValidationResult(true)
    }
    
    private fun validateNumber(value: String): Boolean {
        return try {
            value.toDoubleOrNull() != null
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    private fun validatePhoneNumber(value: String): Boolean {
        // Basic phone number validation - can be enhanced
        return value.matches(Regex("^\\+?[1-9][0-9]{6,14}$"))
    }
    
    private fun validateEmail(value: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()
    }
    
    private fun validateDate(value: String): Boolean {
        // Basic date validation for YYYY-MM-DD format
        return value.matches(Regex("^(?:19|20)\\d{2}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"))
    }
    
    private fun validateOption(value: String): Boolean {
        // Option fields should have a non-empty value (not the default "Select..." option)
        return value.isNotEmpty()
    }
} 