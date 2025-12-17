package com.parthenios.risemate.ui.matching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.parthenios.risemate.R
import com.parthenios.risemate.data.MatchRequest
import com.parthenios.risemate.data.MatchRequestStatus
import com.parthenios.risemate.data.Match
import com.parthenios.risemate.data.MatchStatus
import com.parthenios.risemate.data.User
import com.parthenios.risemate.ui.chat.ChatActivity
import com.parthenios.risemate.databinding.FragmentMatchingBinding
import com.parthenios.risemate.utils.UserPreferences
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.parthenios.risemate.utils.NotificationHelper
import kotlinx.coroutines.tasks.await
import java.util.Date

class MatchingFragment : Fragment() {
    private var _binding: FragmentMatchingBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private lateinit var firestore: FirebaseFirestore
    private var currentUser: User? = null
    private val receivedRequests = mutableListOf<MatchRequest>()
    private lateinit var receivedRequestsAdapter: MatchRequestAdapter
    private var loadRequestsJob: Job? = null
    private var acceptRequestJob: Job? = null
    private var rejectRequestJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userPreferences = UserPreferences(requireContext())
        firestore = FirebaseFirestore.getInstance()
        currentUser = userPreferences.getUser()
        setupUI()
        loadMatchRequests()
    }

    private fun setupUI() {
        binding.apply {
            tvTitle.text = "Eşleşme İstekleri"
            tvSubtitle.visibility = View.GONE
            
            // Sadece alınan istekler için RecyclerView ayarla
            setupReceivedRequestsRecyclerView()
        }
    }
    
    private fun setupReceivedRequestsRecyclerView() {
        // Sadece alınan istekler - butonlar aktif
        receivedRequestsAdapter = MatchRequestAdapter(
            onAcceptClick = { request -> 
                // Kabul etme işlemi
                acceptMatchRequest(request)
            },
            onRejectClick = { request -> 
                // Reddetme işlemi
                rejectMatchRequest(request)
            },
            isSentRequests = false // Alınan istekler
        )
        
        binding.recyclerViewReceivedRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = receivedRequestsAdapter
        }
    }

    private fun loadMatchRequests() {
        if (currentUser == null) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
            }
            return
        }
        
        loadRequestsJob?.cancel()
        loadRequestsJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = currentUser?.uid ?: return@launch
                
                // Sadece alınan istekleri yükle - sadece beklemede olanlar
                val receivedQuery = firestore.collection("matchRequests")
                    .whereEqualTo("toUserId", userId)
                    .whereEqualTo("status", MatchRequestStatus.PENDING.name)
                    .get().await()
                
                val receivedRequestsList = receivedQuery.documents.mapNotNull { document ->
                    try {
                        document.toObject(MatchRequest::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                
                // Engellenen kullanıcıları filtrele
                val blockedUserIds = currentUser?.blockedUsers?.toSet() ?: emptySet()
                val filtered = receivedRequestsList.filter { request ->
                    request.fromUserId !in blockedUserIds
                }
                
                // Kabul edilen eşleşme sayısını al
                val acceptedMatches1 = firestore.collection("matches")
                    .whereEqualTo("user1Id", userId)
                    .whereEqualTo("status", MatchStatus.ACTIVE.name)
                    .get().await()
                val acceptedMatches2 = firestore.collection("matches")
                    .whereEqualTo("user2Id", userId)
                    .whereEqualTo("status", MatchStatus.ACTIVE.name)
                    .get().await()
                val totalAccepted = acceptedMatches1.size() + acceptedMatches2.size()
                
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    
                    receivedRequests.clear()
                    receivedRequests.addAll(filtered)
                    receivedRequestsAdapter.submitList(receivedRequests.toList())
                    
                    // Kabul sayısını güncelle
                    binding.tvAcceptedCount.text = totalAccepted.toString()
                    
                    // UI güncelleme
                    updateUI()

                    // Basit bildirim: yeni istek varsa haber ver
                    if (filtered.isNotEmpty() && context != null) {
                        NotificationHelper.showMatchRequestNotification(
                            requireContext(),
                            title = "Yeni eşleşme isteği",
                            body = "Bir kullanıcı sizden eşleşme istiyor"
                        )
                    }
                }

                // Süreye bağlı temizlik devre dışı bırakıldı
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Eşleşme istekleri yüklenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun updateUI() {
        binding.apply {
            val pendingCount = receivedRequests.size
            
            // Stats güncelle
            tvPendingCount.text = pendingCount.toString()
            
            if (receivedRequests.isEmpty()) {
                layoutNoMatches.visibility = View.VISIBLE
                tvSectionTitle.visibility = View.GONE
                recyclerViewReceivedRequests.visibility = View.GONE
            } else {
                layoutNoMatches.visibility = View.GONE
                tvSectionTitle.visibility = View.VISIBLE
                recyclerViewReceivedRequests.visibility = View.VISIBLE
            }
        }
    }
    
    private fun acceptMatchRequest(request: MatchRequest) {
        println("DEBUG: Accepting request ${request.id}")
        // Anlık olarak UI'den kaldır
        removeRequestFromUI(request)
        
        acceptRequestJob?.cancel()
        acceptRequestJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // İsteği kabul et
                firestore.collection("matchRequests").document(request.id)
                    .update("status", MatchRequestStatus.ACCEPTED.name)
                    .await()
                
                // Eşleşme oluştur
                val createdMatchId = createMatch(request)
                
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        // Yönlendirme kaldırıldı: sadece bilgilendir
                        Toast.makeText(requireContext(), "Tebrikler, eşleştiniz! Mesajlar bölümünden sohbeti başlatabilirsiniz.", Toast.LENGTH_LONG).show()
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    // Hata durumunda eski duruma geri döndür
                    receivedRequests.add(request)
                    receivedRequestsAdapter.submitList(receivedRequests.toList())
                    updateUI()
                    if (context != null) {
                        Toast.makeText(requireContext(), "İstek kabul edilirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun rejectMatchRequest(request: MatchRequest) {
        // Anlık olarak UI'den kaldır
        removeRequestFromUI(request)
        
        rejectRequestJob?.cancel()
        rejectRequestJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // İsteği reddet
                firestore.collection("matchRequests").document(request.id)
                    .update("status", MatchRequestStatus.REJECTED.name)
                    .await()
                
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Eşleşme isteği reddedildi", Toast.LENGTH_SHORT).show()
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    // Hata durumunda eski duruma geri döndür
                    receivedRequests.add(request)
                    receivedRequestsAdapter.submitList(receivedRequests.toList())
                    if (context != null) {
                        Toast.makeText(requireContext(), "İstek reddedilirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun updateRequestStatusInUI(request: MatchRequest, newStatus: MatchRequestStatus) {
        // Kabul edilen istekler için UI'yi güncelle (chat'e yönlendirilecek)
        val updatedRequest = request.copy(status = newStatus)
        val index = receivedRequests.indexOfFirst { it.id == request.id }
        
        if (index != -1) {
            // Listeyi güncelle
            receivedRequests[index] = updatedRequest
            
            // Adapter'ı güncelle - notifyItemChanged kullan
            receivedRequestsAdapter.notifyItemChanged(index)
            
            // Debug için log
            println("DEBUG: Request ${request.id} status updated to ${newStatus.name}")
            println("DEBUG: Item at index $index updated")
            println("DEBUG: Updated request status: ${updatedRequest.status}")
        } else {
            println("DEBUG: Request ${request.id} not found in received requests")
            println("DEBUG: Available request IDs: ${receivedRequests.map { it.id }}")
        }
    }
    
    private fun removeRequestFromUI(request: MatchRequest) {
        val index = receivedRequests.indexOfFirst { it.id == request.id }
        
        if (index != -1) {
            // Listeden kaldır
            receivedRequests.removeAt(index)
            
            // Adapter'ı güncelle - notifyItemRemoved kullan
            receivedRequestsAdapter.notifyItemRemoved(index)
            
            // UI'yi güncelle
            updateUI()
            
            println("DEBUG: Request ${request.id} removed from UI")
        } else {
            println("DEBUG: Request ${request.id} not found in received requests")
        }
    }
    
    private suspend fun createMatch(request: MatchRequest): String {
        try {
            // Diğer kullanıcının bilgilerini al
            val otherUserDoc = firestore.collection("users")
                .document(request.fromUserId)
                .get().await()
            
            val otherUser = otherUserDoc.toObject(User::class.java)
            if (otherUser == null) {
                // Gönderen kullanıcı silinmiş ise isteği düşür ve işlem yapma
                try {
                    firestore.collection("matchRequests").document(request.id)
                        .update("status", MatchRequestStatus.REJECTED.name)
                        .await()
                } catch (_: Exception) {}
                throw Exception("Diğer kullanıcı bulunamadı (silinmiş olabilir)")
            }

            // Mevcut aktif eşleşme var mı? Varsa yeniden oluşturma, mevcut ID'yi dön
            val existing1 = firestore.collection("matches")
                .whereEqualTo("user1Id", currentUser!!.uid)
                .whereEqualTo("user2Id", request.fromUserId)
                .whereEqualTo("status", MatchStatus.ACTIVE.name)
                .get().await()
            val existing2 = firestore.collection("matches")
                .whereEqualTo("user1Id", request.fromUserId)
                .whereEqualTo("user2Id", currentUser!!.uid)
                .whereEqualTo("status", MatchStatus.ACTIVE.name)
                .get().await()
            val existingMatch = (existing1.documents + existing2.documents).firstOrNull()
            if (existingMatch != null) {
                // Request'i mevcut match ile ilişkilendir
                try {
                    firestore.collection("matchRequests").document(request.id)
                        .update("matchId", existingMatch.id, "status", MatchRequestStatus.ACCEPTED.name)
                        .await()
                } catch (_: Exception) {}
                return existingMatch.id
            }
            
            // Eşleşme oluştur
            val now = java.util.Date()
            val expiresAt = java.util.Date(now.time + 24 * 60 * 60 * 1000) // 24 saat sonra
            val matchData = hashMapOf(
                "user1Id" to currentUser!!.uid,
                "user2Id" to request.fromUserId,
                "user1Name" to "${currentUser!!.firstName} ${currentUser!!.lastName}",
                "user2Name" to "${otherUser.firstName} ${otherUser.lastName}",
                "status" to MatchStatus.ACTIVE.name,
                "createdAt" to now,
                "lastMessageAt" to null, // Henüz mesaj yok
                "expiresAt" to expiresAt
            )
            
            val matchRef = firestore.collection("matches").add(matchData).await()
            
            // Match ID'yi request'e kaydet (opsiyonel)
            firestore.collection("matchRequests").document(request.id)
                .update("matchId", matchRef.id)
                .await()
            return matchRef.id
        } catch (e: Exception) {
            throw Exception("Eşleşme oluşturulurken hata: ${e.message}")
        }
    }
    
    private fun redirectToChat(request: MatchRequest, matchId: String?) {
        if (!isAdded || context == null) return
        
        try {
            // Diğer kullanıcının bilgilerini al (basit versiyon)
            val otherUser = User(
                uid = request.fromUserId,
                firstName = request.fromUserName.split(" ").firstOrNull() ?: "Kullanıcı",
                lastName = request.fromUserName.split(" ").drop(1).joinToString(" ") ?: "",
                email = "",
                age = 0,
                city = "",
                knownSkills = emptyList(),
                wantedSkills = emptyList()
            )
            
            val intent = android.content.Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("otherUser", otherUser)
            if (!matchId.isNullOrEmpty()) {
                intent.putExtra("matchId", matchId)
            }
            startActivity(intent)
            
        } catch (e: Exception) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Chat'e yönlendirilirken hata: ${e.message}", Toast.LENGTH_LONG).show()
            }
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Coroutine job'ları iptal et
        loadRequestsJob?.cancel()
        acceptRequestJob?.cancel()
        rejectRequestJob?.cancel()
        _binding = null
    }
}
