package com.parthenios.risemate.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.chip.Chip
import com.parthenios.risemate.R
import com.parthenios.risemate.auth.AuthRepository
import com.parthenios.risemate.data.SkillData
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.FragmentWantedSkillsBinding
import com.parthenios.risemate.MainActivity
import com.parthenios.risemate.ui.adapter.SkillChipAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WantedSkillsFragment : Fragment() {
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

        setupRecyclerView()
        updateSelectedChips()

        binding.btnComplete.setOnClickListener {
            if (validateInputs()) {
                completeRegistration()
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
        
        android.util.Log.d("WantedSkillsFragment", "Yetenek sayısı: ${SkillData.predefinedSkills.size}")
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
        // Öğrenmek istediğin yeteneklerde minimum sınır yok
        return true
    }

    private fun completeRegistration() {
        val registerActivity = activity as RegisterActivity
        // Adapter'dan güncel seçili yetenekleri al
        selectedSkills.clear()
        selectedSkills.addAll(adapter.getSelectedSkills())
        registerActivity.wantedSkills = selectedSkills.toList()

        binding.btnComplete.isEnabled = false
        binding.btnComplete.text = "Kayıt tamamlanıyor..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authRepository = AuthRepository()
                val currentUser = authRepository.getCurrentUser()
                
                if (currentUser != null) {
                    // Yaş hesapla
                    val calculatedAge = calculateAge(registerActivity.birthDate)
                    
                    val user = User(
                        uid = currentUser.uid,
                        email = registerActivity.email,
                        firstName = registerActivity.firstName,
                        lastName = registerActivity.lastName,
                        username = registerActivity.username,
                        birthDate = registerActivity.birthDate,
                        age = calculatedAge,
                        city = registerActivity.city,
                        knownSkills = registerActivity.knownSkills,
                        wantedSkills = registerActivity.wantedSkills,
                        birthday = formatBirthday(registerActivity.birthDate)
                    )

                    val result = authRepository.saveUserToFirestore(user)
                    
                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            Toast.makeText(
                                requireContext(),
                                "Kayıt başarıyla tamamlandı!",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(android.content.Intent(requireContext(), MainActivity::class.java))
                            requireActivity().finish()
                        } else {
                            binding.btnComplete.isEnabled = true
                            binding.btnComplete.text = "Kayıt Ol"
                            Toast.makeText(
                                requireContext(),
                                "Kayıt sırasında hata oluştu: ${result.exceptionOrNull()?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.btnComplete.isEnabled = true
                        binding.btnComplete.text = "Kayıt Ol"
                        Toast.makeText(
                            requireContext(),
                            "Kullanıcı oturumu bulunamadı",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnComplete.isEnabled = true
                    binding.btnComplete.text = "Kayıt Ol"
                    Toast.makeText(
                        requireContext(),
                        "Bir hata oluştu: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun calculateAge(birthDate: java.util.Date?): Int {
        if (birthDate == null) return 0
        
        val calendar = java.util.Calendar.getInstance()
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        
        val birthCalendar = java.util.Calendar.getInstance()
        birthCalendar.time = birthDate
        val birthYear = birthCalendar.get(java.util.Calendar.YEAR)
        val birthMonth = birthCalendar.get(java.util.Calendar.MONTH) + 1
        val birthDay = birthCalendar.get(java.util.Calendar.DAY_OF_MONTH)
        
        var age = currentYear - birthYear
        
        if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
            age--
        }
        
        return age
    }
    
    private fun formatBirthday(birthDate: java.util.Date?): String {
        if (birthDate == null) return ""
        
        val calendar = java.util.Calendar.getInstance()
        calendar.time = birthDate
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}