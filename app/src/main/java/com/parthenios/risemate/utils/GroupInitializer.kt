package com.parthenios.risemate.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.data.SkillGroup
import kotlinx.coroutines.tasks.await

/**
 * Firebase'de yetenek gruplarını, şehir gruplarını ve proje grubunu otomatik oluşturan yardımcı sınıf
 */
object GroupInitializer {

    private val firestore = FirebaseFirestore.getInstance()

    // 81 Türkiye ili
    val turkishCities = listOf(
        "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin",
        "Aydın", "Balıkesir", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale",
        "Çankırı", "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum",
        "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkâri", "Hatay", "Isparta", "Mersin",
        "İstanbul", "İzmir", "Kars", "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir", "Kocaeli",
        "Konya", "Kütahya", "Malatya", "Manisa", "Kahramanmaraş", "Mardin", "Muğla", "Muş",
        "Nevşehir", "Niğde", "Ordu", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop", "Sivas",
        "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Şanlıurfa", "Uşak", "Van", "Yozgat", "Zonguldak",
        "Aksaray", "Bayburt", "Karaman", "Kırıkkale", "Batman", "Şırnak", "Bartın", "Ardahan",
        "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye", "Düzce"
    )

    // Proje grubu bilgisi
    private const val PROJECT_GROUP_NAME = "Proje İşbirliği"
    private const val PROJECT_GROUP_DESCRIPTION = "Projeler üzerinde birlikte çalışmak, işbirliği yapmak ve deneyim paylaşmak isteyenler için ortak alan."
    private const val PROJECT_GROUP_SKILL = "project_collaboration"

    // SkillQuizData'daki TÜM yetenekler (63 adet)
    private val skillGroups = mapOf(
        // ========== PROGRAMLAMA DİLLERİ ==========
        "Python" to Pair("Programlama", "Python programlama dilini öğrenmek ve geliştirmek isteyenler için."),
        "Java" to Pair("Programlama", "Java programlama ve nesne yönelimli geliştirme topluluğu."),
        "JavaScript" to Pair("Programlama", "JavaScript ve modern web geliştirme meraklıları."),
        "Kotlin" to Pair("Programlama", "Kotlin ile Android ve multiplatform geliştirme."),
        "TypeScript" to Pair("Programlama", "TypeScript ile tip güvenli JavaScript geliştirme."),
        "C++" to Pair("Programlama", "C++ programlama ve sistem geliştirme topluluğu."),
        "C#" to Pair("Programlama", "C# ve .NET framework geliştirme."),
        "Swift" to Pair("Programlama", "Swift programlama dili ve Apple ekosistemi."),
        "Go" to Pair("Programlama", "Go (Golang) ile yüksek performanslı yazılım."),
        "Dart" to Pair("Programlama", "Dart programlama dili ve Flutter."),
        
        // ========== WEB GELİŞTİRME ==========
        "HTML" to Pair("Web Geliştirme", "HTML ile web sayfası yapısı oluşturma."),
        "CSS" to Pair("Web Geliştirme", "CSS ile web tasarımı ve stil uygulamaları."),
        "React" to Pair("Web Geliştirme", "React ile modern web uygulamaları."),
        "Node.js" to Pair("Web Geliştirme", "Node.js ile sunucu tarafı JavaScript."),
        "Frontend" to Pair("Web Geliştirme", "Ön yüz (frontend) web geliştirme."),
        "Backend" to Pair("Web Geliştirme", "Arka yüz (backend) geliştirme ve API."),
        "Fullstack" to Pair("Web Geliştirme", "Fullstack web geliştirme - hem frontend hem backend."),
        
        // ========== MOBİL GELİŞTİRME ==========
        "Android" to Pair("Mobil Geliştirme", "Android uygulama geliştirme topluluğu."),
        "iOS" to Pair("Mobil Geliştirme", "iOS ve Apple platformları için uygulama."),
        "Flutter" to Pair("Mobil Geliştirme", "Flutter ile cross-platform mobil uygulama."),
        "React Native" to Pair("Mobil Geliştirme", "React Native ile mobil uygulama."),
        
        // ========== MÜZİK ==========
        "Gitar" to Pair("Müzik", "Gitar çalmayı öğrenmek ve geliştirmek isteyenler."),
        "Piyano" to Pair("Müzik", "Piyano eğitimi, teknikler ve repertuvar."),
        "Keman" to Pair("Müzik", "Keman çalmayı öğrenmek ve icra etmek."),
        "Bateri" to Pair("Müzik", "Bateri ve perküsyon enstrümanları."),
        "Şan" to Pair("Müzik", "Vokal eğitimi, şan teknikleri ve ses geliştirme."),
        "Müzik Prodüksiyon" to Pair("Müzik", "Dijital müzik üretimi, mixing ve mastering."),
        
        // ========== SPOR & FITNESS ==========
        "Futbol" to Pair("Spor", "Futbol oynamak ve taktik geliştirmek isteyenler."),
        "Basketbol" to Pair("Spor", "Basketbol oyunu, teknikler ve stratejiler."),
        "Fitness" to Pair("Spor", "Fitness, vücut geliştirme ve sağlıklı yaşam."),
        "Yoga" to Pair("Spor", "Yoga pratiği, meditasyon ve zihin-beden dengesi."),
        "Boks" to Pair("Spor", "Boks teknikleri ve dövüş sporları."),
        "Yüzme" to Pair("Spor", "Yüzme teknikleri ve su sporları."),
        "Tenis" to Pair("Spor", "Tenis oyunu ve raket sporları."),
        "Voleybol" to Pair("Spor", "Voleybol oyunu ve takım sporları."),
        "Bisiklet" to Pair("Spor", "Bisiklet sürme, rotalar ve ekipman."),
        "Pilates" to Pair("Spor", "Pilates egzersizleri ve esneklik."),
        "Dövüş Sanatları" to Pair("Spor", "Uzak doğu dövüş sanatları ve savunma."),
        "Koşu" to Pair("Spor", "Koşu, maraton hazırlığı ve dayanıklılık."),
        
        // ========== DİLLER ==========
        "İngilizce" to Pair("Dil", "İngilizce öğrenme ve konuşma pratiği."),
        "Almanca" to Pair("Dil", "Almanca dil öğrenimi ve kültür."),
        "Fransızca" to Pair("Dil", "Fransızca konuşma ve kültür."),
        "İtalyanca" to Pair("Dil", "İtalyanca dil öğrenimi ve kültür."),
        "İspanyolca" to Pair("Dil", "İspanyolca konuşma ve Latin Amerika kültürü."),
        "Arapça" to Pair("Dil", "Arapça dil öğrenimi ve kültür."),
        "Japonca" to Pair("Dil", "Japonca öğrenme ve Japon kültürü."),
        "Korece" to Pair("Dil", "Korece dil öğrenimi ve Kore kültürü."),
        
        // ========== AKADEMİK ==========
        "Matematik" to Pair("Akademik", "Matematik problemleri, teoriler ve uygulamalar."),
        "Fizik" to Pair("Akademik", "Fizik bilimleri, deneyler ve teorik fizik."),
        "Kimya" to Pair("Akademik", "Kimya bilimleri ve laboratuvar çalışmaları."),
        "Biyoloji" to Pair("Akademik", "Biyoloji bilimleri ve yaşam bilimleri."),
        "Tarih" to Pair("Akademik", "Tarih araştırmaları ve geçmişin izinde."),
        "Psikoloji" to Pair("Akademik", "Psikoloji bilimleri ve insan davranışları."),
        "Ekonomi" to Pair("Akademik", "Ekonomi bilimleri, finans ve piyasalar."),
        "Edebiyat" to Pair("Akademik", "Edebiyat, yazarlık ve kitap tartışmaları."),
        "Mühendislik" to Pair("Akademik", "Mühendislik bilimleri ve teknik çözümler."),
        "Tıp" to Pair("Akademik", "Tıp bilimleri ve sağlık alanı."),
        "Hukuk" to Pair("Akademik", "Hukuk bilimleri ve yasal konular."),
        
        // ========== SANAT & TASARIM ==========
        "Fotoğrafçılık" to Pair("Sanat", "Fotoğraf çekimi, düzenleme ve görsel sanatlar."),
        "Grafik Tasarım" to Pair("Sanat", "Grafik tasarım, logo ve görsel kimlik."),
        "Resim" to Pair("Sanat", "Resim sanatı, çizim teknikleri ve tablolar."),
        "Video Edit" to Pair("Sanat", "Video düzenleme, montaj ve prodüksiyon.")
    )

    suspend fun initializeGroupsIfNeeded() {
        try {
            // Mevcut grupları kontrol et
            val existingGroups = firestore.collection("groups").get().await()
            val existingSkills = existingGroups.documents.mapNotNull { 
                it.getString("skill") 
            }.toSet()

            // Eksik yetenek gruplarını bul ve oluştur
            val missingSkills = skillGroups.keys.filter { it !in existingSkills }
            
            if (missingSkills.isNotEmpty()) {
                for (skill in missingSkills) {
                    val (category, description) = skillGroups[skill] ?: continue
                    createSkillGroup(skill, category, description)
                }
            }

            // Proje grubunu oluştur (yoksa)
            if (PROJECT_GROUP_SKILL !in existingSkills) {
                createProjectGroup()
            }

            // Şehir gruplarını oluştur (yoksa)
            val existingCityGroups = existingGroups.documents
                .filter { it.getString("groupType") == "city" }
                .mapNotNull { it.getString("skill") }
                .toSet()
            
            val missingCities = turkishCities.filter { it !in existingCityGroups }
            for (city in missingCities) {
                createCityGroup(city)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun createSkillGroup(skill: String, category: String, description: String) {
        try {
            val groupData = hashMapOf(
                "name" to "$skill Grubu",
                "description" to description,
                "skill" to skill,
                "category" to category,
                "groupType" to "skill",
                "photoUrl" to "",
                "memberCount" to 0,
                "createdAt" to java.util.Date()
            )
            firestore.collection("groups").add(groupData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun createProjectGroup() {
        try {
            val groupData = hashMapOf(
                "name" to PROJECT_GROUP_NAME,
                "description" to PROJECT_GROUP_DESCRIPTION,
                "skill" to PROJECT_GROUP_SKILL,
                "category" to "Ortak",
                "groupType" to "project",
                "photoUrl" to "",
                "memberCount" to 0,
                "createdAt" to java.util.Date()
            )
            firestore.collection("groups").add(groupData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun createCityGroup(city: String) {
        try {
            val groupData = hashMapOf(
                "name" to "$city Grubu",
                "description" to "$city şehrindeki RiseMate kullanıcıları için buluşma noktası. Şehrinizdeki insanlarla tanışın, etkinlikler düzenleyin!",
                "skill" to city,
                "category" to "Şehir",
                "groupType" to "city",
                "photoUrl" to "",
                "memberCount" to 0,
                "createdAt" to java.util.Date()
            )
            firestore.collection("groups").add(groupData).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createGroupForSkill(skill: String): String? {
        try {
            val existing = firestore.collection("groups")
                .whereEqualTo("skill", skill)
                .get()
                .await()

            if (!existing.isEmpty) {
                return existing.documents.first().id
            }

            val categoryDesc = skillGroups[skill] ?: Pair("Diğer", "$skill ile ilgilenenler için grup.")
            val (category, description) = categoryDesc

            val groupData = hashMapOf(
                "name" to "$skill Grubu",
                "description" to description,
                "skill" to skill,
                "category" to category,
                "groupType" to "skill",
                "photoUrl" to "",
                "memberCount" to 0,
                "createdAt" to java.util.Date()
            )

            val docRef = firestore.collection("groups").add(groupData).await()
            return docRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Kullanıcının şehir grubunu ve proje grubunu getirir (Ortak tab için)
     */
    suspend fun getCommonGroupsForUser(userCity: String): List<SkillGroup> {
        val commonGroups = mutableListOf<SkillGroup>()
        
        try {
            // Proje işbirliği grubunu getir
            val projectGroup = firestore.collection("groups")
                .whereEqualTo("groupType", "project")
                .get()
                .await()
            
            projectGroup.documents.mapNotNull { doc ->
                doc.toObject(SkillGroup::class.java)?.copy(id = doc.id)
            }.forEach { commonGroups.add(it) }

            // Kullanıcının şehir grubunu getir
            if (userCity.isNotEmpty()) {
                val cityGroup = firestore.collection("groups")
                    .whereEqualTo("groupType", "city")
                    .whereEqualTo("skill", userCity)
                    .get()
                    .await()
                
                cityGroup.documents.mapNotNull { doc ->
                    doc.toObject(SkillGroup::class.java)?.copy(id = doc.id)
                }.forEach { commonGroups.add(it) }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return commonGroups
    }
    
    // Tüm yeteneklerin listesi (dışarıdan erişim için)
    fun getAllSkills(): List<String> = skillGroups.keys.toList()
}


