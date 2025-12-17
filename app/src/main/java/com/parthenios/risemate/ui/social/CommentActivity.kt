package com.parthenios.risemate.ui.social

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.data.Comment
import com.parthenios.risemate.data.Post
import com.parthenios.risemate.databinding.ActivityCommentBinding
import com.parthenios.risemate.utils.LocaleHelper
import com.parthenios.risemate.utils.UserPreferences
import java.text.SimpleDateFormat
import java.util.*

class CommentActivity : AppCompatActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.updateBaseContextLocale(newBase))
    }

    private lateinit var binding: ActivityCommentBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var post: Post
    private lateinit var commentsAdapter: CommentsAdapter
    private var replyingToComment: Comment? = null // Yanıt verilen yorum

    companion object {
        const val EXTRA_POST = "post"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Klavye açıldığında layout'u resize et ve input görünsün
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )
        
        enableEdgeToEdge()

        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)

        // Sistem bar renkleri
        window.statusBarColor = resources.getColor(R.color.primary_color, theme)
        window.navigationBarColor = resources.getColor(R.color.white, theme)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            
            // Klavye açıkken bottom padding'i ayarla
            val bottomPadding = if (ime.bottom > 0) ime.bottom else systemBars.bottom
            v.setPadding(systemBars.left, 0, systemBars.right, bottomPadding)
            
            insets
        }

        // Post verisini al
        post = intent.getSerializableExtra(EXTRA_POST) as? Post ?: run {
            Toast.makeText(this, "Gönderi bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupPostView()
        setupCurrentUserPhoto()
        setupCommentsList()
        setupCommentInput()
        loadComments()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        updateToolbarCommentCount(post.comments.size)
    }

    private fun updateToolbarCommentCount(count: Int) {
        binding.tvCommentCountToolbar.text = when {
            count == 0 -> "Henüz yorum yok"
            count == 1 -> "1 yorum"
            else -> "$count yorum"
        }
    }

    private fun setupCurrentUserPhoto() {
        val user = userPreferences.getUser()
        if (user != null && user.photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(user.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(binding.ivCurrentUserPhoto)
        } else {
            binding.ivCurrentUserPhoto.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun setupPostView() {
        binding.apply {
            tvUserName.text = post.userName
            tvTimestamp.text = formatTimestamp(post.timestamp)

            if (post.userPhotoUrl.isNotEmpty()) {
                Glide.with(this@CommentActivity)
                    .load(post.userPhotoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(ivUserPhoto)
            } else {
                ivUserPhoto.setImageResource(R.drawable.ic_profile)
            }

            tvContent.text = post.content

            if (!post.imageUrl.isNullOrEmpty()) {
                cardPostImage.visibility = View.VISIBLE
                Glide.with(this@CommentActivity)
                    .load(post.imageUrl)
                    .placeholder(R.drawable.ic_image)
                    .into(ivPostImage)
            } else {
                cardPostImage.visibility = View.GONE
            }

            updateLikeUI()

            layoutLikeButton.setOnClickListener {
                togglePostLike()
            }
            
            tvCommentCount.text = post.comments.size.toString()
        }
    }

    private fun updateLikeUI() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val isLiked = if (currentUserId != null) post.likes.contains(currentUserId) else false

        binding.tvLikeIcon.text = if (isLiked) "❤️" else "🤍"
        binding.tvLikeCount.text = post.likeCount.toString()
    }

    private fun togglePostLike() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val postRef = firestore.collection("posts").document(post.id)

        val isLiked = post.likes.contains(currentUserId)
        val newLikes = post.likes.toMutableList()
        var newLikeCount = post.likeCount

        if (isLiked) {
            newLikes.remove(currentUserId)
            newLikeCount = (newLikeCount - 1).coerceAtLeast(0)
        } else {
            if (!newLikes.contains(currentUserId)) {
                newLikes.add(currentUserId)
                newLikeCount++
            }
        }

        // Optimistic update
        post = post.copy(likes = newLikes, likeCount = newLikeCount)
        updateLikeUI()

        if (isLiked) {
            postRef.update(
                "likes", com.google.firebase.firestore.FieldValue.arrayRemove(currentUserId),
                "likeCount", com.google.firebase.firestore.FieldValue.increment(-1)
            ).addOnFailureListener {
                // Revert on failure
                Toast.makeText(this, "Hata oluştu", Toast.LENGTH_SHORT).show()
                post = post.copy(
                    likes = (post.likes + currentUserId).toMutableList(),
                    likeCount = post.likeCount + 1
                )
                updateLikeUI()
            }
        } else {
            postRef.update(
                "likes", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId),
                "likeCount", com.google.firebase.firestore.FieldValue.increment(1)
            ).addOnSuccessListener {
                Toast.makeText(this, "Beğenildi! ❤️", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                // Revert on failure
                Toast.makeText(this, "Hata oluştu", Toast.LENGTH_SHORT).show()
                val revertedLikes = post.likes.toMutableList().apply { remove(currentUserId) }
                post = post.copy(
                    likes = revertedLikes,
                    likeCount = (post.likeCount - 1).coerceAtLeast(0)
                )
                updateLikeUI()
            }
        }
    }

    private fun setupCommentsList() {
        commentsAdapter = CommentsAdapter(
            onLikeClick = { comment ->
                toggleCommentLike(comment)
            },
            onReplyClick = { comment ->
                startReplyingTo(comment)
            },
            onMoreClick = { comment ->
                showCommentOptionsDialog(comment)
            },
            onReplyLikeClick = { parentComment, reply ->
                toggleReplyLike(parentComment, reply)
            }
        )
        binding.recyclerViewComments.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity)
            adapter = commentsAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun startReplyingTo(comment: Comment) {
        replyingToComment = comment
        val replyText = "@${comment.userName} "
        binding.etComment.setText(replyText)
        binding.etComment.setSelection(replyText.length)
        binding.etComment.requestFocus()
        
        // Hint'i güncelle
        binding.etComment.hint = "${comment.userName} kişisine yanıt yaz..."
        
        // Klavyeyi aç
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(binding.etComment, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        
        // Scroll to comment input
        binding.main.post {
            binding.etComment.parent?.requestChildFocus(binding.etComment, binding.etComment)
        }
    }

    private fun toggleCommentLike(comment: Comment) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        
        firestore.collection("posts").document(post.id)
            .get()
            .addOnSuccessListener { document ->
                val postData = document.toObject(Post::class.java) ?: return@addOnSuccessListener
                
                val updatedComments = postData.comments.map { c ->
                    if (c.id == comment.id) {
                        val newLikes = c.likes.toMutableList()
                        val isCurrentlyLiked = newLikes.contains(currentUserId)
                        
                        if (isCurrentlyLiked) {
                            newLikes.remove(currentUserId)
                        } else {
                            newLikes.add(currentUserId)
                        }
                        
                        c.copy(
                            likes = newLikes,
                            likeCount = newLikes.size
                        )
                    } else {
                        c
                    }
                }
                
                // Firestore'a kaydet
                val commentsData = updatedComments.map { c ->
                    hashMapOf(
                        "id" to c.id,
                        "userId" to c.userId,
                        "userName" to c.userName,
                        "userPhotoUrl" to c.userPhotoUrl,
                        "content" to c.content,
                        "timestamp" to c.timestamp,
                        "parentId" to c.parentId,
                        "likes" to c.likes,
                        "likeCount" to c.likeCount,
                        "replies" to c.replies.map { r ->
                            hashMapOf(
                                "id" to r.id,
                                "userId" to r.userId,
                                "userName" to r.userName,
                                "userPhotoUrl" to r.userPhotoUrl,
                                "content" to r.content,
                                "timestamp" to r.timestamp,
                                "parentId" to r.parentId,
                                "likes" to r.likes,
                                "likeCount" to r.likeCount,
                                "replies" to emptyList<Any>()
                            )
                        }
                    )
                }
                
                firestore.collection("posts").document(post.id)
                    .update("comments", commentsData)
                    .addOnSuccessListener {
                        loadComments()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Beğeni kaydedilemedi", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun toggleReplyLike(parentComment: Comment, reply: Comment) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        
        firestore.collection("posts").document(post.id)
            .get()
            .addOnSuccessListener { document ->
                val postData = document.toObject(Post::class.java) ?: return@addOnSuccessListener
                
                val updatedComments = postData.comments.map { c ->
                    if (c.id == parentComment.id) {
                        val updatedReplies = c.replies.map { r ->
                            if (r.id == reply.id) {
                                val newLikes = r.likes.toMutableList()
                                val isCurrentlyLiked = newLikes.contains(currentUserId)
                                
                                if (isCurrentlyLiked) {
                                    newLikes.remove(currentUserId)
                                } else {
                                    newLikes.add(currentUserId)
                                }
                                
                                r.copy(likes = newLikes, likeCount = newLikes.size)
                            } else {
                                r
                            }
                        }
                        c.copy(replies = updatedReplies.toMutableList())
                    } else {
                        c
                    }
                }
                
                saveCommentsToFirestore(updatedComments)
            }
    }

    private fun saveCommentsToFirestore(comments: List<Comment>) {
        val firestore = FirebaseFirestore.getInstance()
        
        val commentsData = comments.map { c ->
            hashMapOf(
                "id" to c.id,
                "userId" to c.userId,
                "userName" to c.userName,
                "userPhotoUrl" to c.userPhotoUrl,
                "content" to c.content,
                "timestamp" to c.timestamp,
                "parentId" to c.parentId,
                "likes" to c.likes,
                "likeCount" to c.likeCount,
                "replies" to c.replies.map { r ->
                    hashMapOf(
                        "id" to r.id,
                        "userId" to r.userId,
                        "userName" to r.userName,
                        "userPhotoUrl" to r.userPhotoUrl,
                        "content" to r.content,
                        "timestamp" to r.timestamp,
                        "parentId" to r.parentId,
                        "likes" to r.likes,
                        "likeCount" to r.likeCount,
                        "replies" to emptyList<Any>()
                    )
                }
            )
        }
        
        firestore.collection("posts").document(post.id)
            .update("comments", commentsData)
            .addOnSuccessListener {
                loadComments()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Değişiklikler kaydedilemedi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCommentOptionsDialog(comment: Comment) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val isOwner = comment.userId == currentUserId

        val options = if (isOwner) {
            arrayOf("Yorumu Sil", "İptal")
        } else {
            arrayOf("Bildir", "İptal")
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Seçenekler")
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "Yorumu Sil" -> deleteComment(comment)
                    "Bildir" -> Toast.makeText(this, "Yorum bildirildi", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteComment(comment: Comment) {
        val firestore = FirebaseFirestore.getInstance()
        
        firestore.collection("posts").document(post.id)
            .get()
            .addOnSuccessListener { document ->
                val postData = document.toObject(Post::class.java)
                val updatedComments = postData?.comments?.filter { it.id != comment.id } ?: emptyList()
                
                saveCommentsToFirestore(updatedComments)
                Toast.makeText(this, "Yorum silindi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupCommentInput() {
        binding.btnSendComment.setOnClickListener {
            val commentText = binding.etComment.text?.toString()?.trim() ?: ""
            if (commentText.isNotEmpty()) {
                if (replyingToComment != null) {
                    addReply(commentText, replyingToComment!!)
                } else {
                    addComment(commentText)
                }
            } else {
                Toast.makeText(this, "Lütfen bir yorum yazın", Toast.LENGTH_SHORT).show()
            }
        }

        // Input focus listener - yanıt modundan çıkış
        binding.etComment.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.etComment.text.isNullOrEmpty()) {
                clearReplyMode()
            }
        }

        binding.btnCancel.setOnClickListener {
            binding.etComment.text?.clear()
            clearReplyMode()
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.etComment.windowToken, 0)
        }
    }

    private fun clearReplyMode() {
        replyingToComment = null
        binding.etComment.hint = "Yorum yaz..."
    }

    private fun addComment(commentText: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Oturum açmalısınız", Toast.LENGTH_SHORT).show()
            return
        }

        val user = userPreferences.getUser()
        if (user == null) {
            Toast.makeText(this, "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSendComment.isEnabled = false
        binding.etComment.isEnabled = false

        val firestore = FirebaseFirestore.getInstance()
        val commentData = hashMapOf(
            "id" to firestore.collection("posts").document().id,
            "userId" to currentUserId,
            "userName" to "${user.firstName} ${user.lastName}",
            "userPhotoUrl" to (user.photoUrl ?: ""),
            "content" to commentText,
            "timestamp" to System.currentTimeMillis(),
            "parentId" to null,
            "likes" to mutableListOf<String>(),
            "likeCount" to 0,
            "replies" to mutableListOf<Any>()
        )

        firestore.collection("posts").document(post.id)
            .update("comments", com.google.firebase.firestore.FieldValue.arrayUnion(commentData))
            .addOnSuccessListener {
                binding.etComment.text?.clear()
                binding.btnSendComment.isEnabled = true
                binding.etComment.isEnabled = true
                clearReplyMode()
                Toast.makeText(this, "Yorum eklendi! 💬", Toast.LENGTH_SHORT).show()
                loadComments()
            }
            .addOnFailureListener {
                binding.btnSendComment.isEnabled = true
                binding.etComment.isEnabled = true
                Toast.makeText(this, "Yorum eklenirken hata oluştu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addReply(replyText: String, parentComment: Comment) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Oturum açmalısınız", Toast.LENGTH_SHORT).show()
            return
        }

        val user = userPreferences.getUser()
        if (user == null) {
            Toast.makeText(this, "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSendComment.isEnabled = false
        binding.etComment.isEnabled = false

        val firestore = FirebaseFirestore.getInstance()
        
        firestore.collection("posts").document(post.id)
            .get()
            .addOnSuccessListener { document ->
                val postData = document.toObject(Post::class.java) ?: return@addOnSuccessListener
                
                val newReply = Comment(
                    id = firestore.collection("posts").document().id,
                    userId = currentUserId,
                    userName = "${user.firstName} ${user.lastName}",
                    userPhotoUrl = user.photoUrl ?: "",
                    content = replyText,
                    timestamp = System.currentTimeMillis(),
                    parentId = parentComment.id,
                    likes = mutableListOf(),
                    likeCount = 0,
                    replies = mutableListOf()
                )
                
                val updatedComments = postData.comments.map { c ->
                    if (c.id == parentComment.id) {
                        val updatedReplies = c.replies.toMutableList()
                        updatedReplies.add(newReply)
                        c.copy(replies = updatedReplies)
                    } else {
                        c
                    }
                }
                
                saveCommentsToFirestore(updatedComments)
                
                binding.etComment.text?.clear()
                binding.btnSendComment.isEnabled = true
                binding.etComment.isEnabled = true
                clearReplyMode()
                Toast.makeText(this, "Yanıt eklendi! 💬", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                binding.btnSendComment.isEnabled = true
                binding.etComment.isEnabled = true
                Toast.makeText(this, "Yanıt eklenirken hata oluştu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadComments() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("posts").document(post.id)
            .get()
            .addOnSuccessListener { document ->
                val postData = document.toObject(Post::class.java)?.copy(id = document.id)
                
                // Sadece ana yorumları al (parentId null olanlar)
                val mainComments = postData?.comments?.filter { it.parentId == null } ?: emptyList()
                
                // Yorumları timestamp'e göre sırala
                val sortedComments = mainComments.sortedByDescending { it.timestamp }
                
                commentsAdapter.submitList(sortedComments)
                
                // Toplam yorum sayısı (ana yorumlar + yanıtlar)
                val totalComments = mainComments.sumOf { 1 + it.replies.size }
                
                updateToolbarCommentCount(totalComments)
                binding.tvCommentCount.text = "$totalComments yorum"

                if (sortedComments.isEmpty()) {
                    binding.layoutNoComments.visibility = View.VISIBLE
                    binding.recyclerViewComments.visibility = View.GONE
                } else {
                    binding.layoutNoComments.visibility = View.GONE
                    binding.recyclerViewComments.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Yorumlar yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Şimdi"
            diff < 3600000 -> "${diff / 60000} dakika önce"
            diff < 86400000 -> "${diff / 3600000} saat önce"
            diff < 604800000 -> "${diff / 86400000} gün önce"
            else -> {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }
}
