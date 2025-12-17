package com.parthenios.risemate.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: EditProfilePagerAdapter
    
    // Kullanıcı bilgilerini saklamak için
    var firstName: String = ""
    var lastName: String = ""
    var age: Int = 0
    var city: String = ""
    var knownSkills: List<String> = emptyList()
    var wantedSkills: List<String> = emptyList()
    
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Mevcut kullanıcı bilgilerini al
        currentUser = intent.getSerializableExtra("user") as? User
        if (currentUser != null) {
            firstName = currentUser!!.firstName
            lastName = currentUser!!.lastName
            age = currentUser!!.age
            city = currentUser!!.city
            knownSkills = currentUser!!.knownSkills
            wantedSkills = currentUser!!.wantedSkills
        }
        
        setupViewPager()
    }

    private fun setupViewPager() {
        viewPager = binding.viewPager
        adapter = EditProfilePagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false // Swipe'ı devre dışı bırak
    }

    fun goToNextStep() {
        if (viewPager.currentItem < adapter.itemCount - 1) {
            viewPager.currentItem = viewPager.currentItem + 1
        } else {
            // Düzenleme tamamlandı, kaydet ve geri dön
            saveProfile()
        }
    }

    fun goToPreviousStep() {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }
    
    fun saveProfile() {
        // ProfileFragment'teki updateProfile fonksiyonunu çağır
        val resultIntent = Intent().apply {
            putExtra("firstName", firstName)
            putExtra("lastName", lastName)
            putExtra("age", age)
            putExtra("city", city)
            putExtra("knownSkills", knownSkills.toTypedArray())
            putExtra("wantedSkills", wantedSkills.toTypedArray())
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

