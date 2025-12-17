package com.parthenios.risemate.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.parthenios.risemate.R
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.ItemBlockedUserBinding

class BlockedUsersAdapter(
    private val blockedUsers: List<User>,
    private val onUnblockClick: (User) -> Unit
) : RecyclerView.Adapter<BlockedUsersAdapter.ViewHolder>() {
    
    inner class ViewHolder(
        private val binding: ItemBlockedUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User) {
            binding.apply {
                tvUserName.text = "${user.firstName} ${user.lastName}"
                tvUsername.text = "@${user.username}"
                
                // Profil fotoğrafını yükle
                if (user.photoUrl.isNotEmpty()) {
                    Glide.with(binding.root.context)
                        .load(user.photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .into(ivProfilePhoto)
                } else {
                    ivProfilePhoto.setImageResource(R.drawable.ic_profile)
                }
                
                btnUnblock.setOnClickListener {
                    onUnblockClick(user)
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBlockedUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(blockedUsers[position])
    }
    
    override fun getItemCount(): Int = blockedUsers.size
}








