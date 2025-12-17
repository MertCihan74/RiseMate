package com.parthenios.risemate.ui.matching

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.parthenios.risemate.R
import com.parthenios.risemate.auth.AuthRepository
import com.parthenios.risemate.databinding.ActivityMatchingBinding
import com.parthenios.risemate.ui.auth.LoginActivity
import com.parthenios.risemate.ui.explore.ExploreActivity
import com.parthenios.risemate.ui.profile.ProfileActivity

class MatchingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatchingBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            enableEdgeToEdge()
            
            binding = ActivityMatchingBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Sadece yan ve alt padding uygula, üst padding uygulama
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
                insets
            }
            
            authRepository = AuthRepository()
            
            setupToolbar()
            setupUI()
            setupBottomNavigation()
            setupDrawerNavigation()
        } catch (e: Exception) {
            // Hata durumunda basit bir mesaj göster
            android.widget.Toast.makeText(this, "Hata: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
    }

    private fun setupDrawerNavigation() {
        binding.navigationDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_matching -> {
                    // Zaten eşleşmeler sayfasındayız
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Ayarlar yakında gelecek!", Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_help -> {
                    Toast.makeText(this, "Yardım yakında gelecek!", Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_logout -> {
                    try {
                        // Fragment'i temizle
                        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                        if (currentFragment != null) {
                            supportFragmentManager.beginTransaction()
                                .remove(currentFragment)
                                .commitAllowingStateLoss()
                        }
                        authRepository.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } catch (e: Exception) {
                        authRepository.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupUI() {
        try {
            binding.apply {
                tvTitle.text = "Eşleşmelerin"
                tvSubtitle.text = "Seninle eşleşen kullanıcıları burada görebilirsin"

                // Mock kartlar kaldırıldı; sadece boş durum text'i gösterilecek
                tvNoMatches.text = "Henüz eşleşmeniz bulunmuyor. Keşfet sayfasından kullanıcıları inceleyin!"
                tvNoMatches.visibility = android.view.View.VISIBLE
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "UI Hatası: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_matching -> {
                    // Zaten eşleşme sayfasındayız
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
        
        // Eşleşme sayfasını seçili göster
        binding.bottomNavigation.selectedItemId = R.id.nav_matching
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(binding.navigationDrawer)
                true
            }
            else -> super.onOptionsItemSelected(item)
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
