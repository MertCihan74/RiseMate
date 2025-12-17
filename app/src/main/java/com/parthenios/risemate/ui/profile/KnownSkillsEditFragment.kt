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
import com.parthenios.risemate.databinding.FragmentKnownSkillsBinding
import com.parthenios.risemate.ui.adapter.SkillChipAdapter

/**
 * Profil düzenlemede "Bildiğim Yetenekler" adımı.
 * Register akışındaki KnownSkillsFragment ile benzer, ancak EditProfileActivity ile çalışır.
 */
class KnownSkillsEditFragment : Fragment() {
    private var _binding: FragmentKnownSkillsBinding? = null
    private val binding get() = _binding!!
    private val selectedSkills = mutableSetOf<String>()
    private lateinit var adapter: SkillChipAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKnownSkillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editProfileActivity = activity as? EditProfileActivity
        editProfileActivity?.let {
            selectedSkills.clear()
            selectedSkills.addAll(it.knownSkills)
        }

        setupRecyclerView()
        updateSelectedChips()

        // Buton metinlerini düzenle
        binding.btnNext.text = "Devam Et"
        binding.btnBack.text = "Geri"

        binding.btnNext.setOnClickListener {
            if (validateInputs()) {
                saveData()
                editProfileActivity?.goToNextStep()
            }
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

    private fun validateInputs(): Boolean {
        selectedSkills.clear()
        selectedSkills.addAll(adapter.getSelectedSkills())

        if (selectedSkills.size < 3) {
            android.widget.Toast.makeText(
                requireContext(),
                "En az 3 bildiğin yetenek seçmelisiniz",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun saveData() {
        val editProfileActivity = activity as? EditProfileActivity ?: return
        selectedSkills.clear()
        selectedSkills.addAll(adapter.getSelectedSkills())
        editProfileActivity.knownSkills = selectedSkills.toList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}








