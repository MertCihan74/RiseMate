package com.parthenios.risemate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.parthenios.risemate.R
import com.parthenios.risemate.auth.AuthRepository
import com.parthenios.risemate.databinding.ActivityMainBinding
import com.parthenios.risemate.ui.auth.LoginActivity
import com.parthenios.risemate.ui.explore.ExploreFragment
import com.parthenios.risemate.ui.matching.MatchingFragment
import com.parthenios.risemate.ui.profile.ProfileFragment
import com.parthenios.risemate.ui.chat.ChatListFragment
import com.parthenios.risemate.utils.UserPreferences
import com.parthenios.risemate.utils.PremiumManager
import com.parthenios.risemate.utils.LocaleHelper
import com.parthenios.risemate.ui.premium.PremiumActivity
import com.parthenios.risemate.ui.call.IncomingCallService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import nl.joery.animatedbottombar.AnimatedBottomBar

class MainActivity : AppCompatActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.updateBaseContextLocale(newBase))
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var userPreferences: UserPreferences

    // App mode - Sadece Eşleşme modu (Sosyal ayrı activity)
    enum class AppMode {
        DATING // Keşfet, Eşleşmeler, Mesajlar
    }

    private var currentMode = AppMode.DATING

    // Fragment navigation state
    private var currentFragmentTag: String? = null
    @Volatile
    private var isNavigating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Sistem bar renkleri: üst (status) primary color, alt (nav) beyaz
        window.statusBarColor = resources.getColor(R.color.primary_color, theme)
        window.navigationBarColor = resources.getColor(R.color.white, theme)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightNavigationBars = true  // beyaz nav bar için koyu ikonlar
            isAppearanceLightStatusBars = false     // status bar koyu değil, açık ikonlar
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Sadece yan ve alt padding uygula, üst padding uygulama
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        
        authRepository = AuthRepository()
        userPreferences = UserPreferences(this)
        
        // Kullanıcı giriş yapmış mı kontrol et
        if (authRepository.getCurrentUser() == null || !userPreferences.isUserLoggedIn()) {
            // Kullanıcı giriş yapmamış, LoginActivity'ye yönlendir
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // Gelen aramaları dinlemek için servisi başlat
        startIncomingCallService()
        
        setupToolbar()
        setupDrawerNavigation()
        setupBottomNavigation()
        setupFab()
        applyBottomInset()
        loadProfilePhoto()
        loadDrawerHeader()
        
        // Varsayılan olarak Keşfet fragment'ini göster
        if (savedInstanceState == null) {
            showFragment("explore")
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Profil fotoğrafı güncellenmiş olabilir, tekrar yükle
        loadProfilePhoto()
        loadDrawerHeader()
        // Çevrimiçi durumunu güncelle
        updateOnlineStatus(true)
    }
    
    override fun onPause() {
        super.onPause()
        // Kullanıcı hala giriş yapmışsa çevrimdışı durumunu güncelle
        if (authRepository.getCurrentUser() != null && userPreferences.isUserLoggedIn()) {
            updateOnlineStatus(false)
        }
    }
    
    override fun onStop() {
        super.onStop()
        // Kullanıcı hala giriş yapmışsa çevrimdışı durumunu güncelle
        if (authRepository.getCurrentUser() != null && userPreferences.isUserLoggedIn()) {
            updateOnlineStatus(false)
        }
    }
    
    private var onlineStatusJob: Job? = null
    
    private fun updateOnlineStatus(isOnline: Boolean) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            onlineStatusJob?.cancel()
            onlineStatusJob = lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val currentTime = System.currentTimeMillis()
                    val updates = hashMapOf<String, Any>(
                        "isOnline" to isOnline,
                        "lastSeen" to currentTime
                    )
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUser.uid)
                        .update(updates)
                        .await()
                    
                    android.util.Log.d("MainActivity", "Durum güncellendi: isOnline=$isOnline, lastSeen=$currentTime")
                } catch (e: Exception) {
                    // Hata durumunda sessizce devam et
                    android.util.Log.e("MainActivity", "Durum güncellenirken hata: ${e.message}")
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        // Menü butonuna click listener ekle
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navigationDrawer)
        }
        
        // Profil butonuna click listener ekle
        binding.cardProfile.setOnClickListener {
            startActivity(Intent(this@MainActivity, com.parthenios.risemate.ui.profile.ProfileActivity::class.java))
        }
        
        // Filtre butonuna click listener ekle
        binding.btnFilter.setOnClickListener {
            // ExploreFragment'taki showFilterBottomSheet fonksiyonunu çağır
            val exploreFragment = supportFragmentManager.findFragmentByTag("explore") as? ExploreFragment
            exploreFragment?.showFilterBottomSheet()
        }
    }
    
    private fun updateFilterButtonVisibility(isVisible: Boolean) {
        binding.layoutFilterButton.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
    
    private fun updateFilterBadge(count: Int) {
        if (count > 0) {
            binding.tvFilterBadge.visibility = View.VISIBLE
            binding.tvFilterBadge.text = count.toString()
        } else {
            binding.tvFilterBadge.visibility = View.GONE
        }
    }
    
    private fun loadProfilePhoto() {
        val user = userPreferences.getUser()
        if (user != null && user.photoUrl.isNotEmpty()) {
            // Base64 data URL formatında fotoğraf varsa göster
            Glide.with(this)
                .load(user.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(binding.ivProfile)
        } else {
            // Fotoğraf yoksa varsayılan ikonu göster
            binding.ivProfile.setImageResource(R.drawable.ic_profile)
        }
    }
    
    private fun startIncomingCallService() {
        // NOT: Gelen arama servisi şimdilik devre dışı
        // Android 14+ için ek izinler gerekiyor
        // Arama başlatıldığında aktif olacak şekilde tasarlanacak
        /*
        try {
            IncomingCallService.start(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        */
    }
    
    fun onProfilePhotoUpdated() {
        // Profil fotoğrafı güncellendiğinde toolbar'daki fotoğrafı da güncelle
        loadProfilePhoto()
        loadDrawerHeader()
    }
    
    private fun loadDrawerHeader() {
        val user = userPreferences.getUser()
        val headerView = binding.navigationDrawer.getHeaderView(0)
        
        if (user != null) {
            // Kullanıcı adını ayarla
            val tvUserName = headerView.findViewById<android.widget.TextView>(R.id.tvUserName)
            tvUserName?.text = "${user.firstName} ${user.lastName}"
            
            // Profil fotoğrafını yükle
            val ivProfilePhoto = headerView.findViewById<android.widget.ImageView>(R.id.ivProfilePhoto)
            if (user.photoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(user.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(ivProfilePhoto)
            } else {
                ivProfilePhoto?.setImageResource(R.drawable.ic_profile)
            }
        }
    }

    private fun setupDrawerNavigation() {
        binding.navigationDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                // Eşleşme modu navigasyonu
                R.id.nav_dating -> {
                    switchToMode(AppMode.DATING)
                    showFragment("explore") // Varsayılan olarak explore göster
                    binding.drawerLayout.closeDrawers()
                    true
                }

                // Sosyal modu navigasyonu - yeni activity aç
                R.id.nav_social -> {
                    startActivity(Intent(this@MainActivity, com.parthenios.risemate.ui.social.SocialActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }

                // Grup bul navigasyonu - Premium kontrolü
                R.id.nav_groups -> {
                    checkPremiumAndOpenGroups()
                    binding.drawerLayout.closeDrawers()
                    true
                }

                // Soru-Cevap forum
                R.id.nav_forum -> {
                    startActivity(Intent(this@MainActivity, com.parthenios.risemate.ui.forum.QuestionsActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }

                // Genel navigasyon
                R.id.nav_profile -> {
                    startActivity(Intent(this@MainActivity, com.parthenios.risemate.ui.profile.ProfileActivity::class.java))
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_notifications -> {
                    openNotificationSettings()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this@MainActivity, com.parthenios.risemate.ui.settings.SettingsActivity::class.java))
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

    private fun switchToMode(newMode: AppMode) {
        if (currentMode == newMode) return

        currentMode = newMode

        // UI'ı mode'a göre güncelle
        updateUIForMode()
    }

    private fun updateUIForMode() {
        when (currentMode) {
            AppMode.DATING -> {
                // Eşleşme modu: Bottom nav göster, toolbar göster, FAB gizle
                binding.bottomNavigation.visibility = View.VISIBLE
                binding.toolbar.visibility = View.VISIBLE
                binding.fabCreatePost?.visibility = View.GONE
            }
        }
    }
    
    private fun sendSupportEmail() {
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
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                when (newTab.id) {
                    R.id.nav_explore -> showFragment("explore")
                    R.id.nav_matching -> showFragment("matching")
                    R.id.nav_chat -> showFragment("chat")
                    R.id.nav_profile -> {
                        startActivity(Intent(this@MainActivity, com.parthenios.risemate.ui.profile.ProfileActivity::class.java))
                        // Seçimi Explore'da tut
                        binding.bottomNavigation.selectTabById(R.id.nav_explore, true)
                    }
                }
            }
        })
        // Varsayılan sekme
        binding.bottomNavigation.selectTabById(R.id.nav_explore, true)
    }

    private fun setupFab() {
        binding.fabCreatePost.setOnClickListener {
            startActivity(Intent(this, com.parthenios.risemate.ui.social.CreatePostActivity::class.java))
        }
    }

    private fun applyBottomInset() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val extraBottom = systemBars.bottom
            val baseMargin = resources.getDimensionPixelSize(R.dimen.bottom_nav_margin_base)
            val params = view.layoutParams as? ViewGroup.MarginLayoutParams
            params?.let {
                it.bottomMargin = baseMargin + extraBottom
                view.layoutParams = it
            }
            insets
        }
    }

    private fun showFragment(tag: String) {
        // Aynı fragment'a tekrar tıklanmışsa veya navigasyon devam ediyorsa işlem yapma
        if (currentFragmentTag == tag || isNavigating) return
        
        // Activity destroy edilmişse veya state kaybı durumunda işlem yapma
        if (isFinishing || isDestroyed) return
        if (supportFragmentManager.isStateSaved) return
        
        isNavigating = true
        currentFragmentTag = tag
        
        try {
            val fragmentManager = supportFragmentManager
            
            // Mevcut fragment'ları gizle
            val transaction = fragmentManager.beginTransaction()
            
            // Animasyon ekle - yumuşak geçiş
            transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            
            // Tüm mevcut fragment'ları gizle
            fragmentManager.fragments.forEach { fragment ->
                if (fragment.isVisible) {
                    transaction.hide(fragment)
                }
            }
            
            // Hedef fragment'ı bul veya oluştur
            var targetFragment = fragmentManager.findFragmentByTag(tag)
            
            if (targetFragment == null) {
                targetFragment = when (tag) {
                    "explore" -> ExploreFragment().also { fragment ->
                        fragment.onFilterChanged = { count ->
                            updateFilterBadge(count)
                        }
                    }
                    "matching" -> MatchingFragment()
                    "chat" -> ChatListFragment()
                    else -> {
                        isNavigating = false
                        return
                    }
                }
                transaction.add(R.id.fragment_container, targetFragment, tag)
            } else {
                transaction.show(targetFragment)
            }
            
            // Filtre butonunun görünürlüğünü ayarla (sadece Explore'da görünür)
            updateFilterButtonVisibility(tag == "explore")
            
            transaction.commitAllowingStateLoss()
            
            // Fragment transaction tamamlandığında kilidi aç
            fragmentManager.executePendingTransactions()
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Fragment transaction error: ${e.message}")
        } finally {
            // Kilidi aç - her durumda
            binding.root.postDelayed({
                isNavigating = false
            }, 150)
        }
    }
    
    private fun showChatList() {
        showFragment("chat")
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
            // Fallback: Genel uygulama ayarlarını aç
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = android.net.Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    private fun checkPremiumAndOpenGroups() {
        lifecycleScope.launch {
            val isPremium = PremiumManager.isPremium(this@MainActivity)
            if (isPremium) {
                // Premium kullanıcı - direkt grupları aç
                startActivity(Intent(this@MainActivity, com.parthenios.risemate.ui.group.GroupActivity::class.java))
            } else {
                // Premium değil - ödeme sayfasına yönlendir
                startActivity(Intent(this@MainActivity, PremiumActivity::class.java))
            }
        }
    }

    private fun performLogout() {
        try {
            // Önce navigasyon kilidini koy
            isNavigating = true
            
            // Coroutine job'ları iptal et
            onlineStatusJob?.cancel()
            
            // Önce local verileri temizle
            userPreferences.clearUser()
            
            // Tüm fragment'leri güvenli şekilde temizle
            try {
                val fragments = supportFragmentManager.fragments.toList()
                if (fragments.isNotEmpty() && !supportFragmentManager.isStateSaved) {
                    val transaction = supportFragmentManager.beginTransaction()
                    fragments.forEach { fragment ->
                        transaction.remove(fragment)
                    }
                    transaction.commitAllowingStateLoss()
                    supportFragmentManager.executePendingTransactions()
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Fragment temizleme hatası: ${e.message}")
            }
            
            // Sonra Firebase'den çıkış yap
            authRepository.signOut()
            
            // Yeni activity başlat ve mevcut activity'yi kapat
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            // Hata durumunda yine de logout yap
            userPreferences.clearUser()
            authRepository.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
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
    
    override fun onDestroy() {
        super.onDestroy()
        // Coroutine job'ları iptal et
        onlineStatusJob?.cancel()
        isNavigating = false
    }
}
