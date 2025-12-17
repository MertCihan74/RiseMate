package com.parthenios.risemate.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.parthenios.risemate.data.Match
import com.parthenios.risemate.data.MatchStatus
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.FragmentChatListBinding
import com.parthenios.risemate.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.util.Date

class ChatListFragment : Fragment() {
    
    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var firestore: FirebaseFirestore
    private lateinit var chatListAdapter: ChatListAdapter
    private var matchListener: ListenerRegistration? = null
    
    private var currentUser: User? = null
    private val activeMatches = mutableListOf<Match>()
    private var validationJob: Job? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userPreferences = UserPreferences(requireContext())
        firestore = FirebaseFirestore.getInstance()
        currentUser = userPreferences.getUser()
        
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }
        
        setupRecyclerView()
        setupSwipeRefresh()
        loadActiveMatches()
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            // Sayfayı yenile
            loadActiveMatches()
        }
    }
    
    private fun setupRecyclerView() {
        chatListAdapter = ChatListAdapter(
            onChatClick = { match ->
                // Chat'e git
                openChat(match)
            },
            currentUserId = currentUser!!.uid
        )
        
        binding.recyclerViewChats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatListAdapter
        }
    }
    
    private fun loadActiveMatches() {
        val user = currentUser ?: return
        val userId = user.uid
        
        // Önceki listener'ı temizle
        matchListener?.remove()
        
        // Her iki yönlü eşleşmeleri kontrol et
        val query1 = firestore.collection("matches")
            .whereEqualTo("user1Id", userId)
            .whereEqualTo("status", MatchStatus.ACTIVE.name)
            
        val query2 = firestore.collection("matches")
            .whereEqualTo("user2Id", userId)
            .whereEqualTo("status", MatchStatus.ACTIVE.name)
        
        // İlk query'yi dinle
        matchListener = query1.addSnapshotListener { snapshot1, error1 ->
            if (!isAdded || _binding == null) return@addSnapshotListener
            
            if (error1 != null) {
                // Permission denied hatası çıkış yapıldığında oluşabilir, kullanıcıya gösterme
                if (error1.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
                    return@addSnapshotListener
                }
                val ctx = context ?: return@addSnapshotListener
                Toast.makeText(ctx, "Eşleşmeler yüklenirken hata: ${error1.message}", Toast.LENGTH_SHORT).show()
                binding.swipeRefresh.isRefreshing = false
                return@addSnapshotListener
            }
            
            // İkinci query'yi de çalıştır
            query2.get().addOnSuccessListener { snapshot2 ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                val matches1 = snapshot1?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Match::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                val matches2 = snapshot2?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Match::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                val allMatches = (matches1 + matches2).distinctBy { it.id }

                // Karşı kullanıcı silinmişse veya engellenmişse bu eşleşmeleri temizle ve listeden çıkar
                validationJob?.cancel()
                validationJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val blockedUserIds = user.blockedUsers.toSet()
                    val validMatches = mutableListOf<Match>()
                    val now = System.currentTimeMillis()
                    for (m in allMatches) {
                        val otherId = if (m.user1Id == userId) m.user2Id else m.user1Id
                        
                        // Engellenen kullanıcıları filtrele
                        if (otherId in blockedUserIds) {
                            continue
                        }
                        
                        // Süre kontrolü: Sadece hiç mesaj atılmamışsa ve createdAt'den 24 saat geçmişse eşleşmeyi sil
                        val lastMessageTime = m.lastMessageAt?.time
                        val createdAt = m.createdAt?.time
                        
                        // Eğer mesaj atılmışsa, süre kontrolü yapma (eşleşme kalıcı olur)
                        if (lastMessageTime == null && createdAt != null) {
                            val timeSinceCreation = now - createdAt
                            val twentyFourHours = 24 * 60 * 60 * 1000L
                            
                            if (timeSinceCreation >= twentyFourHours) {
                                // 24 saat geçmiş ve hiç mesaj atılmamış, eşleşmeyi sil
                                try {
                                    val msgs = firestore.collection("messages").whereEqualTo("matchId", m.id).get().await()
                                    for (d in msgs.documents) {
                                        try { firestore.collection("messages").document(d.id).delete().await() } catch (_: Exception) {}
                                    }
                                    try { firestore.collection("matches").document(m.id).delete().await() } catch (_: Exception) {}
                                } catch (_: Exception) {}
                                continue // Bu eşleşmeyi listeye ekleme
                            }
                        }
                        
                        val otherDoc = try { firestore.collection("users").document(otherId).get().await() } catch (e: Exception) { null }
                        if (otherDoc != null && otherDoc.exists()) {
                            validMatches.add(m)
                        } else {
                            // Cleanup: bu eşleşmeye bağlı mesajları ve eşleşmeyi sil
                            try {
                                val msgs = firestore.collection("messages").whereEqualTo("matchId", m.id).get().await()
                                for (d in msgs.documents) {
                                    try { firestore.collection("messages").document(d.id).delete().await() } catch (_: Exception) {}
                                }
                                try { firestore.collection("matches").document(m.id).delete().await() } catch (_: Exception) {}
                            } catch (_: Exception) {}
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (!isAdded || _binding == null) return@withContext
                        activeMatches.clear()
                        activeMatches.addAll(validMatches)
                        chatListAdapter.submitList(validMatches.toList()) {
                            // Adapter güncellendi, çevrimiçi durumları yenilenecek
                        }
                        updateUI()
                        // SwipeRefresh'i kapat
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }.addOnFailureListener { error2 ->
                if (!isAdded || _binding == null) return@addOnFailureListener
                val ctx = context ?: return@addOnFailureListener
                Toast.makeText(ctx, "Eşleşmeler yüklenirken hata: ${error2.message}", Toast.LENGTH_SHORT).show()
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
    
    private fun updateUI() {
        if (activeMatches.isEmpty()) {
            binding.layoutNoChats.visibility = View.VISIBLE
            binding.recyclerViewChats.visibility = View.GONE
        } else {
            binding.layoutNoChats.visibility = View.GONE
            binding.recyclerViewChats.visibility = View.VISIBLE
        }
    }
    
    private fun openChat(match: Match) {
        if (!isAdded) return
        val context = context ?: return
        
        // Diğer kullanıcının bilgilerini al
        val otherUserId = if (match.user1Id == currentUser!!.uid) {
            match.user2Id
        } else {
            match.user1Id
        }
        
        val otherUserName = if (match.user1Id == currentUser!!.uid) {
            match.user2Name
        } else {
            match.user1Name
        }
        
        // Geçici User objesi oluştur (sadece chat için)
        val otherUser = User(
            uid = otherUserId,
            firstName = otherUserName.split(" ").firstOrNull() ?: "",
            lastName = otherUserName.split(" ").drop(1).joinToString(" ") ?: "",
            email = "",
            age = 0,
            city = "",
            knownSkills = emptyList(),
            wantedSkills = emptyList()
        )
        
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra("otherUser", otherUser)
        intent.putExtra("matchId", match.id)
        startActivity(intent)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Listener ve job'ları temizle
        matchListener?.remove()
        matchListener = null
        validationJob?.cancel()
        validationJob = null
        _binding = null
    }
}
