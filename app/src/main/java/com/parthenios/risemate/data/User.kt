package com.parthenios.risemate.data

import java.io.Serializable
import java.util.Date

data class User(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val birthDate: Date? = null,
    val age: Int = 0,
    val city: String = "",
    val knownSkills: List<String> = emptyList(),
    val wantedSkills: List<String> = emptyList(),
    val verifiedSkills: List<String> = emptyList(), // Quiz ile doğrulanmış yetenekler
    val createdAt: Long = System.currentTimeMillis(),
    val birthday: String = "", // String format for easier handling
    val publicKey: String = "", // Cihazın Keystore public key'i (Base64-encoded X.509)
    val photoUrl: String = "", // Profil fotoğrafı (Base64 data URL formatında: data:image/jpeg;base64,...)
    val blockedUsers: List<String> = emptyList(), // Engellenen kullanıcı ID'leri
    val isOnline: Boolean = false, // Çevrimiçi durumu
    val lastSeen: Long = 0, // Son görülme zamanı (timestamp)
    // Premium özellikleri
    val isPremium: Boolean = false,
    val premiumType: String = "", // "monthly", "yearly", "lifetime"
    val premiumStartDate: Long = 0,
    val premiumEndDate: Long = 0, // 0 = lifetime
    val purchaseToken: String = "" // Google Play purchase token for verification
) : Serializable
