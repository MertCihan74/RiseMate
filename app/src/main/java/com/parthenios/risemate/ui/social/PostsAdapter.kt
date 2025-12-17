package com.parthenios.risemate.ui.social

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.parthenios.risemate.R
import com.parthenios.risemate.data.Post
import com.parthenios.risemate.databinding.ItemPostBinding
import java.text.SimpleDateFormat
import java.util.*

class PostsAdapter(
    private val onLikeClick: (Post) -> Unit = {},
    private val onCommentClick: (Post) -> Unit = {},
    private val onShareClick: (Post) -> Unit = {},
    private val onDeleteClick: (Post) -> Unit = {},
    private val onUserClick: (Post) -> Unit = {}
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding, onLikeClick, onCommentClick, onShareClick, onDeleteClick, onUserClick)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(
        private val binding: ItemPostBinding,
        private val onLikeClick: (Post) -> Unit,
        private val onCommentClick: (Post) -> Unit,
        private val onShareClick: (Post) -> Unit,
        private val onDeleteClick: (Post) -> Unit,
        private val onUserClick: (Post) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                // User info
                tvUserName.text = post.userName
                tvTimestamp.text = formatTimestamp(post.timestamp)

                // User photo - Tek seferlik get() ile kullanıcının profil resmini al
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                firestore.collection("users")
                    .document(post.userId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        // Activity destroy edilmişse işlem yapma
                        val context = binding.root.context
                        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
                            return@addOnSuccessListener
                        }
                        
                        if (snapshot != null && snapshot.exists()) {
                            val currentPhotoUrl = snapshot.getString("photoUrl") ?: ""

                            if (currentPhotoUrl.isNotEmpty()) {
                                try {
                                    Glide.with(context)
                                        .load(currentPhotoUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .into(ivUserPhoto)
                                } catch (e: Exception) {
                                    // Glide hatası - activity destroy edilmiş olabilir
                                    ivUserPhoto.setImageResource(R.drawable.ic_profile)
                                }
                            } else {
                                ivUserPhoto.setImageResource(R.drawable.ic_profile)
                            }
                        } else {
                            ivUserPhoto.setImageResource(R.drawable.ic_profile)
                        }
                    }
                    .addOnFailureListener {
                        ivUserPhoto.setImageResource(R.drawable.ic_profile)
                    }

                // Post content
                tvContent.text = post.content

                // Handle read more functionality
                val isTextLong = post.content.length > 150 // Approximate character limit for 4 lines
                if (isTextLong) {
                    tvContent.maxLines = 4
                    btnReadMore.visibility = android.view.View.VISIBLE
                    btnReadMore.setOnClickListener {
                        if (tvContent.maxLines == 4) {
                            tvContent.maxLines = Int.MAX_VALUE
                            btnReadMore.text = "Küçült"
                        } else {
                            tvContent.maxLines = 4
                            btnReadMore.text = "Devamını Gör"
                        }
                    }
                } else {
                    tvContent.maxLines = Int.MAX_VALUE
                    btnReadMore.visibility = android.view.View.GONE
                }

                // User click -> profile aç
                layoutUserHeader.setOnClickListener { onUserClick(post) }

                // Get current user ID
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

                // Likes & comments
                val isLiked = currentUserId != null && post.likes.contains(currentUserId)
                tvLikeCount.text = post.likeCount.toString()
                tvCommentCount.text = post.comments.size.toString()
                
                // Update like icon based on state
                if (isLiked) {
                    ivLikeIcon.setImageResource(R.drawable.ic_favorite)
                    ivLikeIcon.setColorFilter(binding.root.context.getColor(R.color.comment_liked))
                    tvLikeCount.setTextColor(binding.root.context.getColor(R.color.comment_liked))
                } else {
                    ivLikeIcon.setImageResource(R.drawable.ic_favorite_border)
                    ivLikeIcon.setColorFilter(binding.root.context.getColor(R.color.text_secondary))
                    tvLikeCount.setTextColor(binding.root.context.getColor(R.color.text_secondary))
                }

                // Post image (if exists)
                if (!post.imageUrl.isNullOrEmpty()) {
                    cardPostImage.visibility = android.view.View.VISIBLE

                    val context = binding.root.context
                    // Activity kontrolü
                    if (context is Activity && !context.isFinishing && !context.isDestroyed) {
                        try {
                            Glide.with(context)
                                .load(post.imageUrl)
                                .centerCrop()
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(ivPostImage)
                        } catch (e: Exception) {
                            // Glide hatası - varsayılan resmi göster
                            ivPostImage.setImageResource(R.drawable.ic_profile)
                        }
                    }

                    // Image'e tıklayınca zoom viewer aç (chat'taki gibi)
                    ivPostImage.setOnClickListener {
                        val context = binding.root.context
                        val intent = android.content.Intent(context, PostImageViewerActivity::class.java).apply {
                            putExtra(PostImageViewerActivity.EXTRA_IMAGE_URL, post.imageUrl)
                        }
                        context.startActivity(intent)
                    }
                } else {
                    cardPostImage.visibility = android.view.View.GONE
                }

                // More button (delete option for own posts)
                if (post.userId == currentUserId) {
                    binding.btnMore.setOnClickListener {
                        // Show delete option
                        android.widget.PopupMenu(binding.root.context, binding.btnMore).apply {
                            menu.add("Sil")
                            setOnMenuItemClickListener { menuItem ->
                                when (menuItem.title) {
                                    "Sil" -> onDeleteClick(post)
                                }
                                true
                            }
                            show()
                        }
                    }
                } else {
                    binding.btnMore.visibility = android.view.View.GONE
                }

                // Like button
                btnLike.setOnClickListener {
                    onLikeClick(post)
                }

                // Comment button
                btnComment.setOnClickListener {
                    onCommentClick(post)
                }

                // Share button
                btnShare.setOnClickListener {
                    onShareClick(post)
                }
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Şimdi" // 1 dakika
                diff < 3600000 -> "${diff / 60000}dk önce" // 1 saat
                diff < 86400000 -> "${diff / 3600000}sa önce" // 1 gün
                else -> {
                    val sdf = SimpleDateFormat("dd MMM", Locale("tr"))
                    sdf.format(Date(timestamp))
                }
            }
        }
    }


    private class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
