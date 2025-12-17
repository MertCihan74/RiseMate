package com.parthenios.risemate.ui.forum

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.parthenios.risemate.MainActivity
import com.parthenios.risemate.R
import com.parthenios.risemate.databinding.ActivityQuestionsBinding
import com.parthenios.risemate.ui.auth.LoginActivity
import com.parthenios.risemate.ui.group.GroupActivity
import com.parthenios.risemate.ui.premium.PremiumActivity
import com.parthenios.risemate.ui.profile.ProfileActivity
import com.parthenios.risemate.ui.settings.SettingsActivity
import com.parthenios.risemate.ui.social.SocialActivity
import com.parthenios.risemate.utils.PremiumManager
import com.parthenios.risemate.utils.UserPreferences
import kotlinx.coroutines.launch

class QuestionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionsBinding
    private lateinit var userPreferences: UserPreferences
    
    private var questionListFragment: QuestionListFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        window.statusBarColor = resources.getColor(R.color.primary_color, theme)

        setupToolbar()
        setupDrawer()
        setupTabs()
        setupFab()
        loadFragment()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(binding.navigationDrawer)
        }
        
        binding.btnLeaderboard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }
    }

    private fun setupDrawer() {
        loadDrawerHeader()
        
        binding.navigationDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dating -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_social -> {
                    startActivity(Intent(this, SocialActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_groups -> {
                    checkPremiumAndOpenGroups()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_forum -> {
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
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

    private fun loadDrawerHeader() {
        val user = userPreferences.getUser()
        val headerView = binding.navigationDrawer.getHeaderView(0)
        
        if (user != null) {
            val tvName = headerView.findViewById<android.widget.TextView>(R.id.tvUserName)
            val ivPhoto = headerView.findViewById<android.widget.ImageView>(R.id.ivProfilePhoto)
            
            tvName?.text = "${user.firstName} ${user.lastName}"
            
            if (user.photoUrl.isNotEmpty() && user.photoUrl.startsWith("data:image")) {
                try {
                    val base64Data = user.photoUrl.substringAfter("base64,")
                    val imageBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ivPhoto?.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    ivPhoto?.setImageResource(R.drawable.ic_profile)
                }
            }
        }
    }

    private fun setupTabs() {
        // Tab'ları ekle
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Tüm Sorular"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Benim İçin"))
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> questionListFragment?.setFilterMode(QuestionListFragment.FilterMode.ALL)
                    1 -> questionListFragment?.setFilterMode(QuestionListFragment.FilterMode.FOR_ME)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupFab() {
        binding.fabAskQuestion.setOnClickListener {
            startActivity(Intent(this, AskQuestionActivity::class.java))
        }
    }

    private fun loadFragment() {
        questionListFragment = QuestionListFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, questionListFragment!!)
            .commit()
    }

    private fun checkPremiumAndOpenGroups() {
        lifecycleScope.launch {
            val isPremium = PremiumManager.isPremium(this@QuestionsActivity)
            if (isPremium) {
                startActivity(Intent(this@QuestionsActivity, GroupActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@QuestionsActivity, PremiumActivity::class.java))
            }
        }
    }

    private fun performLogout() {
        userPreferences.clearUser()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        questionListFragment?.refreshQuestions()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(binding.navigationDrawer)) {
            binding.drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
