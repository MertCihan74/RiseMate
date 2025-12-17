package com.parthenios.risemate.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.parthenios.risemate.databinding.FragmentCityBinding

class CityFragment : Fragment() {
    private var _binding: FragmentCityBinding? = null
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

    private val cityOptions = listOf("Şehrinizi seçin") + turkishCities

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCitySpinner()

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

    private fun setupCitySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cityOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCity.adapter = adapter
        binding.spinnerCity.prompt = "Şehrinizi seçin"
    }

    private fun validateInputs(): Boolean {
        val selectedPosition = binding.spinnerCity.selectedItemPosition
        return if (selectedPosition == 0) { // İlk item "Şehrinizi seçin" placeholder
            binding.tilCity.error = "Lütfen bir şehir seçin"
            false
        } else {
            binding.tilCity.error = null
            true
        }
    }

    private fun saveData() {
        val registerActivity = activity as RegisterActivity
        val selectedCity = binding.spinnerCity.selectedItem.toString()
        registerActivity.city = selectedCity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}