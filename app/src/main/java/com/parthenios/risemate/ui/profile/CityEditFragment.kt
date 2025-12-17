package com.parthenios.risemate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.parthenios.risemate.databinding.FragmentCityEditBinding

class CityEditFragment : Fragment() {
    private var _binding: FragmentCityEditBinding? = null
    private val binding get() = _binding!!

    private val turkishCities = listOf(
        "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin",
        "Aydın", "Balıkesir", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale",
        "Çankırı", "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum",
        "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkâri", "Hatay", "Isparta", "Mersin",
        "İstanbul", "İzmir", "Kars", "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir", "Kocaeli",
        "Konya", "Kütahya", "Malatya", "Manisa", "Kahramanmaraş", "Mardin", "Muğla", "Muş",
        "Nevşehir", "Niğde", "Ordu", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop", "Sivas",
        "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Şanlıurfa", "Uşak", "Van", "Yozgat", "Zonguldak",
        "Aksaray", "Bayburt", "Karaman", "Kırıkkale", "Batman", "Şırnak", "Bartın", "Ardahan",
        "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye", "Düzce"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCityEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCitySpinner()

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

    private fun setupCitySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            turkishCities
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCity.adapter = adapter
        
        // Mevcut şehri seçili göster
        val editProfileActivity = activity as? EditProfileActivity
        if (editProfileActivity != null && editProfileActivity.city.isNotEmpty()) {
            val cityIndex = turkishCities.indexOf(editProfileActivity.city)
            if (cityIndex >= 0) {
                binding.spinnerCity.setSelection(cityIndex)
            }
        }
    }

    private fun validateInputs(): Boolean {
        val selectedPosition = binding.spinnerCity.selectedItemPosition
        if (selectedPosition < 0) {
            binding.tilCity.error = "Lütfen bir şehir seçin"
            return false
        }
        binding.tilCity.error = null
        return true
    }

    private fun saveData() {
        val editProfileActivity = activity as EditProfileActivity
        val selectedCity = binding.spinnerCity.selectedItem.toString()
        editProfileActivity.city = selectedCity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}










