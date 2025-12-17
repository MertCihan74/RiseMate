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
import com.parthenios.risemate.data.Answer
import com.parthenios.risemate.databinding.ItemAnswerBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AnswerAdapter(
    private val currentUserId: String,
    private val questionOwnerId: String,
    private val onLikeClick: (Answer) -> Unit,
    private val onAcceptClick: (Answer) -> Unit
) : ListAdapter<Answer, AnswerAdapter.AnswerViewHolder>(AnswerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val binding = ItemAnswerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AnswerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AnswerViewHolder(
        private val binding: ItemAnswerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(answer: Answer) {
            binding.apply {
                // User info
                tvUserName.text = answer.userName
                tvTime.text = getTimeAgo(answer.createdAt)
                
                // Load user photo
                loadUserPhoto(answer.userPhotoUrl)
                
                // Content
                tvContent.text = answer.content
                
                // Accepted badge
                layoutAccepted.visibility = if (answer.isAccepted) View.VISIBLE else View.GONE
                
                // Like
                tvLikeCount.text = answer.likeCount.toString()
                val isLiked = answer.likedBy.contains(currentUserId)
                ivLike.setColorFilter(
                    if (isLiked) root.context.getColor(R.color.error_color)
                    else root.context.getColor(R.color.text_secondary)
                )
                ivLike.setImageResource(
                    if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                )
                
                // Accept button - only visible to question owner and if not already accepted
                btnAccept.visibility = if (
                    currentUserId == questionOwnerId && 
                    !answer.isAccepted &&
                    answer.userId != currentUserId
                ) View.VISIBLE else View.GONE
                
                // Click listeners
                layoutLike.setOnClickListener { onLikeClick(answer) }
                btnAccept.setOnClickListener { onAcceptClick(answer) }
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
                minutes < 60 -> "$minutes dk önce"
                hours < 24 -> "$hours saat önce"
                days < 7 -> "$days gün önce"
                else -> SimpleDateFormat("dd MMM", Locale("tr")).format(date)
            }
        }
    }

    class AnswerDiffCallback : DiffUtil.ItemCallback<Answer>() {
        override fun areItemsTheSame(oldItem: Answer, newItem: Answer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Answer, newItem: Answer): Boolean {
            return oldItem == newItem
        }
    }
}


