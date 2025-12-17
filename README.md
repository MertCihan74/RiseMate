# RiseMate

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="RiseMate Logo" width="120"/>
</p>

<p align="center">
  <b>Rise</b><i style="color: #FFD700;">Mate</i> - Yeteneklerini paylaş, yeni şeyler öğren!
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=flat-square" alt="Platform"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-blue?style=flat-square" alt="Language"/>
  <img src="https://img.shields.io/badge/Firebase-Firestore-orange?style=flat-square" alt="Firebase"/>
  <img src="https://img.shields.io/badge/Min%20SDK-24-brightgreen?style=flat-square" alt="Min SDK"/>
  <img src="https://img.shields.io/badge/Version-1.1.0-purple?style=flat-square" alt="Version"/>
</p>

---

## 🎯 Uygulama Hakkında

**RiseMate**, birbirinden öğrenmek isteyen insanları buluşturan bir yetenek takası platformudur. Kullanıcılar mevcut yeteneklerini paylaşır ve öğrenmek istediklerini belirtir, böylece bilginin serbestçe aktığı bir topluluk oluşturulur.

### Nasıl Çalışır?

1. 📝 Bildiğiniz yetenekleri seçin (örn: "Gitar", "Python")
2. 🎯 Öğrenmek istediğiniz yetenekleri seçin (örn: "Piyano", "İngilizce")
3. 🤝 Uygulama sizi tamamlayıcı yeteneklere sahip kişilerle eşleştirir
4. 💬 Eşleştiğiniz kişilerle mesajlaşın ve yetenek takası yapın!

**Örnek:** Siz gitar biliyorsunuz ve programlama öğrenmek istiyorsunuz. Başka biri programlama biliyor ve gitar öğrenmek istiyor. İkiniz eşleşiyorsunuz ve karşılıklı öğrenmeye başlıyorsunuz!

---

## ✨ Özellikler

### 🔐 Kimlik Doğrulama
- E-posta/Şifre ile kayıt ve giriş
- Google ile giriş (Google Sign-In)
- Güvenli şifre doğrulama
- E-posta doğrulama

### 👤 Profil Yönetimi
- **6 Adımlı Kayıt Süreci:**
  1. E-posta ve Şifre
  2. Kişisel Bilgiler (ad, soyad, kullanıcı adı)
  3. Doğum Tarihi (yaş hesaplaması ile)
  4. Şehir Seçimi (81 Türkiye ili)
  5. Bilinen Yetenekler (kategorili seçim)
  6. Öğrenmek İstenen Yetenekler
- Profil fotoğrafı yükleme ve düzenleme (UCrop ile kırpma)
- Profil bilgilerini güncelleme
- Kendi profilini ve diğer kullanıcıların profillerini görüntüleme

### 👥 Takip/Takipçi Sistemi
- Diğer kullanıcıları takip etme
- Takipçi ve takip edilen sayılarını görüntüleme
- Takipçiler ve takip edilenler listesi
- Takipten çıkma özelliği
- Profil sayfasından kolay takip yönetimi

### 🔍 Keşfet & Eşleşme
- **Akıllı Eşleşme Algoritması:**
  - Karşılıklı yetenek eşleşmesi (A'nın bildiği = B'nin istediği VE B'nin bildiği = A'nın istediği)
  - Eş anlamlı yetenek tanıma (örn: "gitar" = "akustik gitar")
  - Yazım hatası toleransı (Levenshtein distance)
- Sağa kaydırma ile beğenme
- Sola kaydırma ile geçme
- Kullanıcı kartlarında profil fotoğrafı, yaş, şehir ve yetenekler

### 🎛️ Gelişmiş Filtreleme
- **Şehir Filtresi:**
  - Tüm şehirlerden seçim
  - "Kendi şehrimdekileri bul" seçeneği
- **Yaş Filtresi:**
  - RangeSlider ile esnek yaş aralığı seçimi (18-65+)
- **Birlikte Öğrenelim Modu:**
  - Sadece öğrenmek istediğiniz yetenekleri ortak olan kişilerle eşleşme
  - Öğretmek yerine birlikte öğrenme odaklı eşleşme
- Toolbar'da filtre butonu ve aktif filtre rozeti (badge)
- Filtreleri tek tıkla temizleme

### 💌 Eşleşme İstekleri
- Gelen eşleşme isteklerini görüntüleme
- İstek kabul etme / reddetme
- Bekleyen ve kabul edilen istek sayıları
- 24 saat içinde mesajlaşılmazsa eşleşme otomatik siliniyor

### 💬 Mesajlaşma
- Gerçek zamanlı mesajlaşma (Firestore)
- Fotoğraf gönderme (Base64 sıkıştırma ile)
- Fotoğrafları tam ekranda görüntüleme
- Mesaj arama
- Çevrimiçi/son görülme durumu
- Mesaj silme

### 📝 Sosyal Paylaşım (Gönderiler)
- **Gönderi Oluşturma:**
  - Metin tabanlı gönderiler
  - Fotoğraflı gönderiler
  - Yetenek etiketleme
- **Etkileşim:**
  - Gönderi beğenme (❤️)
  - Yorum yapma
  - Yorumlara yanıt verme
- **Profil Entegrasyonu:**
  - "Bilgiler" ve "Gönderiler" sekmeleri
  - Kendi gönderilerini düzenleme/silme
  - Diğer kullanıcıların gönderilerini görüntüleme

### ❓ Forum / Soru-Cevap
- **Soru Sorma:**
  - Yeteneklerle ilgili sorular
  - Kategori seçimi
  - Detaylı açıklama
- **Cevap Verme:**
  - Soruları cevaplama
  - En iyi cevap seçimi
  - Cevapları oylama
- **Arama & Filtreleme:**
  - Kategoriye göre filtreleme
  - Anahtar kelime araması
  - Popüler ve güncel sorular

### 🎓 Yetenek Quiz Sistemi
- **10+ Farklı Yetenek Kategorisinde Quiz:**
  - 💻 Python Programlama
  - ☕ Java Programlama
  - 🌐 JavaScript
  - 📱 Kotlin & Android
  - ⚛️ React
  - 🎸 Gitar
  - 🎹 Piyano
  - 📸 Fotoğrafçılık
  - 🎨 Grafik Tasarım
  - 🇬🇧 İngilizce
  - 🇩🇪 Almanca
  - Ve daha fazlası...
- **Quiz Özellikleri:**
  - Her quiz'de 10 soru
  - Çoktan seçmeli format
  - Soru başına 30 saniye süre
  - Anlık doğru/yanlış geri bildirimi
  - Detaylı sonuç raporu
- **Skor Sistemi:**
  - Yüzdelik başarı oranı
  - Seviye belirleme (Başlangıç/Orta/İleri)
  - Profilde rozet görünümü
  - Quiz geçmişi

### 🔒 Gizlilik & Güvenlik
- **Engelleme Sistemi:**
  - Kullanıcı engelleme (profil veya chat üzerinden)
  - Engellenen kullanıcılar listesi
  - Engel kaldırma
  - Engelleme yapıldığında tüm chat geçmişi silinir
- **Gizlilik Ayarları:**
  - Çevrimiçi durumunu gizleme
  - Son görülmeyi gizleme
- Hesap silme (tüm veriler kalıcı olarak silinir)

### ⚙️ Ayarlar
- **Bildirim Ayarları:**
  - Bildirimler açık/kapalı
  - Bildirim sesi
  - Bildirim titreşimi
- **Gizlilik Ayarları:**
  - Çevrimiçi durumu gizleme
  - Son görülme gizleme
  - Engellenen kullanıcılar listesi
- **Hesap:**
  - Hesabı sil (2 aşamalı onay)
- **Hakkında:**
  - Uygulama versiyonu
  - Gizlilik Politikası
  - Kullanım Koşulları
  - Destek (e-posta)

### 🎨 Kullanıcı Arayüzü
- **Modern Tasarım:**
  - Material Design 3
  - Mor tema paleti
  - Özel font stilleri (Serif Italic logo)
  - Gradient arka planlar
- **Animasyonlu Bottom Navigation Bar** (AnimatedBottomBar kütüphanesi)
- **Yan Menü (Drawer):**
  - Kullanıcı profil fotoğrafı ve adı
  - Hızlı navigasyon (Ayarlar, Forum, Quiz, Yardım)
- **Toolbar Özellikleri:**
  - Merkezi "RiseMate" logosu
  - Profil fotoğrafı butonu
  - Dinamik filtre butonu (Keşfet sayfasında)
- **Empty State Tasarımları:**
  - 🔍 Keşfet - "Eşleşme bulunamadı"
  - 💫 Eşleşmeler - "Henüz istek yok"
  - 💬 Sohbet - "Henüz mesaj yok"
- **Bottom Sheet Tasarımları:**
  - Filtre seçimi
  - RangeSlider ile yaş aralığı
  - Switch ile mod seçimi

---

## 🛠️ Teknik Altyapı

### Tech Stack
| Kategori | Teknoloji |
|----------|-----------|
| **Platform** | Android (Min SDK 24) |
| **Dil** | Kotlin |
| **Backend** | Firebase (Auth + Firestore + Storage) |
| **Mimari** | Repository Pattern + MVVM |
| **UI** | Material Design 3, XML Layouts |
| **Async** | Kotlin Coroutines |
| **Görsel** | Glide, UCrop |
| **Navigation** | AnimatedBottomBar |
| **Components** | RangeSlider, BottomSheetDialog, RecyclerView |

### Proje Yapısı
```
app/src/main/
├── java/com/parthenios/risemate/
│   ├── auth/              # Kimlik doğrulama
│   ├── data/              # Data modelleri
│   │   ├── Follow.kt          # Takip modeli
│   │   ├── FollowRepository.kt # Takip işlemleri
│   │   ├── MatchFilter.kt     # Filtre modeli
│   │   ├── Post.kt            # Gönderi modeli
│   │   ├── Comment.kt         # Yorum modeli
│   │   ├── Question.kt        # Soru modeli
│   │   ├── Answer.kt          # Cevap modeli
│   │   ├── QuizResult.kt      # Quiz sonuç modeli
│   │   ├── SkillData.kt       # Yetenek verileri
│   │   └── SkillQuizData.kt   # Quiz soru verileri
│   ├── ui/
│   │   ├── auth/          # Giriş/Kayıt ekranları
│   │   ├── chat/          # Mesajlaşma
│   │   ├── explore/       # Keşfet (filtreleme dahil)
│   │   ├── forum/         # Soru-Cevap
│   │   ├── matching/      # Eşleşmeler
│   │   ├── profile/       # Profil (takipçiler, gönderiler)
│   │   │   ├── ProfileFragment.kt
│   │   │   ├── ProfileActivity.kt
│   │   │   ├── FollowersActivity.kt
│   │   │   ├── FollowingActivity.kt
│   │   │   └── PostsAdapter.kt
│   │   ├── quiz/          # Yetenek quizleri
│   │   ├── registration/  # Kayıt adımları
│   │   └── settings/      # Ayarlar
│   ├── utils/             # Yardımcı sınıflar
│   │   └── MatchAlgorithm.kt  # Eşleşme algoritması
│   └── MainActivity.kt
├── res/
│   ├── layout/            # XML layoutlar
│   │   ├── bottom_sheet_filter.xml
│   │   ├── item_follower.xml
│   │   ├── item_following.xml
│   │   ├── item_post.xml
│   │   └── ...
│   ├── drawable/          # Görseller ve şekiller
│   │   ├── ic_filter.xml
│   │   ├── badge_background.xml
│   │   └── ...
│   ├── values/            # Renkler, stringler, arrayler
│   ├── menu/              # Menüler
│   └── font/              # Fontlar
└── assets/
    ├── privacy_policy.html
    └── terms_of_service.html
```

### Temel Bileşenler

#### Eşleşme Algoritması (`MatchAlgorithm.kt`)
- Karşılıklı yetenek eşleşmesi kontrolü
- Eş anlamlı yetenek tanıma
- Yazım hatası toleransı (Levenshtein distance)
- Eşleşme skoru hesaplama
- **Birlikte Öğrenelim modu** (öğrenmek istenen yeteneklere göre eşleşme)
- **Filtreli eşleşme** (şehir, yaş aralığı)

#### Takip Sistemi (`FollowRepository.kt`)
- Takip etme / Takipten çıkma
- Takipçi sayısı alma
- Takip edilen sayısı alma
- Takipçi/Takip edilen listesi

#### Yetenek Sistemi (`SkillData.kt`)
Kategorize edilmiş 50+ yetenek:
- 💻 Yazılım Dilleri
- 📱 Mobil Geliştirme
- 🎵 Müzik & Enstrümanlar
- 🥊 Spor & Dövüş Sanatları
- 🎨 Tasarım & Sanat
- 📚 Akademik
- 🌍 Diller
- Ve daha fazlası...

#### Quiz Sistemi (`SkillQuizData.kt`)
- 10+ kategoride quiz soruları
- Her kategoride 15-20 soru
- Zorluk seviyeleri
- Detaylı açıklamalar

---

## 📱 Ekran Görüntüleri

| Giriş | Keşfet | Filtreler | Eşleşmeler |
|-------|--------|-----------|------------|
| Login ekranı | Kullanıcı kartları | Filtre bottom sheet | İstek listesi |

| Sohbet | Profil | Takipçiler | Quiz |
|--------|--------|------------|------|
| Mesajlaşma | Profil detay | Takipçi listesi | Quiz ekranı |

| Gönderiler | Forum | Ayarlar |
|------------|-------|---------|
| Post paylaşma | Soru-Cevap | Uygulama ayarları |

---

## 🚀 Kurulum

### Gereksinimler
- Android Studio Arctic Fox veya üzeri
- JDK 11+
- Android SDK 24+

### Adımlar

1. **Projeyi klonlayın:**
```bash
git clone https://github.com/your-username/RiseMate.git
cd RiseMate
```

2. **Firebase kurulumu:**
   - [Firebase Console](https://console.firebase.google.com)'da yeni proje oluşturun
   - Android uygulaması ekleyin (paket adı: `com.parthenios.risemate`)
   - `google-services.json` dosyasını Firebase Console'dan indirin
   - `app/google-services.json.example` dosyasını referans alarak `app/google-services.json` olarak kaydedin
   - Authentication'da Email/Password ve Google Sign-In'i etkinleştirin
   - Firestore Database oluşturun
   - Storage'ı etkinleştirin (profil fotoğrafları için)
   
   > ⚠️ **Not:** `google-services.json` dosyası güvenlik nedeniyle repoya dahil edilmemiştir. Kendi Firebase projenizden indirmeniz gerekmektedir.

3. **Projeyi açın ve çalıştırın:**
```bash
./gradlew assembleDebug
```

---

## 📄 Gizlilik & Kullanım

- **Gizlilik Politikası:** Uygulama içinden erişilebilir
- **Kullanım Koşulları:** Uygulama içinden erişilebilir
- **Destek:** risemateapp@gmail.com

---

## 🎨 Marka Renkleri

| Renk | Hex | Kullanım |
|------|-----|----------|
| **Primary (Mor)** | `#6200EE` | Ana tema rengi |
| **Secondary (Koyu Mor)** | `#3700B3` | Gradientler |
| **Accent (Altın/Sarı)** | `#FFD700` | Logo "Mate" kısmı |
| **Surface** | `#F5F5F5` | Arka planlar |
| **Error** | `#FF5252` | Hata ve badge |

---

## 📝 Versiyon Geçmişi

### v1.1.0 (Aralık 2024) - Sosyal Güncellemesi
- ✅ **Takip/Takipçi sistemi**
  - Kullanıcıları takip etme
  - Takipçi/takip edilen listeleri
  - Takipten çıkma
- ✅ **Gelişmiş filtreleme**
  - Şehir filtresi ("Kendi şehrimdekileri bul" dahil)
  - RangeSlider ile yaş aralığı filtresi
  - "Birlikte Öğrenelim" modu
  - Toolbar'da filtre rozeti
- ✅ **Sosyal paylaşım**
  - Gönderi oluşturma (metin + fotoğraf)
  - Beğeni ve yorum sistemi
  - Profilde gönderiler sekmesi
- ✅ **Forum / Soru-Cevap**
  - Soru sorma ve cevaplama
  - Kategorilere göre filtreleme
  - En iyi cevap seçimi
- ✅ **Yetenek Quiz sistemi**
  - 10+ kategoride quiz
  - Zamanlı sorular (30 sn)
  - Skor ve seviye belirleme
- ✅ **UI İyileştirmeleri**
  - Merkezi toolbar tasarımı
  - Bottom sheet filtre arayüzü
  - Takipçi/takip edilen için ayrı aktiviteler

### v1.0.0 (Aralık 2024) - İlk Sürüm
- ✅ Kimlik doğrulama sistemi
- ✅ Profil oluşturma ve düzenleme
- ✅ Kategorili yetenek seçimi
- ✅ Akıllı eşleşme algoritması
- ✅ Keşfet sayfası (swipe)
- ✅ Eşleşme istekleri
- ✅ Gerçek zamanlı mesajlaşma
- ✅ Fotoğraf gönderme
- ✅ Engelleme sistemi
- ✅ Gizlilik ayarları
- ✅ Ayarlar sayfası
- ✅ Gizlilik politikası & Kullanım koşulları

---

## 🗺️ Yol Haritası (Roadmap)

### Yakında Gelecek Özellikler
- [ ] Push bildirimleri (FCM)
- [ ] Görüntülü/sesli arama
- [ ] Grup mesajlaşma
- [ ] Yetenek sertifikaları
- [ ] Kullanıcı doğrulama rozeti
- [ ] Karanlık mod
- [ ] Çoklu dil desteği

---

## 👥 Katkıda Bulunma

1. Bu repoyu fork edin
2. Feature branch oluşturun (`git checkout -b feature/AmazingFeature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add some AmazingFeature'`)
4. Branch'i push edin (`git push origin feature/AmazingFeature`)
5. Pull Request açın

---

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için `LICENSE` dosyasına bakın.

---

<p align="center">
  Made with 💜 by RiseMate Team
</p>

<p align="center">
  <b>Öğrenmeyi paylaş, büyümeyi keşfet!</b>
</p>
