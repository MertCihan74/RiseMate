package com.parthenios.risemate.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.parthenios.risemate.databinding.FragmentPersonalInfoBinding

class PersonalInfoFragment : Fragment() {
    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    // Username'in kullanıcı tarafından manuel değiştirilip değiştirilmediğini takip et
    private var isUsernameManuallyEdited = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Google kullanıcısı ise ad soyad otomatik doldur
        autoFillGoogleUserInfo()

        // Ad veya soyad değiştiğinde username'i otomatik güncelle
        setupAutoUsername()

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

    private fun setupAutoUsername() {
        val nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Kullanıcı manuel olarak username değiştirmediyse otomatik güncelle
                if (!isUsernameManuallyEdited) {
                    updateUsernameAutomatically()
                }
            }
        }

        binding.etFirstName.addTextChangedListener(nameWatcher)
        binding.etLastName.addTextChangedListener(nameWatcher)

        // Username alanına odaklanıldığında manuel düzenleme başladı say
        binding.etUsername.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                isUsernameManuallyEdited = true
            }
        }
    }

    private fun updateUsernameAutomatically() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()

        if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
            val suggestedUsername = generateUsername(firstName, lastName)
            binding.etUsername.setText(suggestedUsername)
        }
    }

    private fun autoFillGoogleUserInfo() {
        val registerActivity = activity as RegisterActivity
        val authRepository = com.parthenios.risemate.auth.AuthRepository()
        val currentUser = authRepository.getCurrentUser()

        // Google kullanıcısı ise email'i her zaman göster
        if (currentUser != null && currentUser.email != null) {
            binding.etEmail.setText(currentUser.email)
            registerActivity.email = currentUser.email!!

            var firstName = ""
            var lastName = ""

            if (currentUser.displayName != null) {
                val displayName = currentUser.displayName!!
                val nameParts = displayName.split(" ")

                if (nameParts.isNotEmpty()) {
                    // İlk isim ad kısmına
                    binding.etFirstName.setText(nameParts[0])
                    firstName = nameParts[0]
                    registerActivity.firstName = nameParts[0]
                }

                if (nameParts.size > 1) {
                    if (nameParts.size == 2) {
                        // 2 kelime: ilki isim, ikincisi soyad
                        binding.etFirstName.setText(nameParts[0])
                        binding.etLastName.setText(nameParts[1])
                        firstName = nameParts[0]
                        lastName = nameParts[1]
                        registerActivity.firstName = nameParts[0]
                        registerActivity.lastName = nameParts[1]
                    } else if (nameParts.size >= 3) {
                        // 3+ kelime: ilk 2'si isim, geri kalanı soyad
                        val firstNamePart = "${nameParts[0]} ${nameParts[1]}"
                        val lastNamePart = nameParts.subList(2, nameParts.size).joinToString(" ")
                        binding.etFirstName.setText(firstNamePart)
                        binding.etLastName.setText(lastNamePart)
                        firstName = firstNamePart
                        lastName = lastNamePart
                        registerActivity.firstName = firstNamePart
                        registerActivity.lastName = lastNamePart
                    }
                }

                // Username için ad_soyad + rastgele sayı oluştur (email yerine)
                val suggestedUsername = generateUsername(firstName, lastName)
                binding.etUsername.setText(suggestedUsername)
                registerActivity.username = suggestedUsername
            }
        } else if (registerActivity.email.isNotEmpty()) {
            // Email zaten dolu (normal kayıt), email'i göster
            binding.etEmail.setText(registerActivity.email)
            binding.etFirstName.requestFocus()
        }
    }

    private fun generateUsername(firstName: String, lastName: String): String {
        // Türkçe karakterleri düzelt
        val turkishChars = mapOf(
            'ç' to 'c', 'Ç' to 'C',
            'ğ' to 'g', 'Ğ' to 'G',
            'ı' to 'i', 'İ' to 'I',
            'ö' to 'o', 'Ö' to 'O',
            'ş' to 's', 'Ş' to 'S',
            'ü' to 'u', 'Ü' to 'U'
        )

        fun normalizeText(text: String): String {
            return text.map { turkishChars[it] ?: it }
                .joinToString("")
                .lowercase()
                .replace(" ", "")
                .replace(Regex("[^a-z0-9]"), "")
        }

        val normalizedFirst = normalizeText(firstName)
        val normalizedLast = normalizeText(lastName)

        // Rastgele 3 haneli sayı ekle
        val randomNum = (100..999).random()

        return when {
            normalizedFirst.isNotEmpty() && normalizedLast.isNotEmpty() ->
                "${normalizedFirst}_${normalizedLast}_$randomNum"
            normalizedFirst.isNotEmpty() ->
                "${normalizedFirst}_$randomNum"
            else ->
                "user_$randomNum"
        }
    }

    private fun validateInputs(): Boolean {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()

        if (firstName.isEmpty()) {
            binding.tilFirstName.error = "Ad gerekli"
            return false
        }

        if (lastName.isEmpty()) {
            binding.tilLastName.error = "Soyad gerekli"
            return false
        }

        if (username.isEmpty()) {
            binding.tilUsername.error = "Kullanıcı adı gerekli"
            return false
        }

        if (username.length < 3) {
            binding.tilUsername.error = "Kullanıcı adı en az 3 karakter olmalı"
            return false
        }

        return true
    }

    private fun saveData() {
        val registerActivity = activity as RegisterActivity
        registerActivity.firstName = binding.etFirstName.text.toString().trim()
        registerActivity.lastName = binding.etLastName.text.toString().trim()
        registerActivity.username = binding.etUsername.text.toString().trim()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}