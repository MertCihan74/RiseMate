package com.parthenios.risemate.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    
    private const val PREFS_NAME = "locale_prefs"
    private const val LANGUAGE_KEY = "language"
    private const val DEFAULT_LANGUAGE = "tr" // Turkish as default
    
    /**
     * Supported languages with their display names
     */
    fun getSupportedLanguages(): List<Pair<String, String>> {
        return listOf(
            "tr" to "Türkçe",
            "en" to "English",
            "de" to "Deutsch",
            "fr" to "Français",
            "es" to "Español"
        )
    }
    
    /**
     * Get current language code
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
    
    /**
     * Set language and save preference
     */
    fun setLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, language).apply()
    }
    
    /**
     * Get display name of current language
     */
    fun getLanguageDisplayName(context: Context): String {
        val currentLanguage = getLanguage(context)
        return getSupportedLanguages().find { it.first == currentLanguage }?.second ?: "Türkçe"
    }
    
    /**
     * Update base context with selected locale
     * Call this in attachBaseContext of Activities
     */
    fun updateBaseContextLocale(context: Context): Context {
        val language = getLanguage(context)
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
    
    /**
     * Apply locale to configuration
     */
    fun applyLocale(context: Context): Context {
        return updateBaseContextLocale(context)
    }
}

