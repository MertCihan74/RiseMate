package com.parthenios.risemate.utils

object TextUtils {
    
    // Kapsamlı yetenek listesi (küçük harf, Türkçe karakterler olmadan)
    private val skillDatabase = listOf(
        // Programlama Dilleri
        "python", "java", "javascript", "typescript", "c", "c++", "c#", "php", "ruby", "go", "rust", 
        "swift", "kotlin", "dart", "scala", "r", "matlab", "perl", "lua", "haskell", "clojure",
        "erlang", "elixir", "f#", "objective-c", "assembly", "cobol", "fortran", "pascal", "delphi",
        "visual basic", "vb.net", "powershell", "shell scripting", "bash scripting",
        // Web Teknolojileri
        "html", "css", "react", "vue", "angular", "nodejs", "express", "django", "flask", "laravel",
        "spring", "asp.net", "rails", "nextjs", "nuxt", "svelte", "bootstrap", "tailwind",
        "sass", "less", "stylus", "webpack", "vite", "gulp", "grunt", "npm", "yarn", "pnpm",
        "graphql", "rest api", "soap", "json", "xml", "jquery", "ajax", "websocket",
        // Mobil Geliştirme
        "android", "ios", "react native", "flutter", "xamarin", "ionic", "cordova", "phonegap",
        "swiftui", "uikit", "jetpack compose", "android studio", "xcode",
        // Veritabanları
        "sql", "mysql", "postgresql", "mongodb", "redis", "oracle", "sqlite", "mariadb",
        "cassandra", "couchdb", "neo4j", "elasticsearch", "dynamodb", "firebase", "supabase",
        // Araçlar ve Frameworkler
        "git", "docker", "kubernetes", "jenkins", "ci/cd", "aws", "azure", "gcp", "linux", "bash",
        "terraform", "ansible", "puppet", "chef", "vagrant", "nginx", "apache", "tomcat",
        "jenkins", "gitlab", "github actions", "circleci", "travis ci", "bitbucket",
        // Tasarım
        "photoshop", "illustrator", "figma", "sketch", "adobe xd", "invision", "ui/ux", "grafik tasarım",
        "indesign", "premiere pro", "after effects", "lightroom", "coreldraw", "canva",
        "adobe animate", "blender", "cinema 4d", "maya", "3ds max", "zbrush", "substance painter",
        // Müzik
        "gitar", "piyano", "keman", "flüt", "saksafon", "bateri", "davul", "bas gitar", "elektro gitar",
        "akustik gitar", "klavye", "org", "şan", "vokal", "müzik prodüksiyon", "dj", "mix", "mastering",
        "vurmalı çalgılar", "yaylı çalgılar", "üflemeli çalgılar", "telli çalgılar", "arp", "mandolin",
        "banjo", "ukulele", "bağlama", "saz", "ud", "kanun", "ney", "zurna", "darbuka", "def",
        "müzik teorisi", "armoni", "kompozisyon", "orkestrasyon", "müzik notası", "solfej",
        // Spor
        "futbol", "basketbol", "voleybol", "tenis", "masa tenisi", "yüzme", "koşu", "fitness", "crossfit",
        "pilates", "yoga", "dans", "bale", "jimnastik", "atletizm", "bisiklet", "kayak", "snowboard",
        "judo", "karate", "taekwondo", "aikido", "boks", "kick boks", "muay thai", "jiu jitsu",
        "güreş", "okçuluk", "binicilik", "golf", "bowling", "bilardo", "satranç", "go", "dama",
        "rafting", "tırmanış", "dağcılık", "kamp", "doğa yürüyüşü", "trekking", "hiking",
        "sörf", "dalış", "su kayağı", "yelken", "kürek", "kano", "rafting",
        // Sanat
        "resim", "çizim", "karikatür", "graffiti", "heykel", "seramik", "cam işçiliği", "mozaik",
        "fotoğrafçılık", "video çekimi", "video edit", "animasyon", "3d modelleme", "mimari çizim",
        "suluboya", "akrilik", "yağlı boya", "pastel", "karakalem", "manga", "anime", "dijital sanat",
        "kaligrafi", "hat sanatı", "ebru", "tezhip", "minyatür", "fresk", "duvar resmi",
        "stop motion", "motion graphics", "vfx", "compositing", "color grading",
        // El Sanatları
        "dikiş", "nakış", "örgü", "tığ işi", "takı yapımı", "ahşap işçiliği", "metal işçiliği",
        "deri işçiliği", "kuyumculuk", "cam işçiliği", "seramik", "çömlekçilik",
        "marangozluk", "doğramacılık", "oymacılık", "kakmacılık", "sedef işçiliği",
        "halı dokuma", "kilim dokuma", "kumaş dokuma", "battaniye örme", "eldiven örme",
        "çanta yapımı", "ayakkabı yapımı", "terzilik", "moda tasarımı", "aksesuar tasarımı",
        // Diller
        "ingilizce", "almanca", "fransızca", "ispanyolca", "italyanca", "portekizce", "rusça",
        "çince", "japonca", "korece", "arapça", "farsça", "yunanca", "lehçe",
        "macarca", "romence", "bulgarca", "sırpça", "hırvatça", "boşnakça", "arapça",
        "fince", "isveççe", "norveççe", "danca", "hollandaca", "flamanca", "belçika dili",
        "türkçe", "türk dili", "dil bilgisi", "çeviri", "tercüme", "simultane çeviri",
        // Akademik
        "matematik", "fizik", "kimya", "biyoloji", "tarih", "coğrafya", "felsefe", "sosyoloji",
        "psikoloji", "edebiyat", "türkçe", "türk dili", "dil bilgisi",
        "astronomi", "jeoloji", "arkeoloji", "antropoloji", "ekonomi", "işletme", "hukuk",
        "siyaset bilimi", "uluslararası ilişkiler", "gazetecilik", "iletişim", "reklamcılık",
        "mimarlık", "mühendislik", "endüstri mühendisliği", "makine mühendisliği", "elektrik mühendisliği",
        "bilgisayar mühendisliği", "yazılım mühendisliği", "inşaat mühendisliği", "kimya mühendisliği",
        // Pratik Beceriler
        "yemek yapma", "mutfak", "pastacılık", "kahve yapımı", "barista", "bahçıvanlık", "bitki bakımı",
        "ev temizliği", "organizasyon", "zaman yönetimi", "liderlik", "iletişim", "sunum",
        "türk mutfağı", "italyan mutfağı", "fransız mutfağı", "japon mutfağı", "çin mutfağı",
        "mexican mutfağı", "hint mutfağı", "vegan yemek", "vejetaryen yemek", "gluten-free yemek",
        "ekmek yapımı", "pasta yapımı", "çikolata yapımı", "dondurma yapımı", "şarap yapımı",
        "bahçe tasarımı", "peyzaj mimarisi", "bitki yetiştirme", "sebze yetiştirme", "meyve yetiştirme",
        // Teknik
        "elektronik", "elektrik", "makine", "otomotiv", "araba tamiri", "motor tamiri",
        "bilgisayar tamiri", "telefon tamiri", "elektronik tamiri", "saat tamiri",
        "klima tamiri", "buzdolabı tamiri", "çamaşır makinesi tamiri", "bulaşık makinesi tamiri",
        "elektrik tesisatı", "su tesisatı", "kalorifer tesisatı", "doğalgaz tesisatı",
        "boyama", "badana", "sıva", "alçı", "fayans", "seramik döşeme", "parke döşeme",
        // İş ve Kariyer
        "yazılım", "programlama", "kodlama", "web geliştirme", "mobil geliştirme", "oyun geliştirme",
        "kitap okuma", "yazma", "blog yazma", "içerik üretimi", "sosyal medya", "pazarlama",
        "satış", "muhasebe", "finans", "yönetim", "girişimcilik", "koçluk", "mentorluk",
        "proje yönetimi", "scrum", "agile", "kanban", "lean", "six sigma", "kalite yönetimi",
        "insan kaynakları", "işe alım", "eğitim", "öğretim", "danışmanlık", "strateji",
        "dijital pazarlama", "seo", "sem", "ppc", "sosyal medya pazarlama", "influencer marketing",
        "e-ticaret", "dropshipping", "affiliate marketing", "içerik pazarlama", "email marketing",
        // Yaratıcı ve Eğlence
        "oyunculuk", "tiyatro", "drama", "improvisation", "stand-up", "komedi", "mizah",
        "hikaye anlatma", "storytelling", "senaryo yazma", "film yapımı", "belgesel yapımı",
        "podcast", "radyo", "sunuculuk", "spikerlik", "seslendirme", "dublaj",
        "oyun tasarımı", "level design", "game art", "pixel art", "concept art",
        // Sağlık ve Wellness
        "meditasyon", "mindfulness", "nefes egzersizi", "masaj", "refleksoloji", "akupunktur",
        "ayurveda", "homeopati", "bitkisel tedavi", "beslenme", "diyetisyenlik", "fitness antrenörlüğü",
        "kişisel antrenörlük", "pilates eğitmenliği", "yoga eğitmenliği", "dans eğitmenliği",
        "fizyoterapi", "ergoterapi", "psikoterapi", "danışmanlık", "yaşam koçluğu",
        // Hayvan Bakımı
        "köpek bakıcılığı", "kedi bakıcılığı", "hayvan bakıcılığı", "at bakıcılığı", "kuş bakıcılığı",
        "balık bakımı", "sürüngen bakımı", "hayvan eğitimi", "köpek eğitimi", "at eğitimi",
        "veterinerlik", "hayvan sağlığı", "hayvan beslenmesi",
        // Çocuk ve Yaşlı Bakımı
        "çocuk bakıcılığı", "bebek bakımı", "yaşlı bakımı", "hasta bakımı", "engelli bakımı",
        "özel eğitim", "çocuk gelişimi", "pedagoji", "okul öncesi eğitim",
        // Ulaşım
        "araba kullanma", "motor kullanma", "bisiklet", "ehliyet", "trafik kuralları",
        "navigasyon", "harita okuma", "seyahat planlama",
        // Diğer
        "araştırma", "analiz", "problem çözme", "kritik düşünme", "yaratıcı düşünme",
        "takım çalışması", "işbirliği", "müzakere", "ikna", "satış teknikleri",
        "müşteri hizmetleri", "call center", "telefon görüşmesi", "email yazma",
        "sunum teknikleri", "toplantı yönetimi", "etkinlik organizasyonu", "düğün organizasyonu"
    )
    
    /**
     * Levenshtein distance hesaplar (iki string arasındaki fark)
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
     * Türkçe karakterleri İngilizce karşılıklarına çevirir (karşılaştırma için)
     */
    private fun normalizeTurkish(text: String): String {
        return text.lowercase()
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
     * En yakın yeteneği bulur (fuzzy matching)
     */
    private fun findClosestSkill(input: String, maxDistance: Int = 2): String? {
        val normalizedInput = normalizeTurkish(input.lowercase().trim())
        
        var bestMatch: String? = null
        var minDistance = Int.MAX_VALUE
        
        for (skill in skillDatabase) {
            val normalizedSkill = normalizeTurkish(skill)
            val distance = levenshteinDistance(normalizedInput, normalizedSkill)
            
            // Tam eşleşme varsa direkt dön
            if (distance == 0) {
                return skill
            }
            
            // Daha yakın bir eşleşme bulunduysa güncelle
            if (distance < minDistance && distance <= maxDistance) {
                minDistance = distance
                bestMatch = skill
            }
        }
        
        return bestMatch
    }
    
    /**
     * Metni Türkçe sözlüğe uygun şekilde düzenler
     * - İlk harfi büyük yapar
     * - Diğer harfleri küçük yapar
     * - Türkçe karakterleri doğru şekilde büyük/küçük yapar
     */
    fun capitalizeTurkish(text: String): String {
        if (text.isEmpty()) return text
        
        val trimmed = text.trim()
        val words = trimmed.split("\\s+".toRegex())
        
        return words.joinToString(" ") { word ->
            if (word.isEmpty()) return@joinToString ""
            
            val lowerWord = word.lowercase()
            
            // İlk harfi büyük yap (Türkçe karakterleri dikkate alarak)
            val firstChar = when (lowerWord[0]) {
                'i' -> 'İ'
                'ı' -> 'I'
                'ğ' -> 'Ğ'
                'ü' -> 'Ü'
                'ş' -> 'Ş'
                'ö' -> 'Ö'
                'ç' -> 'Ç'
                else -> lowerWord[0].uppercaseChar()
            }
            
            // Geri kalan harfleri küçük yap
            val restChars = lowerWord.substring(1)
            
            firstChar + restChars
        }
    }
    
    /**
     * Yetenek adını düzenler
     */
    fun formatSkillName(skill: String): String {
        return capitalizeTurkish(skill)
    }
    
    /**
     * Yaygın yetenek isimlerini düzeltir (fuzzy matching ile)
     */
    fun correctCommonSkills(skill: String): String {
        if (skill.trim().isEmpty()) return skill
        
        // Önce tam eşleşme kontrolü (hızlı)
        val normalizedInput = normalizeTurkish(skill.lowercase().trim())
        val exactMatch = skillDatabase.find { normalizeTurkish(it) == normalizedInput }
        if (exactMatch != null) {
            return formatSkillName(exactMatch)
        }
        
        // Fuzzy matching ile en yakın eşleşmeyi bul
        // Maksimum 2 karakter farkına izin ver (ör: python -> ptyhon = 1 fark)
        val closestMatch = findClosestSkill(skill, maxDistance = 2)
        
        return if (closestMatch != null) {
            formatSkillName(closestMatch)
        } else {
            // Eşleşme bulunamadıysa, kullanıcının girdiğini formatla
            formatSkillName(skill)
        }
    }
}
