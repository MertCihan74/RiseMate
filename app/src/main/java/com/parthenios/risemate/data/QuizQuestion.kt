package com.parthenios.risemate.data

import java.io.Serializable

/**
 * Quiz sorusu veri modeli
 */
data class QuizQuestion(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val skillName: String = ""
) : Serializable

/**
 * Quiz sonucu veri modeli
 */
data class QuizResult(
    val skillName: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 5,
    val correctAnswers: Int = 0,
    val passed: Boolean = false,
    val completedAt: Long = System.currentTimeMillis(),
    val attempts: Int = 1
) : Serializable







