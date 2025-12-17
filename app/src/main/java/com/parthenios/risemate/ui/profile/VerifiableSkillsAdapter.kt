package com.parthenios.risemate.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parthenios.risemate.R
import com.parthenios.risemate.data.SkillData
import com.parthenios.risemate.data.SkillQuizData
import com.parthenios.risemate.databinding.ItemVerifiableSkillBinding

class VerifiableSkillsAdapter(
    private val onSkillClick: (String) -> Unit
) : ListAdapter<String, VerifiableSkillsAdapter.SkillViewHolder>(SkillDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val binding = ItemVerifiableSkillBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SkillViewHolder(binding, onSkillClick)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SkillViewHolder(
        private val binding: ItemVerifiableSkillBinding,
        private val onSkillClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(skillName: String) {
            val skill = SkillData.findSkillByName(skillName)

            binding.apply {
                // Skill emoji/icon
                tvSkillEmoji.text = skill?.emoji ?: "🎯"

                // Skill name
                tvSkillName.text = skill?.displayName ?: skillName

                // Skill description - Quiz'de 5 soru sorulur
                tvSkillDescription.text = "5 soru • 4 doğru gerekli"

                // Click listener
                root.setOnClickListener {
                    onSkillClick(skillName)
                }
            }
        }
    }

    private class SkillDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
