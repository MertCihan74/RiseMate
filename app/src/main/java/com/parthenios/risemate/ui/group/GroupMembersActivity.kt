package com.parthenios.risemate.ui.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.data.GroupMember
import com.parthenios.risemate.databinding.ActivityGroupMembersBinding
import com.parthenios.risemate.ui.profile.ProfileActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GroupMembersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupMembersBinding
    private lateinit var memberAdapter: GroupMemberAdapter
    private val firestore = FirebaseFirestore.getInstance()
    
    private var groupId: String = ""
    private var groupName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = resources.getColor(R.color.primary_color, theme)

        groupId = intent.getStringExtra("group_id") ?: ""
        groupName = intent.getStringExtra("group_name") ?: "Grup"

        if (groupId.isEmpty()) {
            Toast.makeText(this, "Grup bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        loadMembers()
    }

    private fun setupToolbar() {
        binding.tvGroupName.text = groupName
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        memberAdapter = GroupMemberAdapter { member ->
            // Üyenin profiline git
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_id", member.userId)
            startActivity(intent)
        }

        binding.recyclerViewMembers.apply {
            layoutManager = LinearLayoutManager(this@GroupMembersActivity)
            adapter = memberAdapter
        }
    }

    private fun loadMembers() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = firestore.collection("groupMembers")
                    .whereEqualTo("groupId", groupId)
                    .get()
                    .await()

                val members = result.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(GroupMember::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.joinedAt }

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    
                    if (members.isEmpty()) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.recyclerViewMembers.visibility = View.GONE
                    } else {
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.recyclerViewMembers.visibility = View.VISIBLE
                        memberAdapter.submitList(members)
                        binding.tvTitle.text = "Grup Üyeleri (${members.size})"
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@GroupMembersActivity, "Üyeler yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


