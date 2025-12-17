package com.parthenios.risemate.data

/**
 * Eşleşme filtresi - Keşfet ekranında kullanıcıları filtrelemek için
 */
data class MatchFilter(
    // Şehir filtresi - null ise tüm şehirler
    val city: String? = null,
    
    // Yaş aralığı filtresi
    val minAge: Int? = null,
    val maxAge: Int? = null,
    
    // Birlikte Öğrenelim modu
    // true: Sadece öğrenmek istedikleri yeteneklere göre eşleşme
    // false: Normal eşleşme (karşılıklı yetenek değişimi)
    val learnTogetherMode: Boolean = false
) {
    companion object {
        // Varsayılan filtre - hiçbir kısıtlama yok
        val DEFAULT = MatchFilter()
        
        // Yaş aralığı seçenekleri
        val AGE_RANGES = listOf(
            null to null to "Tümü",
            18 to 25 to "18-25",
            26 to 35 to "26-35",
            36 to 45 to "36-45",
            46 to 55 to "46-55",
            56 to null to "56+"
        )
    }
    
    /**
     * Filtrenin aktif olup olmadığını kontrol eder
     */
    fun isActive(): Boolean {
        return city != null || minAge != null || maxAge != null || learnTogetherMode
    }
    
    /**
     * Aktif filtre sayısını döndürür
     */
    fun activeFilterCount(): Int {
        var count = 0
        if (city != null) count++
        if (minAge != null || maxAge != null) count++
        if (learnTogetherMode) count++
        return count
    }
    
    /**
     * Filtre özetini döndürür
     */
    fun getSummary(): String {
        val parts = mutableListOf<String>()
        
        city?.let { parts.add(it) }
        
        when {
            minAge != null && maxAge != null -> parts.add("$minAge-$maxAge yaş")
            minAge != null -> parts.add("$minAge+ yaş")
            maxAge != null -> parts.add("$maxAge yaşa kadar")
        }
        
        if (learnTogetherMode) {
            parts.add("Birlikte Öğrenelim")
        }
        
        return if (parts.isEmpty()) "Filtre yok" else parts.joinToString(" • ")
    }
}

