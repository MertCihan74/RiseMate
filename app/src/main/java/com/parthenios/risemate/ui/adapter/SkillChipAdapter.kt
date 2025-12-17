package com.parthenios.risemate.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.parthenios.risemate.data.Skill
import com.parthenios.risemate.data.SkillItem
import com.parthenios.risemate.databinding.ItemSkillChipBinding
import com.parthenios.risemate.databinding.ItemSkillCategoryHeaderBinding

class SkillChipAdapter(
    private val onSkillClick: (Skill) -> Unit
) : ListAdapter<SkillItem, RecyclerView.ViewHolder>(SkillItemDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_SKILL = 1
    }

    private val selectedSkills = mutableSetOf<String>()

    fun updateSelectedSkills(newSelectedSkills: Set<String>) {
        val oldSelected = selectedSkills.toSet()
        selectedSkills.clear()
        selectedSkills.addAll(newSelectedSkills)
        
        // Sadece değişen skill item'ları güncelle
        val changedPositions = mutableListOf<Int>()
        currentList.forEachIndexed { index, item ->
            if (item is SkillItem.Skill) {
                val wasSelected = oldSelected.contains(item.skill.name)
                val isSelected = selectedSkills.contains(item.skill.name)
                if (wasSelected != isSelected) {
                    changedPositions.add(index)
                }
            }
        }
        
        changedPositions.forEach { position ->
            notifyItemChanged(position)
        }
    }

    fun getSelectedSkills(): Set<String> = selectedSkills.toSet()

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SkillItem.CategoryHeader -> VIEW_TYPE_HEADER
            is SkillItem.Skill -> VIEW_TYPE_SKILL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemSkillCategoryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                CategoryHeaderViewHolder(binding)
            }
            VIEW_TYPE_SKILL -> {
                val binding = ItemSkillChipBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SkillViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is SkillItem.CategoryHeader -> {
                (holder as CategoryHeaderViewHolder).bind(item.title)
                // Kategori başlıkları tam genişlikte görünsün
                val layoutParams = holder.itemView.layoutParams
                if (layoutParams is FlexboxLayoutManager.LayoutParams) {
                    layoutParams.isWrapBefore = true
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
            is SkillItem.Skill -> {
                (holder as SkillViewHolder).bind(item.skill)
            }
        }
    }

    inner class CategoryHeaderViewHolder(
        private val binding: ItemSkillCategoryHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvCategoryTitle.text = title
        }
    }

    inner class SkillViewHolder(
        private val binding: ItemSkillChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(skill: Skill) {
            val isSelected = selectedSkills.contains(skill.name)
            
            // Emoji + isim formatı
            val displayText = "${skill.emoji} ${skill.name}"
            binding.tvSkill.text = displayText
            
            // Seçili durumunu göster
            binding.root.isSelected = isSelected
            if (isSelected) {
                binding.root.setCardBackgroundColor(
                    androidx.core.content.ContextCompat.getColor(
                        binding.root.context,
                        com.parthenios.risemate.R.color.primary_color
                    )
                )
                binding.tvSkill.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        binding.root.context,
                        android.R.color.white
                    )
                )
            } else {
                binding.root.setCardBackgroundColor(
                    androidx.core.content.ContextCompat.getColor(
                        binding.root.context,
                        android.R.color.white
                    )
                )
                binding.tvSkill.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        binding.root.context,
                        com.parthenios.risemate.R.color.primary_color
                    )
                )
            }
            
            // Tıklama işlemi - seçiliyse kaldır, değilse ekle
            binding.root.setOnClickListener {
                val currentlySelected = selectedSkills.contains(skill.name)
                if (currentlySelected) {
                    selectedSkills.remove(skill.name)
                    // Sadece bu item'ı güncelle
                    notifyItemChanged(adapterPosition)
                    onSkillClick(skill)
                } else {
                    selectedSkills.add(skill.name)
                    // Sadece bu item'ı güncelle
                    notifyItemChanged(adapterPosition)
                    onSkillClick(skill)
                }
            }
        }
    }

    class SkillItemDiffCallback : DiffUtil.ItemCallback<SkillItem>() {
        override fun areItemsTheSame(oldItem: SkillItem, newItem: SkillItem): Boolean {
            return when {
                oldItem is SkillItem.CategoryHeader && newItem is SkillItem.CategoryHeader -> 
                    oldItem.title == newItem.title
                oldItem is SkillItem.Skill && newItem is SkillItem.Skill -> 
                    oldItem.skill.name == newItem.skill.name
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: SkillItem, newItem: SkillItem): Boolean {
            return oldItem == newItem
        }
    }
}

