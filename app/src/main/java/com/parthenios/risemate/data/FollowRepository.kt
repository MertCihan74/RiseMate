package com.parthenios.risemate.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FollowRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Kullanıcıyı takip et
    suspend fun followUser(followerId: String, followingId: String): Result<Unit> {
        return try {
            if (followerId == followingId) {
                return Result.failure(Exception("Kendinizi takip edemezsiniz"))
            }

            // Zaten takip ediyor mu kontrol et
            val existingFollow = firestore.collection("follows")
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get()
                .await()

            if (!existingFollow.isEmpty) {
                return Result.failure(Exception("Bu kullanıcıyı zaten takip ediyorsunuz"))
            }

            // Takip ilişkisini oluştur
            val follow = Follow(
                followerId = followerId,
                followingId = followingId
            )

            firestore.collection("follows").add(follow).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Takibi bırak
    suspend fun unfollowUser(followerId: String, followingId: String): Result<Unit> {
        return try {
            val followDoc = firestore.collection("follows")
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get()
                .await()

            if (followDoc.isEmpty) {
                return Result.failure(Exception("Bu kullanıcıyı takip etmiyorsunuz"))
            }

            // Takip ilişkisini sil
            followDoc.documents.first().reference.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Takip ediyor mu kontrol et
    suspend fun isFollowing(followerId: String, followingId: String): Boolean {
        return try {
            val followDoc = firestore.collection("follows")
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get()
                .await()

            !followDoc.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // Takipçi sayısını al
    suspend fun getFollowersCount(userId: String): Int {
        return try {
            val followers = firestore.collection("follows")
                .whereEqualTo("followingId", userId)
                .get()
                .await()

            followers.size()
        } catch (e: Exception) {
            0
        }
    }

    // Takip edilen sayısı
    suspend fun getFollowingCount(userId: String): Int {
        return try {
            val following = firestore.collection("follows")
                .whereEqualTo("followerId", userId)
                .get()
                .await()

            following.size()
        } catch (e: Exception) {
            0
        }
    }

    // Takipçileri al
    suspend fun getFollowers(userId: String): Result<List<String>> {
        return try {
            val followers = firestore.collection("follows")
                .whereEqualTo("followingId", userId)
                .get()
                .await()

            val followerIds = followers.documents.mapNotNull { it.getString("followerId") }
            Result.success(followerIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Takip ettiklerini al
    suspend fun getFollowing(userId: String): Result<List<String>> {
        return try {
            val following = firestore.collection("follows")
                .whereEqualTo("followerId", userId)
                .get()
                .await()

            val followingIds = following.documents.mapNotNull { it.getString("followingId") }
            Result.success(followingIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




