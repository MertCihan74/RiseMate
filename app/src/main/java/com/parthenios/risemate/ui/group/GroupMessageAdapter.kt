package com.parthenios.risemate.ui.group

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.parthenios.risemate.R
import com.parthenios.risemate.data.GroupMessage
import com.parthenios.risemate.databinding.ItemGroupMessageBinding
import java.text.SimpleDateFormat
import java.util.Locale

class GroupMessageAdapter(
    private val currentUserId: String,
    private val onImageClick: (String) -> Unit
) : ListAdapter<GroupMessage, GroupMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemGroupMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(private val binding: ItemGroupMessageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: GroupMessage) {
            val isMyMessage = message.senderId == currentUserId

            if (isMyMessage) {
                binding.layoutOtherMessage.visibility = View.GONE
                binding.cardMyMessage.visibility = View.VISIBLE

                binding.tvMyMessage.text = message.message
                binding.tvMyTime.text = message.timestamp?.let { timeFormat.format(it) } ?: ""

                if (message.messageType == "image" && message.imageUrl.isNotEmpty()) {
                    binding.cardMyImage.visibility = View.VISIBLE
                    binding.tvMyMessage.visibility = if (message.message.isEmpty()) View.GONE else View.VISIBLE
                    loadImage(message.imageUrl, binding.ivMyImage)
                    binding.cardMyImage.setOnClickListener { onImageClick(message.imageUrl) }
                } else {
                    binding.cardMyImage.visibility = View.GONE
                    binding.tvMyMessage.visibility = View.VISIBLE
                }
            } else {
                binding.cardMyMessage.visibility = View.GONE
                binding.layoutOtherMessage.visibility = View.VISIBLE

                binding.tvOtherName.text = message.senderName
                binding.tvOtherMessage.text = message.message
                binding.tvOtherTime.text = message.timestamp?.let { timeFormat.format(it) } ?: ""

                if (message.senderPhotoUrl.isNotEmpty()) {
                    loadImage(message.senderPhotoUrl, binding.ivOtherAvatar, true)
                } else {
                    binding.ivOtherAvatar.setImageResource(R.drawable.ic_profile)
                }

                if (message.messageType == "image" && message.imageUrl.isNotEmpty()) {
                    binding.cardOtherImage.visibility = View.VISIBLE
                    binding.tvOtherMessage.visibility = if (message.message.isEmpty()) View.GONE else View.VISIBLE
                    loadImage(message.imageUrl, binding.ivOtherImage)
                    binding.cardOtherImage.setOnClickListener { onImageClick(message.imageUrl) }
                } else {
                    binding.cardOtherImage.visibility = View.GONE
                    binding.tvOtherMessage.visibility = View.VISIBLE
                }
            }
        }

        private fun loadImage(imageUrl: String, imageView: android.widget.ImageView, isCircle: Boolean = false) {
            try {
                if (imageUrl.startsWith("data:image")) {
                    // Base64 image
                    val base64Data = imageUrl.substringAfter("base64,")
                    val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    imageView.setImageBitmap(bitmap)
                } else {
                    // URL image
                    val glide = Glide.with(imageView).load(imageUrl)
                    if (isCircle) {
                        glide.circleCrop().placeholder(R.drawable.ic_profile).into(imageView)
                    } else {
                        glide.centerCrop().into(imageView)
                    }
                }
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.ic_profile)
            }
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<GroupMessage>() {
        override fun areItemsTheSame(oldItem: GroupMessage, newItem: GroupMessage) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GroupMessage, newItem: GroupMessage) = oldItem == newItem
    }
}
