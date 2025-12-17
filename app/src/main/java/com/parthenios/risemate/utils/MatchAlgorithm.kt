package com.parthenios.risemate.utils

import com.parthenios.risemate.data.MatchFilter
import com.parthenios.risemate.data.User

object MatchAlgorithm {
    
    // Ana kategoriler - aynı kategorideki yetenekler birbiriyle eşleşir
    private val skillCategories = mapOf(
        // Programlama Kategorisi
        "programlama" to listOf(
            "python", "java", "javascript", "typescript", "c", "c++", "c#", "php", "ruby", "go", "rust",
            "swift", "kotlin", "dart", "scala", "r", "matlab", "perl", "lua", "haskell", "clojure",
            "erlang", "elixir", "f#", "objective-c", "assembly", "cobol", "fortran", "pascal", "delphi",
            "visual basic", "vb.net", "powershell", "shell scripting", "bash scripting",
            "kodlama", "yazılım", "software", "development", "geliştirme", "programlama",
            "web geliştirme", "mobil geliştirme", "oyun geliştirme"
        ),
        // Web Teknolojileri Kategorisi
        "web" to listOf(
            "html", "css", "react", "vue", "angular", "nodejs", "express", "django", "flask", "laravel",
            "spring", "asp.net", "rails", "nextjs", "nuxt", "svelte", "bootstrap", "tailwind",
            "sass", "less", "stylus", "webpack", "vite", "gulp", "grunt", "npm", "yarn", "pnpm",
            "graphql", "rest api", "soap", "json", "xml", "jquery", "ajax", "websocket",
            "frontend", "backend", "fullstack"
        ),
        // Mobil Geliştirme Kategorisi
        "mobil" to listOf(
            "android", "ios", "react native", "flutter", "xamarin", "ionic", "cordova", "phonegap",
            "swiftui", "uikit", "jetpack compose", "android studio", "xcode",
            "uygulama", "app", "mobile"
        ),
        // Veritabanları Kategorisi
        "veritabani" to listOf(
            "sql", "mysql", "postgresql", "mongodb", "redis", "oracle", "sqlite", "mariadb",
            "cassandra", "couchdb", "neo4j", "elasticsearch", "dynamodb", "firebase", "supabase",
            "database", "db", "veritabanı"
        ),
        // Müzik Kategorisi
        "muzik" to listOf(
            "gitar", "piyano", "keman", "flüt", "saksafon", "bateri", "davul", "bas gitar", "elektro gitar",
            "akustik gitar", "klavye", "org", "şan", "vokal", "müzik prodüksiyon", "dj", "mix", "mastering",
            "vurmalı çalgılar", "yaylı çalgılar", "üflemeli çalgılar", "telli çalgılar", "arp", "mandolin",
            "banjo", "ukulele", "bağlama", "saz", "ud", "kanun", "ney", "zurna", "darbuka", "def",
            "müzik teorisi", "armoni", "kompozisyon", "orkestrasyon", "müzik notası", "solfej",
            "müzik", "enstrüman", "çalgı", "müzik aleti"
        ),
        // Dövüş Sanatları Kategorisi
        "dovus sanatlari" to listOf(
            "judo", "karate", "taekwondo", "aikido", "boks", "kick boks", "muay thai", "jiu jitsu",
            "güreş", "dövüş", "dövüş sanatları", "martial arts", "savunma sanatları", "boxing", "kickboxing"
        ),
        // Spor Kategorisi (Dövüş Sanatları hariç)
        "spor" to listOf(
            "futbol", "basketbol", "voleybol", "tenis", "masa tenisi", "yüzme", "koşu", "fitness", "crossfit",
            "pilates", "yoga", "dans", "bale", "jimnastik", "atletizm", "bisiklet", "kayak", "snowboard",
            "okçuluk", "binicilik", "golf", "bowling", "bilardo", "satranç", "go", "dama",
            "rafting", "tırmanış", "dağcılık", "kamp", "doğa yürüyüşü", "trekking", "hiking",
            "sörf", "dalış", "su kayağı", "yelken", "kürek", "kano",
            "spor", "egzersiz", "antrenman", "fiziksel aktivite"
        ),
        // Tasarım Kategorisi
        "tasarim" to listOf(
            "photoshop", "illustrator", "figma", "sketch", "adobe xd", "invision", "ui/ux", "grafik tasarım",
            "indesign", "premiere pro", "after effects", "lightroom", "coreldraw", "canva",
            "adobe animate", "blender", "cinema 4d", "maya", "3ds max", "zbrush", "substance painter",
            "tasarım", "design", "görsel", "grafik", "ui", "ux", "web tasarım"
        ),
        // Sanat Kategorisi
        "sanat" to listOf(
            "resim", "çizim", "karikatür", "graffiti", "heykel", "seramik", "cam işçiliği", "mozaik",
            "fotoğrafçılık", "video çekimi", "video edit", "animasyon", "3d modelleme", "mimari çizim",
            "suluboya", "akrilik", "yağlı boya", "pastel", "karakalem", "manga", "anime", "dijital sanat",
            "kaligrafi", "hat sanatı", "ebru", "tezhip", "minyatür", "fresk", "duvar resmi",
            "stop motion", "motion graphics", "vfx", "compositing", "color grading",
            "sanat", "görsel sanatlar", "fotoğraf", "video"
        ),
        // El Sanatları Kategorisi
        "el sanatlari" to listOf(
            "dikiş", "nakış", "örgü", "tığ işi", "takı yapımı", "ahşap işçiliği", "metal işçiliği",
            "deri işçiliği", "kuyumculuk", "cam işçiliği", "seramik", "çömlekçilik",
            "marangozluk", "doğramacılık", "oymacılık", "kakmacılık", "sedef işçiliği",
            "halı dokuma", "kilim dokuma", "kumaş dokuma", "battaniye örme", "eldiven örme",
            "çanta yapımı", "ayakkabı yapımı", "terzilik", "moda tasarımı", "aksesuar tasarımı",
            "el sanatları", "hobi", "craft"
        ),
        // Diller Kategorisi
        "diller" to listOf(
            "ingilizce", "almanca", "fransızca", "ispanyolca", "italyanca", "portekizce", "rusça",
            "çince", "japonca", "korece", "arapça", "farsça", "yunanca", "lehçe",
            "macarca", "romence", "bulgarca", "sırpça", "hırvatça", "boşnakça",
            "fince", "isveççe", "norveççe", "danca", "hollandaca", "flamanca", "belçika dili",
            "türkçe", "dil", "çeviri", "tercüme", "simultane çeviri"
        ),
        // Sağlık ve Wellness Kategorisi
        "saglik" to listOf(
            "meditasyon", "mindfulness", "nefes egzersizi", "masaj", "refleksoloji", "akupunktur",
            "ayurveda", "homeopati", "bitkisel tedavi", "beslenme", "diyetisyenlik", "fitness antrenörlüğü",
            "kişisel antrenörlük", "pilates eğitmenliği", "yoga eğitmenliği", "dans eğitmenliği",
            "fizyoterapi", "ergoterapi", "psikoterapi", "danışmanlık", "yaşam koçluğu",
            "sağlık", "wellness", "spiritual"
        ),
        // Yemek Kategorisi
        "yemek" to listOf(
            "yemek yapma", "mutfak", "pastacılık", "kahve yapımı", "barista",
            "türk mutfağı", "italyan mutfağı", "fransız mutfağı", "japon mutfağı", "çin mutfağı",
            "mexican mutfağı", "hint mutfağı", "vegan yemek", "vejetaryen yemek", "gluten-free yemek",
            "ekmek yapımı", "pasta yapımı", "çikolata yapımı", "dondurma yapımı", "şarap yapımı"
        )
    )
    
    // Yetenek eş anlamlıları (sadece gerçekten aynı şey olanlar)
    private val skillSynonyms = mapOf(
        "python" to listOf("python programlama", "python kodlama"),
        "java" to listOf("java programlama", "java kodlama"),
        "javascript" to listOf("js", "ecmascript"),
        "typescript" to listOf("ts"),
        "gitar" to listOf("elektro gitar", "akustik gitar", "bas gitar"),
        "piyano" to listOf("klavye", "org"),
        "bateri" to listOf("davul"),
        "fotoğrafçılık" to listOf("fotoğraf"),
        "fotoğraf" to listOf("fotoğrafçılık"),
        "resim" to listOf("çizim"),
        "çizim" to listOf("resim")
    )
    
    /**
     * Türkçe karakterleri normalize eder (karşılaştırma için)
     */
    private fun normalizeTurkish(text: String): String {
        return text.lowercase().trim()
            .replace("ı", "i")
            .replace("ğ", "g")
            .replace("ü", "u")
            .replace("ş", "s")
            .replace("ö", "o")
            .replace("ç", "c")
            .replace("İ", "i")
            .replace("Ğ", "g")
            .replace("Ü", "u")
            .replace("Ş", "s")
            .replace("Ö", "o")
            .replace("Ç", "c")
    }
    
    /**
     * Levenshtein distance hesaplar (fuzzy matching için)
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[m][n]
    }
    
    /**
     * Bir yeteneğin hangi kategoride olduğunu bulur
     */
    private fun getSkillCategory(skill: String): String? {
        val normalizedSkill = normalizeTurkish(skill)
        
        for ((category, skills) in skillCategories) {
            val normalizedSkills = skills.map { normalizeTurkish(it) }
            if (normalizedSkills.contains(normalizedSkill)) {
                return category
            }
        }
        
        return null
    }
    
    /**
     * Bir yeteneğin eş anlamlılarını bulur
     */
    private fun getSkillSynonyms(skill: String): List<String> {
        val normalizedSkill = normalizeTurkish(skill)
        
        // Direkt eş anlamlılar
        val directSynonyms = skillSynonyms[skill] ?: emptyList()
        
        // Ters eş anlamlılar (skill bir eş anlamlı olabilir)
        val reverseSynonyms = skillSynonyms.entries
            .filter { (_, synonyms) -> synonyms.contains(skill) }
            .map { it.key }
        
        return (directSynonyms + reverseSynonyms).distinct()
    }
    
    /**
     * İki kullanıcı arasında eşleşme olup olmadığını kontrol eder
     * Eşleşme kriteri:
     * - User1'in öğrenmek istediği yetenekler, User2'nin bildiği yeteneklerle eşleşmeli
     * - User2'nin öğrenmek istediği yetenekler, User1'in bildiği yeteneklerle eşleşmeli
     */
    fun isMatch(user1: User, user2: User): Boolean {
        // User1'in öğrenmek istediği yetenekler, User2'nin bildiği yeteneklerle eşleşiyor mu?
        val user1WantsMatch = user1.wantedSkills.any { wantedSkill ->
            user2.knownSkills.any { knownSkill ->
                isSkillMatch(wantedSkill, knownSkill)
            }
        }
        
        // User2'nin öğrenmek istediği yetenekler, User1'in bildiği yeteneklerle eşleşiyor mu?
        val user2WantsMatch = user2.wantedSkills.any { wantedSkill ->
            user1.knownSkills.any { knownSkill ->
                isSkillMatch(wantedSkill, knownSkill)
            }
        }
        
        // Her iki yön de eşleşmeli
        return user1WantsMatch && user2WantsMatch
    }
    
    /**
     * İki yetenek arasında eşleşme olup olmadığını kontrol eder
     * Eşleşme sadece aynı veya eş anlamlı yetenekler için geçerli!
     * 
     * 1. Tam eşleşme (aynı yetenek)
     * 2. Eş anlamlılar kontrolü (sadece tanımlanmış eş anlamlılar)
     * 3. Fuzzy matching (yazım hataları için - çok sıkı kurallar)
     * 
     * NOT: Kategori bazlı eşleşme KALDIRILDI!
     * Python bilen birisi C++ öğretemez, gitar bilen birisi piyano öğretemez.
     * Yetenekler BİREBİR eşleşmeli!
     */
    private fun isSkillMatch(skill1: String, skill2: String): Boolean {
        val normalizedSkill1 = normalizeTurkish(skill1)
        val normalizedSkill2 = normalizeTurkish(skill2)
        
        // 1. Tam eşleşme
        if (normalizedSkill1 == normalizedSkill2) {
            return true
        }
        
        // 2. Eş anlamlılar kontrolü (sadece tanımlanmış eş anlamlılar)
        val synonyms1 = getSkillSynonyms(skill1)
        val synonyms2 = getSkillSynonyms(skill2)
        
        // Skill1'in eş anlamlıları skill2 ile eşleşiyor mu?
        if (synonyms1.any { normalizeTurkish(it) == normalizedSkill2 }) {
            return true
        }
        
        // Skill2'nin eş anlamlıları skill1 ile eşleşiyor mu?
        if (synonyms2.any { normalizeTurkish(it) == normalizedSkill1 }) {
            return true
        }
        
        // Skill1 ve skill2'nin eş anlamlıları birbiriyle eşleşiyor mu?
        if (synonyms1.any { syn1 ->
            synonyms2.any { syn2 ->
                normalizeTurkish(syn1) == normalizeTurkish(syn2)
            }
        }) {
            return true
        }
        
        // 3. Fuzzy matching (sadece yazım hataları için - çok sıkı kurallar)
        // Sadece 3-10 karakter arası yetenekler için
        if (normalizedSkill1.length in 3..10 && normalizedSkill2.length in 3..10) {
            val distance = levenshteinDistance(normalizedSkill1, normalizedSkill2)
            
            // Çok sıkı kurallar:
            // - Maksimum 1 karakter farkı (yazım hatası toleransı)
            // - Uzunluk farkı en fazla 1 karakter olmalı
            val lengthDiff = kotlin.math.abs(normalizedSkill1.length - normalizedSkill2.length)
            if (distance == 1 && lengthDiff <= 1) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Kullanıcı için potansiyel eşleşmeleri bulur ve skoruna göre sıralar
     */
    fun findPotentialMatches(currentUser: User, allUsers: List<User>): List<User> {
        return findPotentialMatchesWithFilter(currentUser, allUsers, MatchFilter.DEFAULT)
    }
    
    /**
     * Filtre ile potansiyel eşleşmeleri bulur
     */
    fun findPotentialMatchesWithFilter(
        currentUser: User, 
        allUsers: List<User>, 
        filter: MatchFilter
    ): List<User> {
        return allUsers
            .filter { otherUser ->
                // Kendisi değil
                otherUser.uid != currentUser.uid &&
                // Filtre kontrolü
                passesFilter(otherUser, filter) &&
                // Eşleşme var (mod'a göre)
                if (filter.learnTogetherMode) {
                    isLearnTogetherMatch(currentUser, otherUser)
                } else {
                    isMatch(currentUser, otherUser)
                }
            }
            .sortedByDescending { otherUser ->
                // Eşleşme skoruna göre sırala (yüksek skor önce)
                if (filter.learnTogetherMode) {
                    calculateLearnTogetherScore(currentUser, otherUser)
                } else {
                    calculateMatchScore(currentUser, otherUser)
                }
            }
    }
    
    /**
     * Kullanıcının filtreyi geçip geçmediğini kontrol eder
     */
    private fun passesFilter(user: User, filter: MatchFilter): Boolean {
        // Şehir filtresi
        if (filter.city != null && !filter.city.equals(user.city, ignoreCase = true)) {
            return false
        }
        
        // Yaş filtresi
        if (filter.minAge != null && user.age < filter.minAge) {
            return false
        }
        if (filter.maxAge != null && user.age > filter.maxAge) {
            return false
        }
        
        return true
    }
    
    /**
     * Birlikte Öğrenelim modu için eşleşme kontrolü
     * Her iki kullanıcının da öğrenmek istediği en az bir ortak yetenek olmalı
     */
    fun isLearnTogetherMatch(user1: User, user2: User): Boolean {
        // User1'in öğrenmek istediği yetenekler, User2'nin de öğrenmek istediği yeteneklerle eşleşiyor mu?
        return user1.wantedSkills.any { skill1 ->
            user2.wantedSkills.any { skill2 ->
                isSkillMatch(skill1, skill2)
            }
        }
    }
    
    /**
     * Birlikte Öğrenelim modu için skor hesaplar
     * Daha fazla ortak öğrenmek istenen yetenek = daha yüksek skor
     */
    fun calculateLearnTogetherScore(user1: User, user2: User): Int {
        if (!isLearnTogetherMatch(user1, user2)) return 0
        
        // Ortak öğrenmek istenen yetenek sayısı
        val commonWantedSkills = user1.wantedSkills.count { skill1 ->
            user2.wantedSkills.any { skill2 ->
                isSkillMatch(skill1, skill2)
            }
        }
        
        // Maksimum olası eşleşme
        val maxPossible = minOf(user1.wantedSkills.size, user2.wantedSkills.size)
        
        return if (maxPossible > 0) {
            (commonWantedSkills * 100) / maxPossible
        } else {
            0
        }
    }
    
    /**
     * Eşleşme skorunu hesaplar (0-100 arası)
     * Daha fazla eşleşen yetenek = daha yüksek skor
     */
    fun calculateMatchScore(user1: User, user2: User): Int {
        if (!isMatch(user1, user2)) return 0
        
        var totalScore = 0.0
        var maxPossibleScore = 0.0
        
        // User1'in öğrenmek istediği yeteneklerin eşleşme oranı
        if (user1.wantedSkills.isNotEmpty()) {
            val user1WantedMatches = user1.wantedSkills.count { wantedSkill ->
                user2.knownSkills.any { knownSkill ->
                    isSkillMatch(wantedSkill, knownSkill)
                }
            }
            val user1Score = (user1WantedMatches * 100.0) / user1.wantedSkills.size
            totalScore += user1Score
            maxPossibleScore += 100.0
        }
        
        // User2'nin öğrenmek istediği yeteneklerin eşleşme oranı
        if (user2.wantedSkills.isNotEmpty()) {
            val user2WantedMatches = user2.wantedSkills.count { wantedSkill ->
                user1.knownSkills.any { knownSkill ->
                    isSkillMatch(wantedSkill, knownSkill)
                }
            }
            val user2Score = (user2WantedMatches * 100.0) / user2.wantedSkills.size
            totalScore += user2Score
            maxPossibleScore += 100.0
        }
        
        // Ortalama skor
        return if (maxPossibleScore > 0) {
            (totalScore / maxPossibleScore * 100).toInt()
        } else {
            0
        }
    }
}
