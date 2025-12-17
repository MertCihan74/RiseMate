package com.parthenios.risemate.ui.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.parthenios.risemate.R
import com.parthenios.risemate.data.SkillGroup
import com.parthenios.risemate.databinding.ItemGroupBinding

class GroupAdapter(
    private val onGroupClick: (SkillGroup) -> Unit,
    private val onJoinClick: (SkillGroup) -> Unit
) : ListAdapter<SkillGroup, GroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    private var joinedGroupIds: Set<String> = emptySet()

    fun setJoinedGroups(groupIds: Set<String>) {
        joinedGroupIds = groupIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroupViewHolder(private val binding: ItemGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group: SkillGroup) {
            binding.tvGroupName.text = group.name
            binding.tvDescription.text = group.description
            binding.tvSkillTag.text = group.skill
            binding.tvCategoryTag.text = group.category
            binding.tvMemberCount.text = "${group.memberCount} üye"

            if (group.photoUrl.isNotEmpty()) {
                Glide.with(binding.ivGroupPhoto).load(group.photoUrl).centerCrop().placeholder(R.drawable.ic_group).into(binding.ivGroupPhoto)
                binding.ivGroupPhoto.setPadding(0, 0, 0, 0)
            } else {
                binding.ivGroupPhoto.setImageResource(R.drawable.ic_group)
            }

            val isJoined = joinedGroupIds.contains(group.id)
            binding.btnJoin.text = if (isJoined) "Üye" else "Katıl"
            binding.btnJoin.isEnabled = !isJoined

            binding.root.setOnClickListener { onGroupClick(group) }
            binding.btnJoin.setOnClickListener { if (!isJoined) onJoinClick(group) }
        }
    }

    private class GroupDiffCallback : DiffUtil.ItemCallback<SkillGroup>() {
        override fun areItemsTheSame(oldItem: SkillGroup, newItem: SkillGroup) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SkillGroup, newItem: SkillGroup) = oldItem == newItem
    }
}
