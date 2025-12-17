package com.parthenios.risemate.ui.explore

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.parthenios.risemate.R
import com.parthenios.risemate.databinding.ItemSkillChipExploreBinding

/**
 * Keşfet kartlarında yatay skill listesi için adaptör.
 * Glassmorphism efektli skill chip'leri kullanır.
 * Doğrulanmış yetenekler farklı stil ile gösterilir.
 */
class SkillChipAdapter(
    private val skills: List<String>,
    private val verifiedSkills: List<String> = emptyList()
) : RecyclerView.Adapter<SkillChipAdapter.SkillChipViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillChipViewHolder {
        val binding = ItemSkillChipExploreBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SkillChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SkillChipViewHolder, position: Int) {
        holder.bind(skills[position], verifiedSkills)
    }

    override fun getItemCount(): Int = skills.size

    class SkillChipViewHolder(
        private val binding: ItemSkillChipExploreBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(skill: String, verifiedSkills: List<String>) {
            val isVerified = verifiedSkills.contains(skill)
            
            // Doğrulanmış yetenekler için ✓ ekle
            binding.tvSkill.text = if (isVerified) "$skill ✓" else skill
            
            // Doğrulanmış yetenekler için özel stil
            val card = binding.root as com.google.android.material.card.MaterialCardView
            if (isVerified) {
                // Mor gradient benzeri solid renk (glassmorphism üzerinde)
                card.setCardBackgroundColor(Color.parseColor("#B06200EE"))
                card.strokeColor = Color.parseColor("#806200EE")
                card.strokeWidth = 2
                binding.tvSkill.setTextColor(Color.WHITE)
            } else {
                // Normal glassmorphism stili
                card.setCardBackgroundColor(Color.parseColor("#40FFFFFF"))
                card.strokeColor = Color.parseColor("#50FFFFFF")
                card.strokeWidth = 1
                binding.tvSkill.setTextColor(Color.WHITE)
            }
        }
    }
}
