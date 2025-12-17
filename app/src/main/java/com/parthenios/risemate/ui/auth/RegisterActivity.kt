package com.parthenios.risemate.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.parthenios.risemate.databinding.ActivityRegisterBinding
import com.parthenios.risemate.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: RegisterPagerAdapter
    
    // Yeni e-posta kaydı mı yoksa Google ile giriş sonrası profil tamamlama mı?
    private var isNewRegistration: Boolean = false
    
    // Kullanıcı bilgilerini saklamak için
    var email: String = ""
    var password: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var username: String = ""
    var birthDate: java.util.Date? = null
    var age: Int = 0
    var city: String = ""
    var knownSkills: List<String> = emptyList()
    var wantedSkills: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Yeni kayıt mı kontrol et
        isNewRegistration = intent.getBooleanExtra("isNewRegistration", false)
        
        val authRepository = com.parthenios.risemate.auth.AuthRepository()
        
        // Eğer yeni kayıt değilse (Google ile giriş sonrası) kullanıcı kontrolü yap
        if (!isNewRegistration && authRepository.getCurrentUser() == null) {
            // Kullanıcı giriş yapmamış, LoginActivity'ye yönlendir
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupViewPager()
        
        // Yeni kayıt ise EmailFragment'tan başla, değilse Google kullanıcı kontrolü yap
        if (isNewRegistration) {
            viewPager.currentItem = 0 // EmailFragment'tan başla
        } else {
            checkGoogleUser()
        }
    }

    private fun checkGoogleUser() {
        val authRepository = com.parthenios.risemate.auth.AuthRepository()
        val currentUser = authRepository.getCurrentUser()
        
        if (currentUser != null && currentUser.email != null) {
            // Google kullanıcısı ise email'i otomatik doldur
            email = currentUser.email!!
            
            // Kullanıcının Firestore'da kayıtlı olup olmadığını kontrol et
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = authRepository.getUserFromFirestore(currentUser.uid)
                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            val user = result.getOrNull()
                            if (user != null && user.firstName.isNotEmpty()) {
                                // Kullanıcı tam kayıtlı, ana sayfaya git
                                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                                finish()
                            } else {
                                // Kullanıcı eksik bilgileri var, ilk sayfadan başla
                                viewPager.currentItem = 1 // Personal Info sayfasından başla
                            }
                        } else {
                            // Hata durumunda ilk sayfadan başla
                            viewPager.currentItem = 1
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        viewPager.currentItem = 1
                    }
                }
            }
        }
    }

    private fun setupViewPager() {
        viewPager = binding.viewPager
        adapter = RegisterPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false // Swipe'ı devre dışı bırak
    }

    fun goToNextStep() {
        if (viewPager.currentItem < adapter.itemCount - 1) {
            viewPager.currentItem = viewPager.currentItem + 1
        } else {
            // Kayıt tamamlandı, ana sayfaya git
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun goToPreviousStep() {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }
}
