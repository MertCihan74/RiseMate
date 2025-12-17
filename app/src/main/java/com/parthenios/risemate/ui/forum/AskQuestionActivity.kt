package com.parthenios.risemate.ui.forum

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.databinding.ActivityAskQuestionBinding
import com.parthenios.risemate.utils.GroupInitializer
import com.parthenios.risemate.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class AskQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAskQuestionBinding
    private lateinit var userPreferences: UserPreferences
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var selectedImageUri: Uri? = null
    private var selectedSkill: String? = null
    
    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        window.statusBarColor = resources.getColor(R.color.primary_color, theme)

        setupToolbar()
        setupSkillDropdown()
        setupImagePicker()
        setupSubmitButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSkillDropdown() {
        val skills = GroupInitializer.getAllSkills()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, skills)
        
        binding.actvSkill.setAdapter(adapter)
        binding.actvSkill.setOnItemClickListener { _, _, position, _ ->
            selectedSkill = skills[position]
        }
    }

    private fun setupImagePicker() {
        binding.cardImage.setOnClickListener {
            if (selectedImageUri == null) {
                pickImage()
            }
        }
        
        binding.layoutAddImage.setOnClickListener {
            pickImage()
        }
        
        binding.btnRemoveImage.setOnClickListener {
            removeImage()
        }
    }

    private fun pickImage() {
        val intent = android.content.Intent(
            android.content.Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun removeImage() {
        selectedImageUri = null
        binding.ivPreview.visibility = View.GONE
        binding.btnRemoveImage.visibility = View.GONE
        binding.layoutAddImage.visibility = View.VISIBLE
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data?.data != null) {
            selectedImageUri = data.data
            binding.ivPreview.setImageURI(selectedImageUri)
            binding.ivPreview.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.VISIBLE
            binding.layoutAddImage.visibility = View.GONE
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            submitQuestion()
        }
    }

    private fun submitQuestion() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val skill = selectedSkill
        
        // Validation
        if (skill.isNullOrEmpty()) {
            binding.tilSkill.error = "Lütfen bir yetenek seçin"
            return
        }
        binding.tilSkill.error = null
        
        if (title.isEmpty()) {
            binding.tilTitle.error = "Başlık gerekli"
            return
        }
        if (title.length < 10) {
            binding.tilTitle.error = "Başlık en az 10 karakter olmalı"
            return
        }
        binding.tilTitle.error = null
        
        if (content.isEmpty()) {
            binding.tilContent.error = "Açıklama gerekli"
            return
        }
        if (content.length < 20) {
            binding.tilContent.error = "Açıklama en az 20 karakter olmalı"
            return
        }
        binding.tilContent.error = null
        
        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: throw Exception("Kullanıcı bulunamadı")
                val user = userPreferences.getUser() ?: throw Exception("Kullanıcı bilgisi bulunamadı")
                
                // Process image if exists
                val imageBase64 = selectedImageUri?.let { processImage(it) } ?: ""
                
                // Create question data
                val questionData = hashMapOf(
                    "userId" to currentUser.uid,
                    "userName" to "${user.firstName} ${user.lastName}",
                    "userPhotoUrl" to user.photoUrl,
                    "title" to title,
                    "content" to content,
                    "skill" to skill,
                    "category" to getSkillCategory(skill),
                    "imageUrl" to imageBase64,
                    "answerCount" to 0,
                    "viewCount" to 0,
                    "likeCount" to 0,
                    "likedBy" to emptyList<String>(),
                    "isResolved" to false,
                    "acceptedAnswerId" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "isDeleted" to false
                )
                
                // Save to Firestore
                firestore.collection("questions").add(questionData).await()
                
                // Send notifications to users who know this skill
                sendNotificationsToExperts(skill, title, currentUser.uid)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AskQuestionActivity, "Soru başarıyla gönderildi!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(this@AskQuestionActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processImage(uri: Uri): String {
        @Suppress("DEPRECATION")
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        
        // Resize
        val maxSize = 800
        val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        
        // Compress to Base64
        val baos = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val imageBytes = baos.toByteArray()
        
        return "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    private fun getSkillCategory(skill: String): String {
        // Kategorileri belirle
        val programmingSkills = listOf("Python", "Java", "JavaScript", "Kotlin", "TypeScript", "C++", "C#", "Swift", "Go", "Dart")
        val webSkills = listOf("HTML", "CSS", "React", "Node.js", "Frontend", "Backend", "Fullstack")
        val mobileSkills = listOf("Android", "iOS", "Flutter", "React Native")
        val musicSkills = listOf("Gitar", "Piyano", "Keman", "Bateri", "Şan", "Müzik Prodüksiyon")
        val sportSkills = listOf("Futbol", "Basketbol", "Fitness", "Yoga", "Boks", "Yüzme", "Tenis", "Voleybol", "Bisiklet", "Pilates", "Dövüş Sanatları", "Koşu")
        val languageSkills = listOf("İngilizce", "Almanca", "Fransızca", "İtalyanca", "İspanyolca", "Arapça", "Japonca", "Korece")
        val academicSkills = listOf("Matematik", "Fizik", "Kimya", "Biyoloji", "Tarih", "Psikoloji", "Ekonomi", "Edebiyat", "Mühendislik", "Tıp", "Hukuk")
        val artSkills = listOf("Fotoğrafçılık", "Grafik Tasarım", "Resim", "Video Edit")
        
        return when {
            skill in programmingSkills -> "Programlama"
            skill in webSkills -> "Web Geliştirme"
            skill in mobileSkills -> "Mobil Geliştirme"
            skill in musicSkills -> "Müzik"
            skill in sportSkills -> "Spor"
            skill in languageSkills -> "Dil"
            skill in academicSkills -> "Akademik"
            skill in artSkills -> "Sanat"
            else -> "Diğer"
        }
    }

    private suspend fun sendNotificationsToExperts(skill: String, questionTitle: String, askerId: String) {
        try {
            // Bu yeteneği bilen kullanıcıları bul
            val experts = firestore.collection("users")
                .whereArrayContains("knownSkills", skill)
                .get()
                .await()
            
            // Her uzman için bildirim oluştur
            experts.documents.forEach { doc ->
                val userId = doc.id
                
                // Kendisine bildirim gönderme
                if (userId == askerId) return@forEach
                
                val notificationData = hashMapOf(
                    "userId" to userId,
                    "type" to "new_question",
                    "title" to "$skill hakkında yeni soru",
                    "body" to questionTitle,
                    "skill" to skill,
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                
                firestore.collection("notifications").add(notificationData)
            }
        } catch (e: Exception) {
            // Bildirim hatası soruyu engellemesin
            e.printStackTrace()
        }
    }
}


