package com.parthenios.risemate.ui.forum

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.parthenios.risemate.ui.chat.ImageViewerActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.parthenios.risemate.R
import com.parthenios.risemate.data.Answer
import com.parthenios.risemate.data.Question
import com.parthenios.risemate.databinding.ActivityQuestionDetailBinding
import com.parthenios.risemate.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionDetailBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var answerAdapter: AnswerAdapter
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var question: Question? = null
    private var answersListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        window.statusBarColor = resources.getColor(R.color.primary_color, theme)

        @Suppress("DEPRECATION")
        question = intent.getSerializableExtra("question") as? Question
        
        if (question == null) {
            Toast.makeText(this, "Soru bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupQuestionView()
        setupAnswersRecyclerView()
        setupAnswerInput()
        loadAnswers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Sadece soru sahibi silme butonunu görebilir
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null && currentUserId == question?.userId) {
            menuInflater.inflate(R.menu.menu_question_detail, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showDeleteConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Soruyu Sil")
            .setMessage("Bu soruyu silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
            .setPositiveButton("Sil") { _, _ ->
                deleteQuestion()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun deleteQuestion() {
        val questionId = question?.id ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Soruyu sil (soft delete)
                firestore.collection("questions").document(questionId)
                    .update("isDeleted", true).await()
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@QuestionDetailActivity, "Soru silindi", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@QuestionDetailActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupQuestionView() {
        val q = question ?: return
        
        binding.apply {
            tvUserName.text = q.userName
            tvTime.text = getTimeAgo(q.createdAt)
            tvSkillTag.text = q.skill
            tvTitle.text = q.title
            tvContent.text = q.content
            tvLikeCount.text = q.likeCount.toString()
            tvViewCount.text = "${q.viewCount} görüntülenme"
            
            // User photo
            loadUserPhoto(q.userPhotoUrl, ivUserPhoto)
            
            // Question image
            if (q.imageUrl.isNotEmpty() && q.imageUrl.startsWith("data:image")) {
                cardImage.visibility = View.VISIBLE
                loadBase64Image(q.imageUrl, ivQuestionImage)
                
                // Fotoğrafa tıklandığında yakınlaştır
                cardImage.setOnClickListener {
                    openImageViewer(q.imageUrl)
                }
            } else {
                cardImage.visibility = View.GONE
            }
            
            // Like state
            val currentUserId = auth.currentUser?.uid ?: ""
            val isLiked = q.likedBy.contains(currentUserId)
            updateLikeUI(isLiked)
            
            // Like click
            layoutLike.setOnClickListener { toggleQuestionLike() }
        }
    }

    private fun updateLikeUI(isLiked: Boolean) {
        binding.ivLike.setColorFilter(
            if (isLiked) getColor(R.color.error_color)
            else getColor(R.color.text_secondary)
        )
        binding.ivLike.setImageResource(
            if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
        )
    }

    private fun toggleQuestionLike() {
        val q = question ?: return
        val currentUserId = auth.currentUser?.uid ?: return
        val questionRef = firestore.collection("questions").document(q.id)
        
        val isLiked = q.likedBy.contains(currentUserId)
        
        if (isLiked) {
            questionRef.update(
                mapOf(
                    "likedBy" to FieldValue.arrayRemove(currentUserId),
                    "likeCount" to FieldValue.increment(-1)
                )
            ).addOnSuccessListener {
                val newLikedBy = q.likedBy.toMutableList().apply { remove(currentUserId) }
                question = q.copy(likedBy = newLikedBy, likeCount = q.likeCount - 1)
                binding.tvLikeCount.text = question!!.likeCount.toString()
                updateLikeUI(false)
            }
        } else {
            questionRef.update(
                mapOf(
                    "likedBy" to FieldValue.arrayUnion(currentUserId),
                    "likeCount" to FieldValue.increment(1)
                )
            ).addOnSuccessListener {
                val newLikedBy = q.likedBy.toMutableList().apply { add(currentUserId) }
                question = q.copy(likedBy = newLikedBy, likeCount = q.likeCount + 1)
                binding.tvLikeCount.text = question!!.likeCount.toString()
                updateLikeUI(true)
            }
        }
    }

    private fun setupAnswersRecyclerView() {
        val currentUserId = auth.currentUser?.uid ?: ""
        val questionOwnerId = question?.userId ?: ""
        
        answerAdapter = AnswerAdapter(
            currentUserId = currentUserId,
            questionOwnerId = questionOwnerId,
            onLikeClick = { answer -> toggleAnswerLike(answer) },
            onAcceptClick = { answer -> acceptAnswer(answer) }
        )

        binding.recyclerViewAnswers.apply {
            layoutManager = LinearLayoutManager(this@QuestionDetailActivity)
            adapter = answerAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadAnswers() {
        val questionId = question?.id ?: return
        
        answersListener = firestore.collection("answers")
            .whereEqualTo("questionId", questionId)
            .whereEqualTo("isDeleted", false)
            .orderBy("isAccepted", Query.Direction.DESCENDING)
            .orderBy("likeCount", Query.Direction.DESCENDING)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Fallback query without complex ordering
                    loadAnswersSimple()
                    return@addSnapshotListener
                }
                
                if (snapshot == null) return@addSnapshotListener
                
                processAnswersSnapshot(snapshot.documents)
            }
    }

    private fun loadAnswersSimple() {
        val questionId = question?.id ?: return
        
        answersListener?.remove()
        answersListener = firestore.collection("answers")
            .whereEqualTo("questionId", questionId)
            .whereEqualTo("isDeleted", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                processAnswersSnapshot(snapshot.documents)
            }
    }

    private fun processAnswersSnapshot(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        val answers = documents.mapNotNull { doc ->
            try {
                val data = doc.data ?: return@mapNotNull null
                
                @Suppress("UNCHECKED_CAST")
                Answer(
                    id = doc.id,
                    questionId = data["questionId"] as? String ?: "",
                    userId = data["userId"] as? String ?: "",
                    userName = data["userName"] as? String ?: "",
                    userPhotoUrl = data["userPhotoUrl"] as? String ?: "",
                    content = data["content"] as? String ?: "",
                    imageUrl = data["imageUrl"] as? String ?: "",
                    likeCount = (data["likeCount"] as? Long)?.toInt() ?: 0,
                    likedBy = data["likedBy"] as? List<String> ?: emptyList(),
                    isAccepted = data["isAccepted"] as? Boolean ?: false,
                    createdAt = doc.getTimestamp("createdAt")?.toDate() ?: Date(),
                    isDeleted = data["isDeleted"] as? Boolean ?: false
                )
            } catch (e: Exception) {
                null
            }
        }.sortedWith(
            compareByDescending<Answer> { it.isAccepted }
                .thenByDescending { it.likeCount }
                .thenBy { it.createdAt }
        )
        
        answerAdapter.submitList(answers)
        binding.tvAnswersHeader.text = "Cevaplar (${answers.size})"
        binding.layoutEmptyAnswers.visibility = if (answers.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupAnswerInput() {
        binding.btnSendAnswer.setOnClickListener {
            sendAnswer()
        }
    }

    private fun sendAnswer() {
        val content = binding.etAnswer.text.toString().trim()
        
        if (content.isEmpty()) {
            Toast.makeText(this, "Cevap boş olamaz", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (content.length < 10) {
            Toast.makeText(this, "Cevap en az 10 karakter olmalı", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.btnSendAnswer.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: throw Exception("Kullanıcı bulunamadı")
                val user = userPreferences.getUser() ?: throw Exception("Kullanıcı bilgisi bulunamadı")
                val questionId = question?.id ?: throw Exception("Soru bulunamadı")
                
                val answerData = hashMapOf(
                    "questionId" to questionId,
                    "userId" to currentUser.uid,
                    "userName" to "${user.firstName} ${user.lastName}",
                    "userPhotoUrl" to user.photoUrl,
                    "content" to content,
                    "imageUrl" to "",
                    "likeCount" to 0,
                    "likedBy" to emptyList<String>(),
                    "isAccepted" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "isDeleted" to false
                )
                
                // Add answer
                firestore.collection("answers").add(answerData).await()
                
                // Update answer count
                firestore.collection("questions").document(questionId)
                    .update("answerCount", FieldValue.increment(1))
                
                withContext(Dispatchers.Main) {
                    binding.etAnswer.text?.clear()
                    binding.btnSendAnswer.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@QuestionDetailActivity, "Cevap gönderildi", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnSendAnswer.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@QuestionDetailActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleAnswerLike(answer: Answer) {
        val currentUserId = auth.currentUser?.uid ?: return
        val answerRef = firestore.collection("answers").document(answer.id)
        
        val isLiked = answer.likedBy.contains(currentUserId)
        
        if (isLiked) {
            answerRef.update(
                mapOf(
                    "likedBy" to FieldValue.arrayRemove(currentUserId),
                    "likeCount" to FieldValue.increment(-1)
                )
            )
        } else {
            answerRef.update(
                mapOf(
                    "likedBy" to FieldValue.arrayUnion(currentUserId),
                    "likeCount" to FieldValue.increment(1)
                )
            )
        }
    }

    private fun acceptAnswer(answer: Answer) {
        val questionId = question?.id ?: return
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Mark answer as accepted
                firestore.collection("answers").document(answer.id)
                    .update("isAccepted", true).await()
                
                // Mark question as resolved
                firestore.collection("questions").document(questionId)
                    .update(
                        mapOf(
                            "isResolved" to true,
                            "acceptedAnswerId" to answer.id
                        )
                    ).await()
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@QuestionDetailActivity, "Cevap kabul edildi", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@QuestionDetailActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserPhoto(photoUrl: String, imageView: android.widget.ImageView) {
        if (photoUrl.startsWith("data:image")) {
            try {
                val base64Data = photoUrl.substringAfter("base64,")
                val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.default_avatar)
            }
        } else {
            imageView.setImageResource(R.drawable.default_avatar)
        }
    }

    private fun loadBase64Image(base64Url: String, imageView: android.widget.ImageView) {
        try {
            val base64Data = base64Url.substringAfter("base64,")
            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            imageView.visibility = View.GONE
        }
    }

    private fun openImageViewer(imageUrl: String) {
        val intent = Intent(this, ImageViewerActivity::class.java)
        intent.putExtra("image_url", imageUrl)
        startActivity(intent)
    }

    private fun getTimeAgo(date: Date): String {
        val now = Date()
        val diff = now.time - date.time
        
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        
        return when {
            seconds < 60 -> "Az önce"
            minutes < 60 -> "$minutes dakika önce"
            hours < 24 -> "$hours saat önce"
            days < 7 -> "$days gün önce"
            else -> SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(date)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        answersListener?.remove()
    }
}

