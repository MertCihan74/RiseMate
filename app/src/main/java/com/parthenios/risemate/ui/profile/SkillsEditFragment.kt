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
import com.parthenios.risemate.databinding.FragmentSkillsEditBinding
import com.parthenios.risemate.ui.adapter.SkillChipAdapter

class SkillsEditFragment : Fragment() {
    private var _binding: FragmentSkillsEditBinding? = null
    private val binding get() = _binding!!
    private val selectedKnownSkills = mutableSetOf<String>()
    private val selectedWantedSkills = mutableSetOf<String>()
    private lateinit var knownSkillsAdapter: SkillChipAdapter
    private lateinit var wantedSkillsAdapter: SkillChipAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkillsEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mevcut yetenekleri yükle
        val editProfileActivity = activity as? EditProfileActivity
        if (editProfileActivity != null) {
            selectedKnownSkills.clear()
            selectedKnownSkills.addAll(editProfileActivity.knownSkills)
            selectedWantedSkills.clear()
            selectedWantedSkills.addAll(editProfileActivity.wantedSkills)
        }

        setupRecyclerViews()
        updateSelectedChips()

        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveData()
                editProfileActivity?.saveProfile()
            }
        }

        binding.btnBack.setOnClickListener {
            (activity as EditProfileActivity).goToPreviousStep()
        }
    }

    private fun setupRecyclerViews() {
        // Bildiğim Yetenekler RecyclerView
        knownSkillsAdapter = SkillChipAdapter(
            onSkillClick = { skill ->
                // Adapter zaten toggle yapıyor, sadece fragment'taki listeyi güncelle
                selectedKnownSkills.clear()
                selectedKnownSkills.addAll(knownSkillsAdapter.getSelectedSkills())
                updateSelectedChips()
            }
        )

        val knownLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = com.google.android.flexbox.JustifyContent.FLEX_START
            alignItems = com.google.android.flexbox.AlignItems.STRETCH
        }
        
        binding.rvKnownSkills.layoutManager = knownLayoutManager
        binding.rvKnownSkills.setHasFixedSize(false)
        binding.rvKnownSkills.isNestedScrollingEnabled = true
        binding.rvKnownSkills.adapter = knownSkillsAdapter
        knownSkillsAdapter.submitList(SkillData.getAllSkillsWithCategories())
        knownSkillsAdapter.updateSelectedSkills(selectedKnownSkills)

        // Öğrenmek İstediğim Yetenekler RecyclerView
        wantedSkillsAdapter = SkillChipAdapter(
            onSkillClick = { skill ->
                // Adapter zaten toggle yapıyor, sadece fragment'taki listeyi güncelle
                selectedWantedSkills.clear()
                selectedWantedSkills.addAll(wantedSkillsAdapter.getSelectedSkills())
                updateSelectedChips()
            }
        )

        val wantedLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = com.google.android.flexbox.JustifyContent.FLEX_START
            alignItems = com.google.android.flexbox.AlignItems.STRETCH
        }
        
        binding.rvWantedSkills.layoutManager = wantedLayoutManager
        binding.rvWantedSkills.setHasFixedSize(false)
        binding.rvWantedSkills.isNestedScrollingEnabled = true
        binding.rvWantedSkills.adapter = wantedSkillsAdapter
        wantedSkillsAdapter.submitList(SkillData.getAllSkillsWithCategories())
        wantedSkillsAdapter.updateSelectedSkills(selectedWantedSkills)
        
        android.util.Log.d("SkillsEditFragment", "Yetenek sayısı: ${SkillData.predefinedSkills.size}")
    }

    private fun updateSelectedChips() {
        // Bildiğim Yetenekler
        binding.chipGroupSelectedKnownSkills.removeAllViews()
        selectedKnownSkills.forEach { skillName ->
            val skill = SkillData.findSkillByName(skillName)
            if (skill != null) {
                val chip = Chip(requireContext())
                chip.text = skill.displayName
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    selectedKnownSkills.remove(skillName)
                    knownSkillsAdapter.updateSelectedSkills(selectedKnownSkills)
                    updateSelectedChips()
                }
                binding.chipGroupSelectedKnownSkills.addView(chip)
            }
        }

        // Öğrenmek İstediğim Yetenekler
        binding.chipGroupSelectedWantedSkills.removeAllViews()
        selectedWantedSkills.forEach { skillName ->
            val skill = SkillData.findSkillByName(skillName)
            if (skill != null) {
                val chip = Chip(requireContext())
                chip.text = skill.displayName
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    selectedWantedSkills.remove(skillName)
                    wantedSkillsAdapter.updateSelectedSkills(selectedWantedSkills)
                    updateSelectedChips()
                }
                binding.chipGroupSelectedWantedSkills.addView(chip)
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Adapter'lardan güncel seçili yetenekleri al
        selectedKnownSkills.clear()
        selectedKnownSkills.addAll(knownSkillsAdapter.getSelectedSkills())
        selectedWantedSkills.clear()
        selectedWantedSkills.addAll(wantedSkillsAdapter.getSelectedSkills())
        
        if (selectedKnownSkills.size < 3) {
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
        val editProfileActivity = activity as EditProfileActivity
        // Adapter'lardan güncel seçili yetenekleri al
        selectedKnownSkills.clear()
        selectedKnownSkills.addAll(knownSkillsAdapter.getSelectedSkills())
        selectedWantedSkills.clear()
        selectedWantedSkills.addAll(wantedSkillsAdapter.getSelectedSkills())
        editProfileActivity.knownSkills = selectedKnownSkills.toList()
        editProfileActivity.wantedSkills = selectedWantedSkills.toList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


