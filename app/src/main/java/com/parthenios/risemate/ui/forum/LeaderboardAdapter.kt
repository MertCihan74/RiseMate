package com.parthenios.risemate.ui.forum

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parthenios.risemate.R
import com.parthenios.risemate.data.LeaderboardEntry
import com.parthenios.risemate.databinding.ItemLeaderboardBinding

class LeaderboardAdapter : ListAdapter<LeaderboardEntry, LeaderboardAdapter.LeaderboardViewHolder>(LeaderboardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LeaderboardViewHolder(
        private val binding: ItemLeaderboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: LeaderboardEntry) {
            binding.apply {
                tvRank.text = entry.rank.toString()
                tvUserName.text = entry.userName
                tvAnswerCount.text = entry.acceptedAnswerCount.toString()
                
                // Load user photo
                if (entry.userPhotoUrl.startsWith("data:image")) {
                    try {
                        val base64Data = entry.userPhotoUrl.substringAfter("base64,")
                        val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        ivUserPhoto.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        ivUserPhoto.setImageResource(R.drawable.default_avatar)
                    }
                } else {
                    ivUserPhoto.setImageResource(R.drawable.default_avatar)
                }
            }
        }
    }

    class LeaderboardDiffCallback : DiffUtil.ItemCallback<LeaderboardEntry>() {
        override fun areItemsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem == newItem
        }
    }
}


