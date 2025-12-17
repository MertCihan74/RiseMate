package com.parthenios.risemate.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Follow(
    val id: String = "",
    val followerId: String = "", // Takip eden kişi
    val followingId: String = "", // Takip edilen kişi
    @ServerTimestamp
    val timestamp: Date? = null
)




