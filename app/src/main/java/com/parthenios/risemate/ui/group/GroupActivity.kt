package com.parthenios.risemate.ui.group

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.parthenios.risemate.MainActivity
import com.parthenios.risemate.R
import com.parthenios.risemate.databinding.ActivityGroupBinding
import com.parthenios.risemate.ui.auth.LoginActivity
import com.parthenios.risemate.ui.profile.ProfileActivity
import com.parthenios.risemate.ui.settings.SettingsActivity
import com.parthenios.risemate.ui.social.SocialActivity
import com.parthenios.risemate.utils.GroupInitializer
import com.parthenios.risemate.utils.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)

        if (FirebaseAuth.getInstance().currentUser == null || !userPreferences.isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Status bar ve navigation bar renkleri
        window.statusBarColor = resources.getColor(R.color.primary_color, theme)
        window.navigationBarColor = resources.getColor(R.color.white, theme)

        setupToolbar()
        setupTabs()
        setupDrawerNavigation()
        loadDrawerHeader()

        // Firebase'de grupları oluştur (yoksa)
        lifecycleScope.launch(Dispatchers.IO) {
            GroupInitializer.initializeGroupsIfNeeded()
        }

        if (savedInstanceState == null) {
            showGroupListFragment("common")
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(binding.navigationDrawer)
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Ortak"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Bildiğim"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Öğrenmek İstediğim"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val filterType = when (tab.position) {
                    0 -> "common" // Ortak gruplar (proje + şehir)
                    1 -> "known"
                    2 -> "wanted"
                    else -> "common"
                }
                showGroupListFragment(filterType)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun showGroupListFragment(filterType: String) {
        val fragment = GroupListFragment.newInstance(filterType)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setupDrawerNavigation() {
        binding.navigationDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dating -> {
                    startActivity(Intent(this@GroupActivity, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_social -> {
                    startActivity(Intent(this@GroupActivity, SocialActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_groups -> {
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_forum -> {
                    startActivity(Intent(this@GroupActivity, com.parthenios.risemate.ui.forum.QuestionsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this@GroupActivity, ProfileActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_notifications -> {
                    openNotificationSettings()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this@GroupActivity, SettingsActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_help -> {
                    sendSupportEmail()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_logout -> {
                    performLogout()
                    true
                }
                else -> false
            }
        }
    }

    private fun openNotificationSettings() {
        try {
            val intent = Intent()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                intent.action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
            } else {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", packageName)
                intent.putExtra("app_uid", applicationInfo.uid)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Bildirim ayarları açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendSupportEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("risemateapp@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "RiseMate Destek Talebi")
            }
            startActivity(Intent.createChooser(intent, "E-posta Gönder"))
        } catch (e: Exception) {
            Toast.makeText(this, "E-posta uygulaması bulunamadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogout() {
        userPreferences.clearUser()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun loadDrawerHeader() {
        val user = userPreferences.getUser()
        val headerView = binding.navigationDrawer.getHeaderView(0)

        if (user != null) {
            val tvUserName = headerView.findViewById<android.widget.TextView>(R.id.tvUserName)
            tvUserName?.text = "${user.firstName} ${user.lastName}"

            val ivProfilePhoto = headerView.findViewById<android.widget.ImageView>(R.id.ivProfilePhoto)
            if (user.photoUrl.isNotEmpty()) {
                Glide.with(this).load(user.photoUrl).circleCrop().placeholder(R.drawable.ic_profile).into(ivProfilePhoto!!)
            } else {
                ivProfilePhoto?.setImageResource(R.drawable.ic_profile)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadDrawerHeader()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(binding.navigationDrawer)) {
            binding.drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
