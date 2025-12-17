package com.parthenios.risemate.data

import java.io.Serializable

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: MutableList<String> = mutableListOf(), // Beğenen kullanıcı ID'leri
    val likeCount: Int = 0,
    val comments: MutableList<Comment> = mutableListOf()
) : Serializable

data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val parentId: String? = null, // Yanıt verilen yorumun ID'si (null ise ana yorum)
    val likes: MutableList<String> = mutableListOf(), // Beğenen kullanıcı ID'leri
    val likeCount: Int = 0,
    val replies: MutableList<Comment> = mutableListOf() // Alt yanıtlar
) : Serializable
