package com.azgear.sendmoney.core.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.azgear.sendmoney.modules.sendmoney.data.LocalizedText

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    ARABIC("ar", "العربية")
}

object LanguageManager {
    private const val PREF_NAME = "language_preferences"
    private const val KEY_LANGUAGE = "selected_language"
    
    private lateinit var preferences: SharedPreferences
    private var isInitialized = false
    
    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()
    
    fun initialize(context: Context) {
        if (!isInitialized) {
            preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedLanguage = preferences.getString(KEY_LANGUAGE, Language.ENGLISH.code)
            _currentLanguage.value = Language.values().find { it.code == savedLanguage } ?: Language.ENGLISH
            isInitialized = true
        }
    }
    
    fun setLanguage(language: Language) {
        _currentLanguage.value = language
        if (isInitialized) {
            preferences.edit()
                .putString(KEY_LANGUAGE, language.code)
                .apply()
        }
    }
    
    fun getCurrentLanguageCode(): String = _currentLanguage.value.code
    
    fun isArabic(): Boolean = _currentLanguage.value == Language.ARABIC
    
    fun getLocalizedText(localizedText: Any?): String {
        return when (localizedText) {
            is String -> localizedText
            is LocalizedText -> localizedText.get(isArabic())
            is Map<*, *> -> {
                val textMap = localizedText as? Map<String, String>
                textMap?.get(getCurrentLanguageCode()) 
                    ?: textMap?.get("en") 
                    ?: ""
            }
            else -> ""
        }
    }
} 