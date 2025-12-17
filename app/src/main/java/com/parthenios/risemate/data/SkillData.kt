package com.parthenios.risemate.data

/**
 * Önceden tanımlı yetenekler listesi
 * Her yetenek bir emoji ile birlikte gelir
 */
data class Skill(
    val name: String,
    val emoji: String
) {
    val displayName: String
        get() = "$emoji $name"
}

/**
 * Kategori başlığı veya yetenek item'ı için sealed class
 */
sealed class SkillItem {
    data class CategoryHeader(val title: String) : SkillItem()
    data class Skill(val skill: com.parthenios.risemate.data.Skill) : SkillItem()
}

/**
 * Kategori bilgisi
 */
data class SkillCategory(
    val title: String,
    val skills: List<Skill>
)

object SkillData {
    // Kategorilere göre yetenekler
    val skillCategories = listOf(
        SkillCategory(
            "💻 Yazılım Dilleri",
            listOf(
                Skill("Python", "🐍"),
                Skill("Java", "☕"),
                Skill("JavaScript", "📜"),
                Skill("TypeScript", "📘"),
                Skill("C++", "⚙️"),
                Skill("C#", "🔷"),
                Skill("Swift", "🐦"),
                Skill("Kotlin", "🟣"),
                Skill("Dart", "🎯"),
                Skill("Go", "🐹"),
                Skill("HTML", "🌐"),
                Skill("CSS", "🎨"),
                Skill("React", "⚛️"),
                Skill("Node.js", "🟢"),
                Skill("Frontend", "🧩"),
                Skill("Backend", "🛠️"),
                Skill("Fullstack", "🧠")
            )
        ),
        SkillCategory(
            "📱 Mobil Geliştirme",
            listOf(
                Skill("Android", "🤖"),
                Skill("iOS", "🍎"),
                Skill("Flutter", "💙"),
                Skill("React Native", "⚛️")
            )
        ),
        SkillCategory(
            "🎵 Müzik",
            listOf(
                Skill("Gitar", "🎸"),
                Skill("Piyano", "🎹"),
                Skill("Keman", "🎻"),
                Skill("Bateri", "🥁"),
                Skill("Şan", "🎤"),
                Skill("Müzik Prodüksiyon", "🎧")
            )
        ),
        SkillCategory(
            "⚽ Spor",
            listOf(
                Skill("Futbol", "⚽"),
                Skill("Basketbol", "🏀"),
                Skill("Fitness", "💪"),
                Skill("Yoga", "🧘"),
                Skill("Boks", "🥊"),
                Skill("Dövüş Sanatları", "🥋"),
                Skill("Koşu", "🏃"),
                Skill("Yüzme", "🏊"),
                Skill("Tenis", "🎾"),
                Skill("Voleybol", "🏐"),
                Skill("Bisiklet", "🚴"),
                Skill("Pilates", "🧘")
            )
        ),
        SkillCategory(
            "🎨 Sanat",
            listOf(
                Skill("Resim", "🖼️"),
                Skill("Fotoğrafçılık", "📷"),
                Skill("Video Edit", "🎬"),
                Skill("Grafik Tasarım", "🎨")
            )
        ),
        SkillCategory(
            "🗣️ Diller",
            listOf(
                Skill("İngilizce", "🇬🇧"),
                Skill("Almanca", "🇩🇪"),
                Skill("Fransızca", "🇫🇷"),
                Skill("İspanyolca", "🇪🇸"),
                Skill("Arapça", "🇸🇦"),
                Skill("İtalyanca", "🇮🇹"),
                Skill("Japonca", "🇯🇵"),
                Skill("Korece", "🇰🇷")
            )
        ),
        SkillCategory(
            "📚 Akademik",
            listOf(
                Skill("Matematik", "📐"),
                Skill("Fizik", "⚛️"),
                Skill("Kimya", "🧪"),
                Skill("Biyoloji", "🔬"),
                Skill("Tarih", "📜"),
                Skill("Edebiyat", "📖"),
                Skill("Psikoloji", "🧠"),
                Skill("Ekonomi", "💰"),
                Skill("Mühendislik", "⚙️"),
                Skill("Tıp", "⚕️"),
                Skill("Hukuk", "⚖️")
            )
        )
    )
    
    /**
     * Tüm yetenekleri kategorileriyle birlikte düz liste olarak döndürür
     * Header'lar ve skill'ler birlikte
     * Kategoriler alfabetik olarak sıralanır
     */
    fun getAllSkillsWithCategories(): List<SkillItem> {
        val result = mutableListOf<SkillItem>()
        
        // Kategorileri alfabetik olarak sırala (emoji'yi göz ardı ederek)
        val sortedCategories = skillCategories.sortedBy { category ->
            // Emoji'yi kaldır ve sadece metni al
            category.title.replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim()
        }
        
        sortedCategories.forEach { category ->
            result.add(SkillItem.CategoryHeader(category.title))
            category.skills.forEach { skill ->
                result.add(SkillItem.Skill(skill))
            }
        }
        return result
    }
    
    /**
     * Tüm yetenekleri düz liste olarak döndürür (kategoriler olmadan)
     */
    val predefinedSkills: List<Skill>
        get() = skillCategories.flatMap { it.skills }
    
    /**
     * Yetenek adına göre Skill bulur
     */
    fun findSkillByName(name: String): Skill? {
        return predefinedSkills.find { 
            it.name.equals(name, ignoreCase = true) 
        }
    }
    
    /**
     * Emoji olmadan sadece yetenek adlarını döndürür
     */
    fun getSkillNames(): List<String> {
        return predefinedSkills.map { it.name }
    }
}

