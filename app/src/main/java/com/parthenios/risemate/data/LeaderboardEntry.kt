package com.parthenios.risemate.data

/**
 * Liderlik tablosu girişi
 */
data class LeaderboardEntry(
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val acceptedAnswerCount: Int = 0,
    val rank: Int = 0
)


