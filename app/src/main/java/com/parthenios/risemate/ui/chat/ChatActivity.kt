package com.parthenios.risemate.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.auth.AuthRepository
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.ActivityChatBinding
import com.parthenios.risemate.utils.LocaleHelper
import com.parthenios.risemate.utils.UserPreferences
import com.parthenios.risemate.utils.ReportHelper
import com.parthenios.risemate.ui.call.CallActivity
import com.parthenios.risemate.data.CallType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.updateBaseContextLocale(newBase))
    }
    
    private lateinit var binding: ActivityChatBinding
    private var otherUser: User? = null
    private var matchId: String? = null
    private lateinit var authRepository: AuthRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var firestore: FirebaseFirestore
    private var blockUserJob: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityChatBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            authRepository = AuthRepository()
            userPreferences = UserPreferences(this)
            firestore = FirebaseFirestore.getInstance()
            
            // Diğer kullanıcı bilgisini al
            otherUser = intent.getSerializableExtra("otherUser") as? User
            matchId = intent.getStringExtra("matchId")
            
            if (otherUser != null) {
                setupToolbar()
                showChatFragment()
            } else {
                Toast.makeText(this, "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Chat açılırken hata: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    
    private var onlineStatusListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var onlineStatusHandler: android.os.Handler? = null
    private var onlineStatusRunnable: Runnable? = null
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false) // Default title'ı gizle
        }
        
        // Custom title'ı ayarla
        binding.tvUserName.text = "${otherUser?.firstName} ${otherUser?.lastName}"
        
        // Toolbar'a tıklayınca profil göster
        binding.tvUserName.setOnClickListener {
            if (otherUser != null) {
                val intent = Intent(this@ChatActivity, com.parthenios.risemate.ui.profile.ProfileActivity::class.java)
                intent.putExtra("user", otherUser)
                startActivity(intent)
            }
        }
        
        // Sesli arama butonu
        binding.btnVoiceCall.setOnClickListener {
            startCall(CallType.VOICE)
        }
        
        // Görüntülü arama butonu
        binding.btnVideoCall.setOnClickListener {
            startCall(CallType.VIDEO)
        }
        
        // Çevrimiçi durumunu dinle
        if (otherUser != null) {
            loadOnlineStatus(otherUser!!.uid)
        }
    }
    
    private fun startCall(callType: String) {
        if (otherUser == null) {
            Toast.makeText(this, "Kullanıcı bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = Intent(this, CallActivity::class.java).apply {
            putExtra(CallActivity.EXTRA_OTHER_USER, otherUser)
            putExtra(CallActivity.EXTRA_CALL_TYPE, callType)
            putExtra(CallActivity.EXTRA_IS_INCOMING, false)
        }
        startActivity(intent)
    }
    
    private fun loadOnlineStatus(userId: String) {
        // Firestore listener ile gerçek zamanlı güncellemeleri dinle
        onlineStatusListener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val hideOnline = snapshot.getBoolean("hideOnlineStatus") ?: false
                    val hideLastSeen = snapshot.getBoolean("hideLastSeen") ?: false
                    updateOnlineStatusUI(
                        snapshot.getBoolean("isOnline") ?: false, 
                        snapshot.getLong("lastSeen") ?: 0L,
                        hideOnline,
                        hideLastSeen
                    )
                }
            }
        
        // Periyodik olarak durumu kontrol et (5 dakika timeout için)
        onlineStatusHandler = android.os.Handler(android.os.Looper.getMainLooper())
        onlineStatusRunnable = object : Runnable {
            override fun run() {
                if (onlineStatusListener != null && otherUser != null) {
                    firestore.collection("users")
                        .document(otherUser!!.uid)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val hideOnline = snapshot.getBoolean("hideOnlineStatus") ?: false
                                val hideLastSeen = snapshot.getBoolean("hideLastSeen") ?: false
                                updateOnlineStatusUI(
                                    snapshot.getBoolean("isOnline") ?: false, 
                                    snapshot.getLong("lastSeen") ?: 0L,
                                    hideOnline,
                                    hideLastSeen
                                )
                            }
                        }
                    // Her 30 saniyede bir kontrol et
                    onlineStatusHandler?.postDelayed(this, 30 * 1000)
                }
            }
        }
        onlineStatusRunnable?.let { 
            onlineStatusHandler?.postDelayed(it, 30 * 1000)
        }
    }
    
    private fun updateOnlineStatusUI(isOnline: Boolean, lastSeen: Long, hideOnline: Boolean = false, hideLastSeen: Boolean = false) {
        val now = System.currentTimeMillis()
        val timeSinceLastSeen = now - lastSeen
        
        // Gizlilik ayarlarını kontrol et
        if (hideOnline && hideLastSeen) {
            binding.tvOnlineStatus.text = ""
            return
        }
        
        // Çevrimiçi olması için hem isOnline=true hem de lastSeen 5 dakikadan yeni olmalı
        val isActuallyOnline = isOnline && lastSeen > 0 && timeSinceLastSeen < (5 * 60 * 1000) // 5 dakika
        
        binding.tvOnlineStatus.text = if (isActuallyOnline && !hideOnline) {
            "🟢 Çevrimiçi"
        } else if (!hideLastSeen) {
            if (lastSeen > 0) {
                val diff = now - lastSeen
                val minutesAgo = (diff / (60 * 1000)).toInt()
                
                when {
                    minutesAgo < 1 -> "Son görülme: Şimdi"
                    minutesAgo < 60 -> "Son görülme: $minutesAgo dk önce"
                    minutesAgo < 1440 -> "Son görülme: ${minutesAgo / 60} saat önce"
                    else -> "Son görülme: ${minutesAgo / 1440} gün önce"
                }
            } else {
                ""
            }
        } else {
            ""
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Tüm listener, handler ve job'ları temizle
        onlineStatusListener?.remove()
        onlineStatusListener = null
        onlineStatusRunnable?.let { onlineStatusHandler?.removeCallbacks(it) }
        onlineStatusRunnable = null
        onlineStatusHandler = null
        blockUserJob?.cancel()
        blockUserJob = null
    }
    
    private fun showChatFragment() {
        try {
            val chatFragment = ChatFragment.newInstance(otherUser!!, matchId)
            replaceFragment(chatFragment)
        } catch (e: Exception) {
            Toast.makeText(this, "Chat fragment oluşturulurken hata: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        
        // Arama menüsünü bul ve ayarla
        val searchItem = menu?.findItem(R.id.menu_search)
        val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView
        
        // SearchView'i genişletilmiş olarak ayarla
        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }
            
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Arama kapatıldığında filtreyi temizle
                val fragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id) as? ChatFragment
                fragment?.filterMessages("")
                return true
            }
        })
        
        searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                // ChatFragment'a arama sorgusu gönder
                val fragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id) as? ChatFragment
                fragment?.filterMessages(newText ?: "")
                return true
            }
        })
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_report_user -> {
                showReportUserBottomSheet()
                true
            }
            R.id.menu_block_user -> {
                showBlockUserDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showReportUserBottomSheet() {
        if (otherUser == null) return
        
        val currentFirebaseUser = authRepository.getCurrentUser()
        val currentUserData = userPreferences.getUser()
        
        if (currentFirebaseUser == null || currentUserData == null) {
            Toast.makeText(this, "Oturum bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }
        
        ReportHelper.showReportBottomSheet(
            context = this,
            reporterId = currentFirebaseUser.uid,
            reporterName = "${currentUserData.firstName} ${currentUserData.lastName}",
            reportedUser = otherUser!!,
            source = "chat",
            onBlockRequested = {
                blockUser()
            }
        )
    }
    
    private fun showBlockUserDialog() {
        if (otherUser == null) return
        
        AlertDialog.Builder(this)
            .setTitle("Kullanıcıyı Engelle")
            .setMessage("${otherUser!!.firstName} ${otherUser!!.lastName} kullanıcısını engellemek istediğinize emin misiniz? Engellediğiniz kullanıcıyı keşfet, eşleşme ve mesajlaşma bölümlerinde göremezsiniz.")
            .setPositiveButton("Engelle") { _, _ ->
                blockUser()
            }
            .setNegativeButton("İptal", null)
            .show()
    }
    
    private fun blockUser() {
        val currentUser = authRepository.getCurrentUser()
        val targetUser = otherUser
        
        if (currentUser == null || targetUser == null) {
            Toast.makeText(this, "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }
        
        blockUserJob?.cancel()
        blockUserJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = currentUser.uid
                val blockedUserId = targetUser.uid
                
                // Mevcut blockedUsers listesini al
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                val currentBlocked = userDoc.get("blockedUsers") as? List<*> ?: emptyList<String>()
                val blockedList = (currentBlocked as? List<String>)?.toMutableList() ?: mutableListOf()
                
                // Engellenecek kullanıcı zaten engellenmiş mi kontrol et
                if (blockedList.contains(blockedUserId)) {
                    withContext(Dispatchers.Main) {
                        if (!isFinishing && !isDestroyed) {
                            Toast.makeText(this@ChatActivity, "Bu kullanıcı zaten engellenmiş", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@launch
                }
                
                // İki kullanıcı arasındaki match'leri ve mesajları sil
                deleteMatchesBetweenUsers(userId, blockedUserId)
                
                // Engellenen kullanıcıyı listeye ekle
                blockedList.add(blockedUserId)
                
                // Firestore'da güncelle
                firestore.collection("users")
                    .document(userId)
                    .update("blockedUsers", blockedList)
                    .await()
                
                // Lokal kullanıcı bilgisini güncelle
                val localUser = userPreferences.getUser()
                if (localUser != null) {
                    val updatedUser = localUser.copy(blockedUsers = blockedList)
                    userPreferences.saveUser(updatedUser)
                }
                
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(this@ChatActivity, "Kullanıcı engellendi", Toast.LENGTH_SHORT).show()
                        finish() // Chat ekranından çık
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatActivity", "Kullanıcı engellenirken hata", e)
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(this@ChatActivity, "Kullanıcı engellenirken hata: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    private suspend fun deleteMatchesBetweenUsers(userId: String, blockedUserId: String) {
        try {
            // Match'leri bul (user1Id = userId, user2Id = blockedUserId)
            val matches1 = firestore.collection("matches")
                .whereEqualTo("user1Id", userId)
                .whereEqualTo("user2Id", blockedUserId)
                .get()
                .await()
            
            // Match'leri bul (user1Id = blockedUserId, user2Id = userId)
            val matches2 = firestore.collection("matches")
                .whereEqualTo("user1Id", blockedUserId)
                .whereEqualTo("user2Id", userId)
                .get()
                .await()
            
            val allMatches = matches1.documents + matches2.documents
            
            for (matchDoc in allMatches) {
                val matchId = matchDoc.id
                
                // Bu match'e ait tüm mesajları sil
                val messages = firestore.collection("messages")
                    .whereEqualTo("matchId", matchId)
                    .get()
                    .await()
                
                for (msgDoc in messages.documents) {
                    try {
                        firestore.collection("messages").document(msgDoc.id).delete().await()
                    } catch (_: Exception) {}
                }
                
                // Match'i sil
                try {
                    firestore.collection("matches").document(matchId).delete().await()
                } catch (_: Exception) {}
            }
            
            android.util.Log.d("ChatActivity", "Deleted ${allMatches.size} matches between $userId and $blockedUserId")
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Match silme hatası", e)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
