package com.parthenios.risemate.data

import java.io.Serializable
import java.util.Date

/**
 * Forum sorusu data class'ı
 */
data class Question(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val title: String = "",
    val content: String = "",
    val skill: String = "",           // İlgili yetenek (Python, Gitar vs.)
    val category: String = "",        // Yetenek kategorisi (Programlama, Müzik vs.)
    val imageUrl: String = "",        // Opsiyonel görsel
    val answerCount: Int = 0,
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val isResolved: Boolean = false,  // Cevaplandı mı?
    val acceptedAnswerId: String = "", // Kabul edilen cevap
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isDeleted: Boolean = false
) : Serializable

/**
 * Soru cevabı data class'ı
 */
data class Answer(
    val id: String = "",
    val questionId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val isAccepted: Boolean = false,  // Soru sahibi tarafından kabul edildi mi?
    val createdAt: Date = Date(),
    val isDeleted: Boolean = false
) : Serializable


