package com.parthenios.risemate.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.parthenios.risemate.R
import com.parthenios.risemate.auth.AuthRepository
import com.parthenios.risemate.data.FollowRepository
import com.parthenios.risemate.databinding.ActivityFollowingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FollowingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFollowingBinding
    private lateinit var followRepository: FollowRepository
    private lateinit var adapter: FollowingAdapter
    private lateinit var authRepository: AuthRepository
    private var userId: String? = null

    companion object {
        const val EXTRA_USER_ID = "user_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        userId = intent.getStringExtra(EXTRA_USER_ID)
        followRepository = FollowRepository()
        authRepository = AuthRepository()

        setupToolbar()
        setupRecyclerView()
        loadFollowing()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        binding.tvTitle.text = "Takip Edilenler"
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = FollowingAdapter { userId ->
            // Kullanıcı profiline git
            val intent = Intent(this, ProfileActivity::class.java)

            // Eğer tıklanan kullanıcı kendi profili ise, kendi profili olarak aç
            val currentUserId = authRepository.getCurrentUser()?.uid
            if (userId == currentUserId) {
                // Kendi profili - user objesi gönderme, kendi profili olarak açılacak
                startActivity(intent)
            } else {
                // Başka kullanıcının profili
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        }
        binding.recyclerViewFollowing.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFollowing.adapter = adapter
    }

    private fun loadFollowing() {
        val uid = userId ?: return

        binding.progressBar.visibility = android.view.View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = followRepository.getFollowing(uid)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = android.view.View.GONE

                    if (result.isSuccess) {
                        val following = result.getOrNull() ?: emptyList()
                        if (following.isEmpty()) {
                            binding.tvEmpty.visibility = android.view.View.VISIBLE
                            binding.recyclerViewFollowing.visibility = android.view.View.GONE
                        } else {
                            binding.tvEmpty.visibility = android.view.View.GONE
                            binding.recyclerViewFollowing.visibility = android.view.View.VISIBLE
                            adapter.submitList(following)
                        }
                    } else {
                        Toast.makeText(this@FollowingActivity, "Takip edilenler yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@FollowingActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
