package com.azgear.sendmoney.modules.sendmoney.data

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Root model for the entire service configuration
 */
@Parcelize
data class SendMoneyConfig(
    @SerializedName("title")
    val title: LocalizedText,
    @SerializedName("services")
    val services: List<Service>
) : Parcelable

/**
 * Service model representing different transfer types (Bank, Wallet, etc.)
 */
@Parcelize
data class Service(
    @SerializedName("label")
    val label: LocalizedText,
    @SerializedName("name")
    val name: String,
    @SerializedName("providers")
    val providers: List<Provider>
) : Parcelable

/**
 * Provider model representing specific service providers
 */
@Parcelize
data class Provider(
    @SerializedName("name")
    val name: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("required_fields")
    val requiredFields: List<FormField>
) : Parcelable

/**
 * Form field model for dynamic form generation
 */
@Parcelize
data class FormField(
    @SerializedName("label")
    val label: LocalizedText,
    @SerializedName("name")
    val name: String,
    @SerializedName("placeholder")
    val placeholder: @RawValue Any? = null, // Can be String or LocalizedText
    @SerializedName("type")
    val type: String,
    @SerializedName("validation")
    val validation: String? = null,
    @SerializedName("max_length")
    val maxLength: @RawValue Any? = null, // Can be Int or String
    @SerializedName("validation_error_message")
    val validationErrorMessage: @RawValue Any? = null, // Can be String or LocalizedText
    @SerializedName("options")
    val options: List<Option>? = null
) : Parcelable {
    
    /**
     * Get localized label based on current language
     */
    fun getLocalizedLabel(isArabic: Boolean = false): String {
        return if (isArabic && label.ar.isNotEmpty()) label.ar else label.en
    }
    
    /**
     * Get localized placeholder based on current language
     */
    fun getLocalizedPlaceholder(isArabic: Boolean = false): String? {
        return when (placeholder) {
            is String -> placeholder
            is LocalizedText -> if (isArabic && placeholder.ar.isNotEmpty()) placeholder.ar else placeholder.en
            else -> null
        }
    }
    
    /**
     * Get localized error message based on current language
     */
    fun getLocalizedErrorMessage(isArabic: Boolean = false): String? {
        return when (validationErrorMessage) {
            is String -> validationErrorMessage
            is LocalizedText -> if (isArabic && validationErrorMessage.ar.isNotEmpty()) validationErrorMessage.ar else validationErrorMessage.en
            else -> null
        }
    }
    
    /**
     * Get max length as integer
     */
    fun getMaxLengthInt(): Int? {
        return when (maxLength) {
            is Int -> maxLength
            is String -> maxLength.toIntOrNull()
            else -> null
        }
    }
}

/**
 * Option model for dropdown/selection fields
 */
@Parcelize
data class Option(
    @SerializedName("label")
    val label: String,
    @SerializedName("name")
    val name: String
) : Parcelable

/**
 * Localized text model supporting English and Arabic
 */
@Parcelize
data class LocalizedText(
    @SerializedName("en")
    val en: String,
    @SerializedName("ar")
    val ar: String = ""
) : Parcelable {
    fun get(isArabic: Boolean = false): String {
        return if (isArabic && ar.isNotEmpty()) ar else en
    }
}


/**
 * Form data model for storing user input
 */
@Parcelize
data class FormData(
    val serviceId: String,
    val providerId: String,
    val fields: MutableMap<String, String> = mutableMapOf()
) : Parcelable {
    fun setField(fieldName: String, value: String) {
        fields[fieldName] = value
    }
    
    fun getField(fieldName: String): String? {
        return fields[fieldName]
    }
}

/**
 * Validation result model
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) 