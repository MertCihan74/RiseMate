package com.parthenios.risemate.ui.social

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.databinding.ActivityCreatePostBinding
import com.parthenios.risemate.utils.UserPreferences as RiseMateUserPreferences
import com.parthenios.risemate.utils.UserPreferences

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var userPreferences: UserPreferences
    private var selectedImageUri: Uri? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivSelectedImage.visibility = android.view.View.VISIBLE
            Glide.with(this)
                .load(it)
                .into(binding.ivSelectedImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        setupToolbar()
        setupClickListeners()
        loadUserInfo()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnAddImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnRemoveImage.setOnClickListener {
            selectedImageUri = null
            binding.ivSelectedImage.visibility = android.view.View.GONE
        }

        binding.btnPost.setOnClickListener {
            createPost()
        }
    }

    private fun loadUserInfo() {
        val user = userPreferences.getUser()
        user?.let {
            binding.tvUserName.text = "${it.firstName} ${it.lastName}"

            if (it.photoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(it.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.ivUserPhoto)
            }
        }
    }

    private fun createPost() {
        val content = binding.etContent.text.toString().trim()

        if (content.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Gönderi içeriği veya resim ekleyin", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Oturum açmalısınız", Toast.LENGTH_SHORT).show()
            return
        }

        val userPreferences = RiseMateUserPreferences(this)
        val user = userPreferences.getUser()
        if (user == null) {
            Toast.makeText(this, "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnPost.isEnabled = false
        binding.btnPost.text = "Paylaşılıyor..."

        if (selectedImageUri != null) {
            // Fotoğraf varsa önce Storage'a yükle
            uploadImageAndCreatePost(content, currentUser, user)
        } else {
            // Fotoğraf yoksa direkt post oluştur
            createPost(content, "", currentUser, user)
        }
    }

    private fun uploadImageAndCreatePost(content: String, currentUser: com.google.firebase.auth.FirebaseUser, user: com.parthenios.risemate.data.User) {
        try {
            // Firebase Storage yerine Base64 encoding kullan
            // Bu daha basit ve güvenilir

            val contentResolver = contentResolver
            val mimeType = contentResolver.getType(selectedImageUri!!)

            if (mimeType == null || !mimeType.startsWith("image/")) {
                Toast.makeText(this, "Lütfen geçerli bir resim dosyası seçin", Toast.LENGTH_SHORT).show()
                binding.btnPost.isEnabled = true
                binding.btnPost.text = "Paylaş"
                return
            }

            // Dosya boyutunu kontrol et (max 5MB Base64 için)
            val fileDescriptor = contentResolver.openFileDescriptor(selectedImageUri!!, "r")
            val fileSize = fileDescriptor?.statSize ?: 0
            fileDescriptor?.close()

            if (fileSize > 5 * 1024 * 1024) { // 5MB
                Toast.makeText(this, "Resim dosyası çok büyük (max 5MB)", Toast.LENGTH_SHORT).show()
                binding.btnPost.isEnabled = true
                binding.btnPost.text = "Paylaş"
                return
            }

            // Resmi Base64'e çevir
            binding.btnPost.text = "İşleniyor..."
            convertImageToBase64(selectedImageUri!!, content, currentUser, user)

        } catch (e: Exception) {
            Toast.makeText(this, "Fotoğraf işlenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.btnPost.isEnabled = true
            binding.btnPost.text = "Paylaş"
        }
    }

    private fun convertImageToBase64(uri: android.net.Uri, content: String, currentUser: com.google.firebase.auth.FirebaseUser, user: com.parthenios.risemate.data.User) {
        try {
            // Resmi compress et (mesajlaşma yaklaşımı gibi)
            val compressedBytes = compressImage(uri)
            if (compressedBytes != null) {
                val base64Image = android.util.Base64.encodeToString(compressedBytes, android.util.Base64.DEFAULT)
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val base64WithPrefix = "data:$mimeType;base64,$base64Image"

                createPost(content, base64WithPrefix, currentUser, user)
            } else {
                throw Exception("Resim sıkıştırılamadı")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Resim dönüştürülürken hata: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.btnPost.isEnabled = true
            binding.btnPost.text = "Paylaş"
        }
    }

    private fun compressImage(uri: android.net.Uri): ByteArray? {
        return try {
            // Bitmap'e çevir ve boyutlandır
            val bitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)

            // Maksimum boyut 1024x1024
            val maxDimension = 1024
            var width = bitmap.width
            var height = bitmap.height

            if (width > height) {
                if (width > maxDimension) {
                    height = (height * maxDimension.toFloat() / width).toInt()
                    width = maxDimension
                }
            } else {
                if (height > maxDimension) {
                    width = (width * maxDimension.toFloat() / height).toInt()
                    height = maxDimension
                }
            }

            // Yeniden boyutlandır
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)
            bitmap.recycle()

            // JPEG'e çevir ve compress et
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            scaledBitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    private fun createPost(content: String, imageUrl: String, currentUser: com.google.firebase.auth.FirebaseUser, user: com.parthenios.risemate.data.User) {
        // Post verisini hazırla
        val postData = hashMapOf(
            "id" to "",
            "userId" to currentUser.uid,
            "userName" to "${user.firstName} ${user.lastName}",
            "userPhotoUrl" to user.photoUrl,
            "content" to content,
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis(),
            "likes" to emptyList<String>(),
            "likeCount" to 0,
            "comments" to emptyList<Map<String, Any>>()
        )

        // Firebase'e kaydet
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("posts")
            .add(postData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Gönderi paylaşıldı!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gönderi paylaşılırken hata oluştu", Toast.LENGTH_SHORT).show()
                binding.btnPost.isEnabled = true
                binding.btnPost.text = "Paylaş"
            }
    }
}
