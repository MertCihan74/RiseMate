package com.parthenios.risemate.ui.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.parthenios.risemate.R
import com.parthenios.risemate.data.GroupMember
import com.parthenios.risemate.databinding.ItemGroupMemberBinding

class GroupMemberAdapter(
    private val onMemberClick: (GroupMember) -> Unit
) : ListAdapter<GroupMember, GroupMemberAdapter.MemberViewHolder>(MemberDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemGroupMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemberViewHolder(private val binding: ItemGroupMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(member: GroupMember) {
            binding.tvName.text = member.userName
            
            // Yetenek tipi
            val skillTypeText = when (member.skillType) {
                "known" -> "Bu yeteneği biliyor"
                "wanted" -> "Bu yeteneği öğrenmek istiyor"
                else -> ""
            }
            binding.tvSkillType.text = skillTypeText
            
            // Badge
            if (member.skillType == "known") {
                binding.tvBadge.visibility = View.VISIBLE
                binding.tvBadge.text = "Biliyor"
                binding.tvBadge.setBackgroundResource(R.drawable.skill_chip_known)
                binding.tvBadge.setTextColor(binding.root.context.getColor(R.color.skill_known_text))
            } else if (member.skillType == "wanted") {
                binding.tvBadge.visibility = View.VISIBLE
                binding.tvBadge.text = "Öğreniyor"
                binding.tvBadge.setBackgroundResource(R.drawable.skill_chip_wanted)
                binding.tvBadge.setTextColor(binding.root.context.getColor(R.color.skill_wanted_text))
            } else {
                binding.tvBadge.visibility = View.GONE
            }
            
            // Avatar
            if (member.userPhotoUrl.isNotEmpty()) {
                Glide.with(binding.ivAvatar)
                    .load(member.userPhotoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_profile)
            }
            
            binding.root.setOnClickListener { onMemberClick(member) }
        }
    }

    private class MemberDiffCallback : DiffUtil.ItemCallback<GroupMember>() {
        override fun areItemsTheSame(oldItem: GroupMember, newItem: GroupMember) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GroupMember, newItem: GroupMember) = oldItem == newItem
    }
}


