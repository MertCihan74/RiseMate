package com.parthenios.risemate.data

import java.io.Serializable
import java.util.Date

/**
 * Yetenek gruplarını temsil eden data class
 * Gruplar Firebase'de hazır olarak bulunur, kullanıcılar tarafından oluşturulmaz
 */
data class SkillGroup(
    val id: String = "",
    val name: String = "",           // Grup adı (örn: "Python Geliştiricileri")
    val description: String = "",    // Grup açıklaması
    val skill: String = "",          // İlişkili yetenek (örn: "Python") veya şehir adı
    val category: String = "",       // Kategori (örn: "Programlama")
    val groupType: String = "skill", // "skill", "city", "project" - Grup tipi
    val photoUrl: String = "",       // Grup fotoğrafı URL
    val memberCount: Int = 0,        // Üye sayısı
    val createdAt: Date? = null
) : Serializable

/**
 * Grup üyelerini temsil eden data class
 */
data class GroupMember(
    val id: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val skillType: String = "",      // "known" veya "wanted" - kullanıcının bu yeteneği bilip bilmediği
    val joinedAt: Date? = null
) : Serializable

/**
 * Grup kategorileri - SkillQuizData ile uyumlu
 */
object GroupCategories {
    val categories = listOf(
        "Tümü",
        "Programlama",
        "Mobil Geliştirme",
        "Web Geliştirme",
        "Veritabanı",
        "Tasarım",
        "Müzik",
        "Dil",
        "Yemek",
        "Spor",
        "Diğer"
    )
}
