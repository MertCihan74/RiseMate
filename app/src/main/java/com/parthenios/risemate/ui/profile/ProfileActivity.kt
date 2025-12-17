package com.parthenios.risemate.ui.profile

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout
import com.parthenios.risemate.R
import com.parthenios.risemate.auth.AuthRepository
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.ActivityProfileBinding
import com.parthenios.risemate.utils.LocaleHelper

class ProfileActivity : AppCompatActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.updateBaseContextLocale(newBase))
    }
    private lateinit var binding: ActivityProfileBinding
    private lateinit var authRepository: AuthRepository
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        
        authRepository = AuthRepository()
        
        setupToolbar()
        setupTabs()

        // Başlangıçta profil bilgilerini göster
        showProfileInfo()
    }

    private fun setupToolbar() {
        // Geri butonuna tıklama
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Bilgiler"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Gönderiler"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showProfileInfo()
                    1 -> showUserPosts()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun showProfileInfo() {
        // ProfileFragment'i oluştur
        val profileFragment = ProfileFragment()

        // Intent'ten kullanıcı bilgilerini al
        user = intent.getSerializableExtra("user") as? User
        val userIdFromIntent = intent.getStringExtra("userId")
        val bundle = Bundle()
        if (user != null) {
            // Başka kullanıcının profili - fragment'e user bilgisini gönder
            bundle.putSerializable("user", user)
        } else if (!userIdFromIntent.isNullOrEmpty()) {
            bundle.putString("userId", userIdFromIntent)
        }
        if (!bundle.isEmpty) {
            profileFragment.arguments = bundle
        }

        // Fragment'i göster
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, profileFragment)
            .commit()
    }

    private fun showUserPosts() {
        // UserPostsFragment oluştur
        val userPostsFragment = UserPostsFragment()

        // Intent'ten kullanıcı bilgilerini al
        user = intent.getSerializableExtra("user") as? User
        val userIdFromIntent = intent.getStringExtra("userId")
        val bundle = Bundle()
        if (user != null) {
            // Başka kullanıcının profili - fragment'e user bilgisini gönder
            bundle.putSerializable("user", user)
        } else if (!userIdFromIntent.isNullOrEmpty()) {
            bundle.putString("userId", userIdFromIntent)
        }
        if (!bundle.isEmpty) {
            userPostsFragment.arguments = bundle
        }

        // Fragment'i göster
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, userPostsFragment)
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
