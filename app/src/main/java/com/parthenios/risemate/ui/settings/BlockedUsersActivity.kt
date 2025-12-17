package com.parthenios.risemate.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.parthenios.risemate.auth.AuthRepository
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.ActivityBlockedUsersBinding
import com.parthenios.risemate.utils.UserPreferences
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BlockedUsersActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBlockedUsersBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: BlockedUsersAdapter
    private val blockedUsers = mutableListOf<User>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityBlockedUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        authRepository = AuthRepository()
        userPreferences = UserPreferences(this)
        firestore = FirebaseFirestore.getInstance()
        
        setupToolbar()
        setupRecyclerView()
        loadBlockedUsers()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Engellenenler"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = BlockedUsersAdapter(
            blockedUsers,
            onUnblockClick = { user ->
                showUnblockDialog(user)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BlockedUsersActivity)
            adapter = this@BlockedUsersActivity.adapter
        }
    }
    
    private fun loadBlockedUsers() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı giriş yapmamış", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Mevcut kullanıcının blockedUsers listesini al
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()
                
                val blockedUserIds = (userDoc.get("blockedUsers") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
                
                if (blockedUserIds.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                    return@launch
                }
                
                // Engellenen kullanıcıların bilgilerini al
                val users = mutableListOf<User>()
                for (userId in blockedUserIds) {
                    try {
                        val result = authRepository.getUserFromFirestore(userId)
                        if (result.isSuccess) {
                            result.getOrNull()?.let { users.add(it) }
                        }
                    } catch (_: Exception) {
                        // Kullanıcı bulunamadı, devam et
                    }
                }
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    
                    if (users.isEmpty()) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        blockedUsers.clear()
                        blockedUsers.addAll(users)
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    Toast.makeText(
                        this@BlockedUsersActivity,
                        "Engellenenler yüklenirken hata: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun showUnblockDialog(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Engeli Kaldır")
            .setMessage("${user.firstName} ${user.lastName} kullanıcısının engelini kaldırmak istediğinize emin misiniz?")
            .setPositiveButton("Engeli Kaldır") { _, _ ->
                unblockUser(user)
            }
            .setNegativeButton("İptal", null)
            .show()
    }
    
    private fun unblockUser(user: User) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı giriş yapmamış", Toast.LENGTH_SHORT).show()
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Mevcut blockedUsers listesini al
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()
                
                val blockedUserIds = (userDoc.get("blockedUsers") as? List<*>)
                    ?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                
                // Kullanıcıyı listeden çıkar
                blockedUserIds.remove(user.uid)
                
                // Firestore'da güncelle
                firestore.collection("users")
                    .document(currentUser.uid)
                    .update("blockedUsers", blockedUserIds)
                    .await()
                
                // Lokal kullanıcı bilgisini güncelle
                val localUser = userPreferences.getUser()
                if (localUser != null) {
                    val updatedUser = localUser.copy(blockedUsers = blockedUserIds)
                    userPreferences.saveUser(updatedUser)
                }
                
                withContext(Dispatchers.Main) {
                    // Listeden kaldır
                    val index = blockedUsers.indexOfFirst { it.uid == user.uid }
                    if (index != -1) {
                        blockedUsers.removeAt(index)
                        adapter.notifyItemRemoved(index)
                    }
                    
                    // Liste boşsa empty state göster
                    if (blockedUsers.isEmpty()) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                    
                    Toast.makeText(
                        this@BlockedUsersActivity,
                        "${user.firstName} ${user.lastName} engeli kaldırıldı",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@BlockedUsersActivity,
                        "Engel kaldırılırken hata: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}








