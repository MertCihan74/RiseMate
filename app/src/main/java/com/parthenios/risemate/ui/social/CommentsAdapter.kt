package com.parthenios.risemate.ui.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.data.Comment
import com.parthenios.risemate.databinding.ItemCommentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CommentsAdapter(
    private val onLikeClick: ((Comment) -> Unit)? = null,
    private val onReplyClick: ((Comment) -> Unit)? = null,
    private val onMoreClick: ((Comment) -> Unit)? = null,
    private val onReplyLikeClick: ((Comment, Comment) -> Unit)? = null // parentComment, reply
) : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding, onLikeClick, onReplyClick, onMoreClick, onReplyLikeClick, currentUserId)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(
        private val binding: ItemCommentBinding,
        private val onLikeClick: ((Comment) -> Unit)?,
        private val onReplyClick: ((Comment) -> Unit)?,
        private val onMoreClick: ((Comment) -> Unit)?,
        private val onReplyLikeClick: ((Comment, Comment) -> Unit)?,
        private val currentUserId: String?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.apply {
                // User name
                tvUserName.text = comment.userName

                // Timestamp
                tvTimestamp.text = formatTimestamp(comment.timestamp)

                // Comment content
                tvCommentContent.text = comment.content

                // User photo - Firestore'dan yükle
                loadUserPhoto(comment.userId, ivUserPhoto)

                // Like count ve durumu
                val isLiked = currentUserId != null && comment.likes.contains(currentUserId)
                updateLikeUI(isLiked, comment.likeCount)

                // Verified badge (şimdilik gizli)
                ivVerified.visibility = View.GONE

                // Liked by text removed
                // updateLikedByText(comment)

                // Click listeners
                layoutLike.setOnClickListener {
                    onLikeClick?.invoke(comment)
                }

                layoutReply.setOnClickListener {
                    onReplyClick?.invoke(comment)
                }

                btnMore.setOnClickListener {
                    onMoreClick?.invoke(comment)
                }

                // Yanıtları göster
                setupReplies(comment)
            }
        }

        private fun setupReplies(comment: Comment) {
            binding.apply {
                if (comment.replies.isNotEmpty()) {
                    layoutReplies.visibility = View.VISIBLE
                    containerReplies.removeAllViews()

                    // İlk 3 yanıtı göster
                    val repliesToShow = comment.replies.take(3)
                    
                    for (reply in repliesToShow) {
                        val replyView = LayoutInflater.from(binding.root.context)
                            .inflate(R.layout.item_comment_reply, containerReplies, false)
                        
                        bindReplyView(replyView, reply, comment)
                        containerReplies.addView(replyView)
                    }

                    // Daha fazla yanıt varsa buton göster
                    if (comment.replies.size > 3) {
                        tvViewReplies.visibility = View.VISIBLE
                        tvViewReplies.text = "↳ ${comment.replies.size - 3} yanıt daha göster"
                    } else {
                        tvViewReplies.visibility = View.GONE
                    }
                } else {
                    layoutReplies.visibility = View.GONE
                }
            }
        }

        private fun bindReplyView(view: View, reply: Comment, parentComment: Comment) {
            val ivReplyUserPhoto = view.findViewById<ImageView>(R.id.ivReplyUserPhoto)
            val tvReplyUserName = view.findViewById<TextView>(R.id.tvReplyUserName)
            val tvReplyTimestamp = view.findViewById<TextView>(R.id.tvReplyTimestamp)
            val tvReplyContent = view.findViewById<TextView>(R.id.tvReplyContent)
            val ivReplyLike = view.findViewById<ImageView>(R.id.ivReplyLike)
            val tvReplyLikeCount = view.findViewById<TextView>(R.id.tvReplyLikeCount)
            val layoutReplyLike = view.findViewById<LinearLayout>(R.id.layoutReplyLike)

            tvReplyUserName.text = reply.userName
            tvReplyTimestamp.text = formatTimestamp(reply.timestamp)
            tvReplyContent.text = reply.content

            // Load reply user photo
            loadUserPhoto(reply.userId, ivReplyUserPhoto)

            // Like durumu
            val isLiked = currentUserId != null && reply.likes.contains(currentUserId)
            if (isLiked) {
                ivReplyLike.setImageResource(R.drawable.ic_favorite)
                ivReplyLike.setColorFilter(view.context.getColor(R.color.comment_liked))
            } else {
                ivReplyLike.setImageResource(R.drawable.ic_favorite_border)
                ivReplyLike.setColorFilter(view.context.getColor(R.color.text_hint))
            }

            // Like count
            if (reply.likeCount > 0) {
                tvReplyLikeCount.visibility = View.VISIBLE
                tvReplyLikeCount.text = reply.likeCount.toString()
            } else {
                tvReplyLikeCount.visibility = View.GONE
            }

            // Click listener
            layoutReplyLike.setOnClickListener {
                onReplyLikeClick?.invoke(parentComment, reply)
            }
        }

        private fun loadUserPhoto(userId: String, imageView: ImageView) {
            val firestore = FirebaseFirestore.getInstance()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userDoc = firestore.collection("users")
                        .document(userId)
                        .get()
                        .await()

                    withContext(Dispatchers.Main) {
                        if (userDoc.exists()) {
                            val photoUrl = userDoc.getString("photoUrl") ?: ""
                            if (photoUrl.isNotEmpty()) {
                                Glide.with(binding.root.context)
                                    .load(photoUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_profile)
                                    .into(imageView)
                            } else {
                                imageView.setImageResource(R.drawable.ic_profile)
                            }
                        } else {
                            imageView.setImageResource(R.drawable.ic_profile)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        imageView.setImageResource(R.drawable.ic_profile)
                    }
                }
            }
        }

        private fun updateLikeUI(isLiked: Boolean, likeCount: Int) {
            binding.apply {
                if (isLiked) {
                    ivLike.setImageResource(R.drawable.ic_favorite)
                    ivLike.setColorFilter(binding.root.context.getColor(R.color.comment_liked))
                    tvLikeCount.setTextColor(binding.root.context.getColor(R.color.comment_liked))
                } else {
                    ivLike.setImageResource(R.drawable.ic_favorite_border)
                    ivLike.setColorFilter(binding.root.context.getColor(R.color.text_secondary))
                    tvLikeCount.setTextColor(binding.root.context.getColor(R.color.text_secondary))
                }
                tvLikeCount.text = if (likeCount > 0) likeCount.toString() else ""
            }
        }

        // Removed updateLikedByText since the view was removed from the layout

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Şimdi"
                diff < 3600000 -> "${diff / 60000} dk"
                diff < 86400000 -> "${diff / 3600000} sa"
                diff < 604800000 -> "${diff / 86400000} gün"
                else -> {
                    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem && 
                   oldItem.likes == newItem.likes && 
                   oldItem.replies == newItem.replies
        }
    }
}
