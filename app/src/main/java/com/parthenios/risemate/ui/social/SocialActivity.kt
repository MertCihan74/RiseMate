package com.parthenios.risemate.ui.social

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.parthenios.risemate.MainActivity
import com.parthenios.risemate.R
import com.parthenios.risemate.databinding.ActivitySocialBinding
import com.parthenios.risemate.ui.auth.LoginActivity
import com.parthenios.risemate.ui.profile.EditProfileActivity
import com.parthenios.risemate.ui.profile.ProfileActivity
import com.parthenios.risemate.ui.settings.PolicyActivity
import com.parthenios.risemate.ui.settings.SettingsActivity
import com.parthenios.risemate.utils.LocaleHelper
import com.parthenios.risemate.utils.UserPreferences
import com.parthenios.risemate.utils.PremiumManager
import com.parthenios.risemate.ui.premium.PremiumActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SocialActivity : AppCompatActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.updateBaseContextLocale(newBase))
    }

    private lateinit var binding: ActivitySocialBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySocialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)

        // Kullanıcı giriş yapmış mı kontrol et
        if (FirebaseAuth.getInstance().currentUser == null || !userPreferences.isUserLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Sistem bar renkleri
        window.statusBarColor = resources.getColor(R.color.primary_color, theme)
        window.navigationBarColor = resources.getColor(R.color.white, theme)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupDrawerNavigation()
        setupFab()
        loadProfilePhoto()
        loadDrawerHeader()

        // Fragment'ları başlat
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SocialFeedFragment())
                .commit()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(binding.navigationDrawer)
        }
    }

    private fun setupDrawerNavigation() {
        binding.navigationDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dating -> {
                    // Eşleşme moduna geç - MainActivity'yi aç
                    startActivity(Intent(this@SocialActivity, MainActivity::class.java))
                    finish() // SocialActivity'yi kapat
                    true
                }
                R.id.nav_social -> {
                    // Zaten sosyal moddayız, drawer'ı kapat
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_groups -> {
                    checkPremiumAndOpenGroups()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_forum -> {
                    startActivity(Intent(this@SocialActivity, com.parthenios.risemate.ui.forum.QuestionsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this@SocialActivity, ProfileActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_notifications -> {
                    // Bildirim ayarları
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
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this@SocialActivity, SettingsActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_help -> {
                    // Destek e-postası
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("risemateapp@gmail.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "RiseMate Destek Talebi")
                            putExtra(Intent.EXTRA_TEXT, "\n\n---\nCihaz: ${android.os.Build.MODEL}\nAndroid: ${android.os.Build.VERSION.RELEASE}")
                        }
                        startActivity(Intent.createChooser(intent, "E-posta Gönder"))
                    } catch (e: Exception) {
                        Toast.makeText(this, "E-posta uygulaması bulunamadı", Toast.LENGTH_SHORT).show()
                    }
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_logout -> {
                    // Çıkış yap
                    userPreferences.clearUser()
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadProfilePhoto() {
        // Profile photo navigation drawer header'da olduğu için ayrı yükleme yapmıyoruz
        // loadDrawerHeader() metodunda yükleniyor
    }

    private fun loadDrawerHeader() {
        val user = userPreferences.getUser()
        val headerView = binding.navigationDrawer.getHeaderView(0)

        if (user != null) {
            val tvUserName = headerView.findViewById<android.widget.TextView>(R.id.tvUserName)
            tvUserName?.text = "${user.firstName} ${user.lastName}"

            val ivProfilePhoto = headerView.findViewById<android.widget.ImageView>(R.id.ivProfilePhoto)
            if (user.photoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(user.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(ivProfilePhoto!!)
            } else {
                ivProfilePhoto?.setImageResource(R.drawable.ic_profile)
            }
        }
    }

    private fun setupFab() {
        binding.fabCreatePost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Drawer header'ı güncelle
        loadDrawerHeader()
    }

    private fun checkPremiumAndOpenGroups() {
        lifecycleScope.launch {
            val isPremium = PremiumManager.isPremium(this@SocialActivity)
            if (isPremium) {
                startActivity(Intent(this@SocialActivity, com.parthenios.risemate.ui.group.GroupActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@SocialActivity, PremiumActivity::class.java))
            }
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(binding.navigationDrawer)) {
            binding.drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
