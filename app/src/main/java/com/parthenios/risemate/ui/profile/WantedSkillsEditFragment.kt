package com.parthenios.risemate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.chip.Chip
import com.parthenios.risemate.data.SkillData
import com.parthenios.risemate.databinding.FragmentWantedSkillsBinding
import com.parthenios.risemate.ui.adapter.SkillChipAdapter

/**
 * Profil düzenlemede "Öğrenmek İstediğim Yetenekler" adımı.
 * Register akışındaki WantedSkillsFragment'e benzer fakat minimum seçim sınırı yok.
 */
class WantedSkillsEditFragment : Fragment() {
    private var _binding: FragmentWantedSkillsBinding? = null
    private val binding get() = _binding!!
    private val selectedSkills = mutableSetOf<String>()
    private lateinit var adapter: SkillChipAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWantedSkillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editProfileActivity = activity as? EditProfileActivity
        editProfileActivity?.let {
            selectedSkills.clear()
            selectedSkills.addAll(it.wantedSkills)
        }

        setupRecyclerView()
        updateSelectedChips()

        // Buton metinlerini düzenle
        binding.btnComplete.text = "Kaydet"
        binding.btnBack.text = "Geri"

        binding.btnComplete.setOnClickListener {
            saveData()
            editProfileActivity?.saveProfile()
        }

        binding.btnBack.setOnClickListener {
            editProfileActivity?.goToPreviousStep()
        }
    }

    private fun setupRecyclerView() {
        adapter = SkillChipAdapter(
            onSkillClick = {
                selectedSkills.clear()
                selectedSkills.addAll(adapter.getSelectedSkills())
                updateSelectedChips()
            }
        )

        val layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = com.google.android.flexbox.JustifyContent.FLEX_START
            alignItems = com.google.android.flexbox.AlignItems.STRETCH
        }

        binding.rvSkills.layoutManager = layoutManager
        binding.rvSkills.setHasFixedSize(false)
        binding.rvSkills.isNestedScrollingEnabled = true
        binding.rvSkills.adapter = adapter

        adapter.submitList(SkillData.getAllSkillsWithCategories())
        adapter.updateSelectedSkills(selectedSkills)
    }

    private fun updateSelectedChips() {
        binding.chipGroupSelectedSkills.removeAllViews()
        selectedSkills.forEach { skillName ->
            val skill = SkillData.findSkillByName(skillName)
            if (skill != null) {
                val chip = Chip(requireContext())
                chip.text = skill.displayName
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    selectedSkills.remove(skillName)
                    adapter.updateSelectedSkills(selectedSkills)
                    updateSelectedChips()
                }
                binding.chipGroupSelectedSkills.addView(chip)
            }
        }
    }

    private fun saveData() {
        val editProfileActivity = activity as? EditProfileActivity ?: return
        selectedSkills.clear()
        selectedSkills.addAll(adapter.getSelectedSkills())
        editProfileActivity.wantedSkills = selectedSkills.toList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}








