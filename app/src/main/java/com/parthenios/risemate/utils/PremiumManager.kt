package com.parthenios.risemate.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Premium durumunu yöneten helper sınıf
 */
object PremiumManager {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Kullanıcının premium olup olmadığını kontrol eder
     * Hem local hem Firebase'den kontrol yapar
     */
    suspend fun isPremium(context: Context): Boolean {
        val userPreferences = UserPreferences(context)
        val localUser = userPreferences.getUser()
        
        // Önce local kontrol
        if (localUser?.isPremium == true) {
            // Premium süresi dolmuş mu kontrol et
            if (localUser.premiumEndDate > 0 && localUser.premiumEndDate < System.currentTimeMillis()) {
                // Süresi dolmuş, Firebase'den güncelle
                return checkAndUpdateFromFirebase(context, userPreferences)
            }
            return true
        }
        
        // Local'de premium değilse Firebase'den kontrol et
        return checkAndUpdateFromFirebase(context, userPreferences)
    }

    /**
     * Firebase'den premium durumunu kontrol eder ve local'i günceller
     */
    private suspend fun checkAndUpdateFromFirebase(context: Context, userPreferences: UserPreferences): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return false
        
        return try {
            val doc = firestore.collection("users").document(currentUser.uid).get().await()
            val isPremium = doc.getBoolean("isPremium") ?: false
            val premiumEndDate = doc.getLong("premiumEndDate") ?: 0
            
            // Premium süresi dolmuş mu?
            if (isPremium && premiumEndDate > 0 && premiumEndDate < System.currentTimeMillis()) {
                // Süresi dolmuş, premium'u kaldır
                firestore.collection("users").document(currentUser.uid)
                    .update(
                        mapOf(
                            "isPremium" to false,
                            "premiumType" to "",
                            "purchaseToken" to ""
                        )
                    ).await()
                
                // Local'i güncelle
                val localUser = userPreferences.getUser()
                if (localUser != null) {
                    userPreferences.saveUser(localUser.copy(
                        isPremium = false,
                        premiumType = "",
                        purchaseToken = ""
                    ))
                }
                return false
            }
            
            // Local'i güncelle
            if (isPremium) {
                val localUser = userPreferences.getUser()
                if (localUser != null && !localUser.isPremium) {
                    val premiumType = doc.getString("premiumType") ?: ""
                    val premiumStartDate = doc.getLong("premiumStartDate") ?: 0
                    val purchaseToken = doc.getString("purchaseToken") ?: ""
                    
                    userPreferences.saveUser(localUser.copy(
                        isPremium = true,
                        premiumType = premiumType,
                        premiumStartDate = premiumStartDate,
                        premiumEndDate = premiumEndDate,
                        purchaseToken = purchaseToken
                    ))
                }
            }
            
            isPremium
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Quick check - sadece local'den kontrol eder (sync olmadan)
     */
    fun isPremiumLocal(context: Context): Boolean {
        val userPreferences = UserPreferences(context)
        val localUser = userPreferences.getUser() ?: return false
        
        if (!localUser.isPremium) return false
        
        // Süre kontrolü
        if (localUser.premiumEndDate > 0 && localUser.premiumEndDate < System.currentTimeMillis()) {
            return false
        }
        
        return true
    }
}


