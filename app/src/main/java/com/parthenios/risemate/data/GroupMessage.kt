package com.parthenios.risemate.data

import java.io.Serializable
import java.util.Date

/**
 * Grup mesajlarını temsil eden data class
 */
data class GroupMessage(
    val id: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val message: String = "",
    val imageUrl: String = "",        // Fotoğraf mesajı için
    val messageType: String = "text", // "text" veya "image"
    val timestamp: Date? = null,
    val isDeleted: Boolean = false
) : Serializable


