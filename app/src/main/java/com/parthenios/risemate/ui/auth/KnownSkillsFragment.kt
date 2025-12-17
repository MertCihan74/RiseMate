package com.parthenios.risemate.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.chip.Chip
import com.parthenios.risemate.data.Skill
import com.parthenios.risemate.data.SkillData
import com.parthenios.risemate.databinding.FragmentKnownSkillsBinding
import com.parthenios.risemate.ui.adapter.SkillChipAdapter

class KnownSkillsFragment : Fragment() {
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

        setupRecyclerView()
        updateSelectedChips()

        binding.btnNext.setOnClickListener {
            if (validateInputs()) {
                saveData()
                (activity as RegisterActivity).goToNextStep()
            }
        }

        binding.btnBack.setOnClickListener {
            (activity as RegisterActivity).goToPreviousStep()
        }
    }

    private fun setupRecyclerView() {
        adapter = SkillChipAdapter(
            onSkillClick = { skill ->
                // Adapter zaten toggle yapıyor, sadece fragment'taki listeyi güncelle
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
        
        // Veriyi yükle (kategorilerle birlikte)
        adapter.submitList(SkillData.getAllSkillsWithCategories())
        adapter.updateSelectedSkills(selectedSkills)
        
        android.util.Log.d("KnownSkillsFragment", "Yetenek sayısı: ${SkillData.predefinedSkills.size}")
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
        // Adapter'dan güncel seçili yetenekleri al
        selectedSkills.clear()
        selectedSkills.addAll(adapter.getSelectedSkills())
        
        if (selectedSkills.size < 3) {
            android.widget.Toast.makeText(
                requireContext(),
                "En az 3 yetenek seçmelisiniz",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun saveData() {
        val registerActivity = activity as RegisterActivity
        // Adapter'dan güncel seçili yetenekleri al
        selectedSkills.clear()
        selectedSkills.addAll(adapter.getSelectedSkills())
        registerActivity.knownSkills = selectedSkills.toList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}