package com.parthenios.risemate.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.auth.AuthRepository
import com.parthenios.risemate.databinding.ActivitySettingsBinding
import com.parthenios.risemate.ui.auth.LoginActivity
import com.parthenios.risemate.utils.LocaleHelper
import com.parthenios.risemate.utils.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var authRepository: AuthRepository
    private lateinit var firestore: FirebaseFirestore

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.updateBaseContextLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        authRepository = AuthRepository()
        firestore = FirebaseFirestore.getInstance()

        setupToolbar()
        setupGeneralSettings()
        setupNotificationSettings()
        setupPrivacySettings()
        setupAccountSettings()
        setupAboutSettings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.settings)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // GENEL AYARLAR
    // ═══════════════════════════════════════════════════════════════════
    private fun setupGeneralSettings() {
        // Mevcut dili göster
        updateLanguageDisplay()

        // Dil seçimi
        binding.cardLanguage.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }

    private fun updateLanguageDisplay() {
        binding.tvCurrentLanguage.text = LocaleHelper.getLanguageDisplayName(this)
    }

    private fun showLanguageSelectionDialog() {
        val languages = LocaleHelper.getSupportedLanguages()
        val currentLanguage = LocaleHelper.getLanguage(this)

        val languageNames = languages.map { it.second }.toTypedArray()
        val languageCodes = languages.map { it.first }

        var selectedIndex = languageCodes.indexOf(currentLanguage)
        if (selectedIndex < 0) selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(languageNames, selectedIndex) { dialog, which ->
                val selectedLanguage = languageCodes[which]

                if (selectedLanguage != currentLanguage) {
                    // Dili değiştir
                    LocaleHelper.setLanguage(this, selectedLanguage)

                    // Kullanıcıya bilgi ver
                    Toast.makeText(this, getString(R.string.language_changed), Toast.LENGTH_SHORT).show()

                    // Activity'yi yeniden başlat
                    dialog.dismiss()
                    restartApp()
                } else {
                    dialog.dismiss()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun restartApp() {
        // Uygulamayı yeniden başlat - MainActivity'ye yönlendir
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent != null) {
            startActivity(intent)
        }
        finish()
        // Geçiş animasyonunu kaldır
        overridePendingTransition(0, 0)
    }

    // ═══════════════════════════════════════════════════════════════════
    // BİLDİRİM AYARLARI
    // ═══════════════════════════════════════════════════════════════════
    private fun setupNotificationSettings() {
        // Bildirimler açık/kapalı
        binding.switchNotifications.isChecked = userPreferences.areNotificationsEnabled()
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            userPreferences.setNotificationsEnabled(isChecked)
            binding.switchNotificationSound.isEnabled = isChecked
            binding.switchNotificationVibration.isEnabled = isChecked
        }

        // Bildirim sesi
        binding.switchNotificationSound.isChecked = userPreferences.isNotificationSoundEnabled()
        binding.switchNotificationSound.isEnabled = userPreferences.areNotificationsEnabled()
        binding.switchNotificationSound.setOnCheckedChangeListener { _, isChecked ->
            userPreferences.setNotificationSound(isChecked)
        }

        // Bildirim titreşimi
        binding.switchNotificationVibration.isChecked = userPreferences.isNotificationVibrationEnabled()
        binding.switchNotificationVibration.isEnabled = userPreferences.areNotificationsEnabled()
        binding.switchNotificationVibration.setOnCheckedChangeListener { _, isChecked ->
            userPreferences.setNotificationVibration(isChecked)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // GİZLİLİK AYARLARI
    // ═══════════════════════════════════════════════════════════════════
    private fun setupPrivacySettings() {
        // Çevrimiçi durumunu gizle
        binding.switchHideOnlineStatus.isChecked = userPreferences.isOnlineStatusHidden()
        binding.switchHideOnlineStatus.setOnCheckedChangeListener { _, isChecked ->
            userPreferences.setOnlineStatusHidden(isChecked)
            updatePrivacySettingsOnServer()
        }

        // Son görülmeyi gizle
        binding.switchHideLastSeen.isChecked = userPreferences.isLastSeenHidden()
        binding.switchHideLastSeen.setOnCheckedChangeListener { _, isChecked ->
            userPreferences.setLastSeenHidden(isChecked)
            updatePrivacySettingsOnServer()
        }

        // Engellenenler listesi
        binding.cardBlockedUsers.setOnClickListener {
            startActivity(Intent(this, BlockedUsersActivity::class.java))
        }

        // Engellenen sayısını göster
        updateBlockedCount()
    }

    private fun updateBlockedCount() {
        val user = userPreferences.getUser()
        val blockedCount = user?.blockedUsers?.size ?: 0
        binding.tvBlockedCount.text = if (blockedCount > 0) {
            "$blockedCount kişi engellendi"
        } else {
            "Engellenen kullanıcıları yönet"
        }
    }

    private fun updatePrivacySettingsOnServer() {
        val currentUser = authRepository.getCurrentUser() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("users")
                    .document(currentUser.uid)
                    .update(
                        mapOf(
                            "hideOnlineStatus" to userPreferences.isOnlineStatusHidden(),
                            "hideLastSeen" to userPreferences.isLastSeenHidden()
                        )
                    )
                    .await()
            } catch (e: Exception) {
                // Hata durumunda sessizce devam et
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // HESAP AYARLARI
    // ═══════════════════════════════════════════════════════════════════
    private fun setupAccountSettings() {
        binding.cardDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Hesabı Sil")
            .setMessage("Hesabınızı silmek istediğinize emin misiniz?\n\n• Tüm mesajlarınız silinecek\n• Tüm eşleşmeleriniz silinecek\n• Profil bilgileriniz silinecek\n\nBu işlem geri alınamaz!")
            .setPositiveButton("Hesabı Sil") { _, _ ->
                showFinalDeleteConfirmation()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showFinalDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Son Onay")
            .setMessage("Hesabınız kalıcı olarak silinecek. Devam etmek istiyor musunuz?")
            .setPositiveButton("Evet, Sil") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Vazgeç", null)
            .show()
    }

    private fun deleteAccount() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı giriş yapmamış", Toast.LENGTH_SHORT).show()
            return
        }

        // Google ile giriş yapan kullanıcıları kontrol et
        val isGoogleUser = currentUser.providerData.any { it.providerId == "google.com" }

        if (isGoogleUser) {
            // Google kullanıcısı - şifre sormadan doğrudan sil
            performAccountDeletionForGoogleUser()
        } else {
            // Email/password kullanıcısı - şifre iste
            showPasswordDialogForDeletion()
        }
    }

    private fun performAccountDeletionForGoogleUser() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = currentUser.uid

                // Tüm kullanıcı verilerini sil
                deleteAllUserData(userId)

                // Firebase Auth'dan sil (Google kullanıcısı için)
                currentUser.delete().await()

                // Local verileri temizle
                userPreferences.clearUser()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Hesabınız başarıyla silindi", Toast.LENGTH_SHORT).show()

                    // Login sayfasına yönlendir
                    val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    when {
                        e.message?.contains("requires recent authentication") == true ||
                                e.message?.contains("sensitive and requires") == true -> {
                            Toast.makeText(
                                this@SettingsActivity,
                                "Güvenlik nedeniyle yeniden Google ile giriş yapmanız gerekiyor. Lütfen çıkış yapıp tekrar giriş yapın.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                this@SettingsActivity,
                                "Hesap silinirken hata: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private suspend fun deleteAllUserData(userId: String) {
        // 1. Eşleşmeleri ve mesajları sil
        val matches1 = firestore.collection("matches")
            .whereEqualTo("user1Id", userId)
            .get().await()
        val matches2 = firestore.collection("matches")
            .whereEqualTo("user2Id", userId)
            .get().await()

        val allMatches = matches1.documents + matches2.documents
        for (match in allMatches) {
            // Mesajları sil
            val messages = firestore.collection("messages")
                .whereEqualTo("matchId", match.id)
                .get().await()
            for (msg in messages.documents) {
                try { firestore.collection("messages").document(msg.id).delete().await() } catch (_: Exception) {}
            }
            // Match'i sil
            try { firestore.collection("matches").document(match.id).delete().await() } catch (_: Exception) {}
        }

        // 2. Eşleşme isteklerini sil (gönderilen ve alınan)
        val sentRequests = firestore.collection("matchRequests")
            .whereEqualTo("fromUserId", userId)
            .get().await()
        val receivedRequests = firestore.collection("matchRequests")
            .whereEqualTo("toUserId", userId)
            .get().await()

        for (request in sentRequests.documents + receivedRequests.documents) {
            try { firestore.collection("matchRequests").document(request.id).delete().await() } catch (_: Exception) {}
        }

        // 3. Gönderileri sil (posts)
        val posts = firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .get().await()
        for (post in posts.documents) {
            try { firestore.collection("posts").document(post.id).delete().await() } catch (_: Exception) {}
        }

        // 4. Grup üyeliklerini sil
        val groupMemberships = firestore.collection("groupMembers")
            .whereEqualTo("userId", userId)
            .get().await()
        for (membership in groupMemberships.documents) {
            try {
                // Grup üye sayısını azalt
                val groupId = membership.getString("groupId")
                if (groupId != null) {
                    firestore.collection("groups").document(groupId)
                        .update("memberCount", com.google.firebase.firestore.FieldValue.increment(-1))
                }
                firestore.collection("groupMembers").document(membership.id).delete().await()
            } catch (_: Exception) {}
        }

        // 5. Grup mesajlarını sil
        val groupMessages = firestore.collection("groupMessages")
            .whereEqualTo("senderId", userId)
            .get().await()
        for (msg in groupMessages.documents) {
            try { firestore.collection("groupMessages").document(msg.id).delete().await() } catch (_: Exception) {}
        }

        // 6. Forum sorularını sil (soft delete - isDeleted = true)
        val questions = firestore.collection("questions")
            .whereEqualTo("userId", userId)
            .get().await()
        for (question in questions.documents) {
            try {
                firestore.collection("questions").document(question.id)
                    .update("isDeleted", true, "content", "[Silinen Kullanıcı]")
                    .await()
            } catch (_: Exception) {}
        }

        // 7. Aramaları sil (calls)
        val callerCalls = firestore.collection("calls")
            .whereEqualTo("callerId", userId)
            .get().await()
        val calleeCalls = firestore.collection("calls")
            .whereEqualTo("calleeId", userId)
            .get().await()
        for (call in callerCalls.documents + calleeCalls.documents) {
            try { firestore.collection("calls").document(call.id).delete().await() } catch (_: Exception) {}
        }

        // 8. Raporları sil (kullanıcının gönderdiği raporlar)
        val reports = firestore.collection("reports")
            .whereEqualTo("reporterId", userId)
            .get().await()
        for (report in reports.documents) {
            try { firestore.collection("reports").document(report.id).delete().await() } catch (_: Exception) {}
        }

        // 9. Takipçi/Takip listelerinden çıkar
        val userDoc = firestore.collection("users").document(userId).get().await()
        val followers = userDoc.get("followers") as? List<String> ?: emptyList()
        val following = userDoc.get("following") as? List<String> ?: emptyList()

        // Takipçilerin following listesinden bu kullanıcıyı çıkar
        for (followerId in followers) {
            try {
                firestore.collection("users").document(followerId)
                    .update("following", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                    .await()
            } catch (_: Exception) {}
        }

        // Takip edilenlerin followers listesinden bu kullanıcıyı çıkar
        for (followingId in following) {
            try {
                firestore.collection("users").document(followingId)
                    .update("followers", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                    .await()
            } catch (_: Exception) {}
        }

        // 10. Diğer kullanıcıların beğeni listelerinden bu kullanıcıyı çıkar
        // (Posts'taki likes alanları)
        val allPosts = firestore.collection("posts").get().await()
        for (post in allPosts.documents) {
            val likes = post.get("likes") as? List<String> ?: emptyList()
            if (userId in likes) {
                try {
                    firestore.collection("posts").document(post.id)
                        .update(
                            "likes", com.google.firebase.firestore.FieldValue.arrayRemove(userId),
                            "likeCount", com.google.firebase.firestore.FieldValue.increment(-1)
                        )
                        .await()
                } catch (_: Exception) {}
            }
        }

        // 11. Yorumlardaki beğenilerden de çıkar
        for (post in allPosts.documents) {
            val comments = post.get("comments") as? List<Map<String, Any>> ?: emptyList()
            var needsUpdate = false
            val updatedComments = comments.map { comment ->
                val commentLikes = (comment["likes"] as? List<String>)?.toMutableList() ?: mutableListOf()
                if (userId in commentLikes) {
                    needsUpdate = true
                    commentLikes.remove(userId)
                    comment.toMutableMap().apply {
                        this["likes"] = commentLikes
                        this["likeCount"] = commentLikes.size
                    }
                } else {
                    comment
                }
            }
            if (needsUpdate) {
                try {
                    firestore.collection("posts").document(post.id)
                        .update("comments", updatedComments)
                        .await()
                } catch (_: Exception) {}
            }
        }

        // 12. Kullanıcı dokümanını sil
        firestore.collection("users").document(userId).delete().await()
    }

    private fun showPasswordDialogForDeletion() {
        val input = EditText(this)
        input.hint = "Şifrenizi girin"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 16, 32, 16)
        layout.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Şifre Doğrulama")
            .setMessage("Hesap silme işlemi için şifrenizi girin:")
            .setView(layout)
            .setPositiveButton("Doğrula") { _, _ ->
                val password = input.text.toString().trim()
                if (password.isEmpty()) {
                    Toast.makeText(this, "Şifre gerekli", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                performAccountDeletion(password)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun performAccountDeletion(password: String) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = currentUser.uid

                // Önce yeniden kimlik doğrulaması yap
                val userPrefs = userPreferences.getUser()
                if (userPrefs?.email?.isNotEmpty() == true) {
                    val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(userPrefs.email, password)
                    currentUser.reauthenticate(credential).await()
                }

                // Tüm kullanıcı verilerini sil
                deleteAllUserData(userId)

                // Firebase Auth'dan sil
                currentUser.delete().await()

                // Local verileri temizle
                userPreferences.clearUser()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Hesabınız başarıyla silindi", Toast.LENGTH_SHORT).show()

                    // Login sayfasına yönlendir
                    val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    when {
                        e.message?.contains("wrong-password") == true ||
                                e.message?.contains("invalid-credential") == true -> {
                            Toast.makeText(
                                this@SettingsActivity,
                                "Şifre yanlış. Lütfen doğru şifrenizi girin.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        e.message?.contains("requires recent authentication") == true ||
                                e.message?.contains("sensitive and requires") == true -> {
                            Toast.makeText(
                                this@SettingsActivity,
                                "Güvenlik nedeniyle şifre doğrulama gerekiyor.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                this@SettingsActivity,
                                "Hesap silinirken hata: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // HAKKINDA AYARLARI
    // ═══════════════════════════════════════════════════════════════════
    private fun setupAboutSettings() {
        // Versiyon
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            binding.tvAppVersion.text = packageInfo.versionName
        } catch (_: Exception) {
            binding.tvAppVersion.text = "1.0.0"
        }


        // Gizlilik Politikası
        binding.cardPrivacyPolicy.setOnClickListener {
            val intent = Intent(this, PolicyActivity::class.java)
            intent.putExtra(PolicyActivity.EXTRA_POLICY_TYPE, PolicyActivity.TYPE_PRIVACY)
            startActivity(intent)
        }

        // Kullanım Koşulları
        binding.cardTermsOfService.setOnClickListener {
            val intent = Intent(this, PolicyActivity::class.java)
            intent.putExtra(PolicyActivity.EXTRA_POLICY_TYPE, PolicyActivity.TYPE_TERMS)
            startActivity(intent)
        }

        // Destek
        binding.cardSupport.setOnClickListener {
            sendSupportEmail()
        }
    }

    private fun sendSupportEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("risemateapp@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "RiseMate Destek Talebi")
                putExtra(Intent.EXTRA_TEXT, "\n\n---\nUygulama Versiyonu: ${binding.tvAppVersion.text}\nCihaz: ${android.os.Build.MODEL}\nAndroid: ${android.os.Build.VERSION.RELEASE}")
            }
            startActivity(Intent.createChooser(intent, "E-posta Gönder"))
        } catch (e: Exception) {
            Toast.makeText(this, "E-posta uygulaması bulunamadı", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Engellenenler listesinden döndüğünde sayıyı güncelle
        updateBlockedCount()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
