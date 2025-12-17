package com.parthenios.risemate.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.parthenios.risemate.data.User

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString("current_user", userJson).apply()
    }

    fun getUser(): User? {
        val userJson = prefs.getString("current_user", null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun clearUser() {
        prefs.edit().remove("current_user").apply()
    }

    fun isUserLoggedIn(): Boolean {
        return getUser() != null
    }
    
    // Bildirim ayarları
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }
    
    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean("notifications_enabled", true) // Varsayılan: açık
    }
    
    fun setNotificationSound(enabled: Boolean) {
        prefs.edit().putBoolean("notification_sound", enabled).apply()
    }
    
    fun isNotificationSoundEnabled(): Boolean {
        return prefs.getBoolean("notification_sound", true) // Varsayılan: açık
    }
    
    fun setNotificationVibration(enabled: Boolean) {
        prefs.edit().putBoolean("notification_vibration", enabled).apply()
    }
    
    fun isNotificationVibrationEnabled(): Boolean {
        return prefs.getBoolean("notification_vibration", true) // Varsayılan: açık
    }
    
    // Gizlilik ayarları
    fun setOnlineStatusHidden(hidden: Boolean) {
        prefs.edit().putBoolean("hide_online_status", hidden).apply()
    }
    
    fun isOnlineStatusHidden(): Boolean {
        return prefs.getBoolean("hide_online_status", false) // Varsayılan: görünür
    }
    
    fun setLastSeenHidden(hidden: Boolean) {
        prefs.edit().putBoolean("hide_last_seen", hidden).apply()
    }
    
    fun isLastSeenHidden(): Boolean {
        return prefs.getBoolean("hide_last_seen", false) // Varsayılan: görünür
    }
    
    // Dil ayarları
    fun setLanguage(language: String) {
        prefs.edit().putString("app_language", language).apply()
    }
    
    fun getLanguage(): String {
        return prefs.getString("app_language", "tr") ?: "tr" // Varsayılan: Türkçe
    }
}



