package com.parthenios.risemate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.parthenios.risemate.databinding.FragmentPersonalInfoEditBinding

class PersonalInfoEditFragment : Fragment() {
    private var _binding: FragmentPersonalInfoEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mevcut bilgileri doldur
        val editProfileActivity = activity as? EditProfileActivity
        if (editProfileActivity != null) {
            binding.etFirstName.setText(editProfileActivity.firstName)
            binding.etLastName.setText(editProfileActivity.lastName)
            binding.etAge.setText(if (editProfileActivity.age > 0) editProfileActivity.age.toString() else "")
        }

        binding.btnNext.setOnClickListener {
            if (validateInputs()) {
                saveData()
                (activity as EditProfileActivity).goToNextStep()
            }
        }

        binding.btnBack.setOnClickListener {
            (activity as EditProfileActivity).goToPreviousStep()
        }
    }

    private fun validateInputs(): Boolean {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val ageStr = binding.etAge.text.toString().trim()

        if (firstName.isEmpty()) {
            binding.tilFirstName.error = "Ad gerekli"
            return false
        } else {
            binding.tilFirstName.error = null
        }

        if (lastName.isEmpty()) {
            binding.tilLastName.error = "Soyad gerekli"
            return false
        } else {
            binding.tilLastName.error = null
        }

        if (ageStr.isNotEmpty()) {
            val age = try {
                ageStr.toInt()
            } catch (e: Exception) {
                binding.tilAge.error = "Geçerli bir yaş giriniz"
                return false
            }
            
            if (age < 13 || age > 150) {
                binding.tilAge.error = "Yaş 13-150 arasında olmalıdır"
                return false
            } else {
                binding.tilAge.error = null
            }
        }

        return true
    }

    private fun saveData() {
        val editProfileActivity = activity as EditProfileActivity
        editProfileActivity.firstName = binding.etFirstName.text.toString().trim()
        editProfileActivity.lastName = binding.etLastName.text.toString().trim()
        
        val ageStr = binding.etAge.text.toString().trim()
        editProfileActivity.age = if (ageStr.isNotEmpty()) {
            try {
                ageStr.toInt()
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}










