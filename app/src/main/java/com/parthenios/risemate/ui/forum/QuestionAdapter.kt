package com.parthenios.risemate.ui.forum

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parthenios.risemate.R
import com.parthenios.risemate.data.Question
import com.parthenios.risemate.databinding.ItemQuestionBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class QuestionAdapter(
    private val currentUserId: String,
    private val onQuestionClick: (Question) -> Unit,
    private val onLikeClick: (Question) -> Unit
) : ListAdapter<Question, QuestionAdapter.QuestionViewHolder>(QuestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuestionViewHolder(
        private val binding: ItemQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question) {
            binding.apply {
                // Skill tag
                tvSkillTag.text = question.skill
                
                // Title & Content
                tvTitle.text = question.title
                tvContent.text = question.content
                
                // User info
                tvUserName.text = question.userName
                tvTime.text = getTimeAgo(question.createdAt)
                loadUserPhoto(question.userPhotoUrl)
                
                // Resolved badge
                layoutResolved.visibility = if (question.isResolved) View.VISIBLE else View.GONE
                
                // Stats
                tvAnswerCount.text = question.answerCount.toString()
                tvViewCount.text = question.viewCount.toString()
                tvLikeCount.text = question.likeCount.toString()
                
                // Like state
                val isLiked = question.likedBy.contains(currentUserId)
                ivLike.setColorFilter(
                    if (isLiked) root.context.getColor(R.color.error_color)
                    else root.context.getColor(R.color.text_secondary)
                )
                ivLike.setImageResource(
                    if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                )
                
                // Click listeners
                root.setOnClickListener { onQuestionClick(question) }
                layoutLike.setOnClickListener { 
                    onLikeClick(question)
                }
            }
        }

        private fun loadUserPhoto(photoUrl: String) {
            if (photoUrl.startsWith("data:image")) {
                try {
                    val base64Data = photoUrl.substringAfter("base64,")
                    val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.ivUserPhoto.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    binding.ivUserPhoto.setImageResource(R.drawable.default_avatar)
                }
            } else {
                binding.ivUserPhoto.setImageResource(R.drawable.default_avatar)
            }
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
                minutes < 60 -> "${minutes}dk"
                hours < 24 -> "${hours}sa"
                days < 7 -> "${days}g"
                else -> SimpleDateFormat("dd MMM", Locale("tr")).format(date)
            }
        }
    }

    class QuestionDiffCallback : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem == newItem
        }
    }
}
