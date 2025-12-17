package com.parthenios.risemate.ui.chat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.parthenios.risemate.data.Message
import com.parthenios.risemate.data.MessageType
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.FragmentChatBinding
import com.parthenios.risemate.utils.UserPreferences
import com.parthenios.risemate.utils.AesGcm
import com.parthenios.risemate.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.Date

class ChatFragment : Fragment() {
    
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var firestore: FirebaseFirestore
    private lateinit var messageAdapter: MessageAdapter
    private var messageListener: ListenerRegistration? = null
    
    private var currentUser: User? = null
    private var otherUser: User? = null
    private var matchId: String? = null
    private var chatKey: String? = null
    private var lastMessageCount: Int = 0
    private var allMessages: List<Message> = emptyList()
    private var currentSearchQuery: String = ""
    
    // Coroutine job'ları
    private var loadMatchJob: Job? = null
    private var sendMessageJob: Job? = null
    private var sendImageJob: Job? = null
    private var deleteMessageJob: Job? = null
    
    private fun deleteMessageForEveryone(message: Message) {
        deleteMessageJob?.cancel()
        deleteMessageJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("messages").document(message.id).delete().await()
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Mesaj herkesten silindi", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Mesaj silinirken hata: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun deleteMessageForMe(message: Message) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Kullanıcı için mesajı soft delete olarak işaretle
                val userId = currentUser?.uid ?: return@launch
                val deletedForUsers = message.deletedForUsers.toMutableList()

                if (!deletedForUsers.contains(userId)) {
                    deletedForUsers.add(userId)

                    firestore.collection("messages")
                        .document(message.id)
                        .update("deletedForUsers", deletedForUsers)
                        .await()

                    withContext(Dispatchers.Main) {
                        if (isAdded && context != null) {
                            Toast.makeText(requireContext(), "Mesaj sizden silindi", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Mesaj silinirken hata: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun copyMessage(message: Message) {
        // Bu zaten MessageAdapter içinde implement edildi
        Toast.makeText(requireContext(), "Mesaj kopyalandı", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_OTHER_USER = "otherUser"
        private const val ARG_MATCH_ID = "matchId"
        private const val REQUEST_IMAGE_PICK = 1001

        fun newInstance(otherUser: User, matchId: String? = null): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putSerializable(ARG_OTHER_USER, otherUser)
            args.putString(ARG_MATCH_ID, matchId)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            userPreferences = UserPreferences(requireContext())
            firestore = FirebaseFirestore.getInstance()
            currentUser = userPreferences.getUser()
            otherUser = arguments?.getSerializable(ARG_OTHER_USER) as? User
            matchId = arguments?.getString(ARG_MATCH_ID)
            
            if (currentUser == null || otherUser == null) {
                Toast.makeText(requireContext(), "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
                return
            }
            
            setupRecyclerView()
            setupSendButton()
            loadOrCreateMatch()
            
            // Geçici olarak güvenlik kontrolünü devre dışı bırak
            // verifyMatchSecurity()
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Chat yüklenirken hata: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    
    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(
            currentUserId = currentUser!!.uid,
            onMessageDelete = ::deleteMessageForEveryone,
            onMessageDeleteForMe = ::deleteMessageForMe,
            onMessageCopy = ::copyMessage
        )
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
            // Animasyon kaynaklı "yenilenme" hissini azaltmak için değişim animasyonlarını kapat
            itemAnimator = null
        }

        // Yeni mesajlar eklendiğinde otomatik olarak en alta kaydır
        messageAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.recyclerViewMessages.post {
                    val total = messageAdapter.itemCount
                    if (total > 0) {
                        binding.recyclerViewMessages.smoothScrollToPosition(total - 1)
                    }
                }
            }
        })
    }
    
    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString().trim()
            if (messageText.isNotEmpty() && matchId != null) {
                sendMessage(messageText)
                binding.etMessage.text?.clear()
            }
        }

        // Fotoğraf gönderme butonu
        binding.btnAttach.setOnClickListener {
            openImagePicker()
        }
    }
    
    private fun loadOrCreateMatch() {
        if (matchId != null) {
            println("DEBUG: Match ID zaten var: $matchId, chatKey yükleniyor ve mesajlar hazırlanıyor")
            loadMatchJob?.cancel()
            loadMatchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Katılımcıları doğrula; diğer kullanıcı silinmişse eşleşme ve mesajları temizle
                    val participantsOk = validateParticipantsOrCleanup()
                    if (!participantsOk) {
                        withContext(Dispatchers.Main) {
                            if (isAdded && context != null) {
                                Toast.makeText(requireContext(), "Karşı kullanıcı silinmiş. Sohbet kaldırıldı.", Toast.LENGTH_LONG).show()
                            }
                            activity?.onBackPressed()
                        }
                        return@launch
                    }
                    val localMatchId = matchId ?: return@launch
                    val doc = firestore.collection("matches").document(localMatchId).get().await()
                    var existingKey = doc.getString("chatKey")
                    if (existingKey.isNullOrEmpty()) {
                        println("DEBUG: chatKey eksik (mevcut eşleşme), oluşturuluyor...")
                        existingKey = AesGcm.generateKeyB64()
                        firestore.collection("matches").document(localMatchId)
                            .update("chatKey", existingKey)
                            .await()
                        println("DEBUG: chatKey oluşturuldu ve kaydedildi (mevcut eşleşme)")
                    }
                    chatKey = existingKey
                    withContext(Dispatchers.Main) { 
                        if (isAdded && _binding != null) {
                            loadMessages() 
                        }
                    }
                } catch (e: Exception) {
                    println("DEBUG: Mevcut eşleşme için chatKey yükleme hatası: ${e.message}")
                    withContext(Dispatchers.Main) {
                        if (isAdded && context != null) {
                            Toast.makeText(requireContext(), "Sohbet anahtarı yüklenemedi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            return
        }
        
        println("DEBUG: Match ID yok, eşleşme aranıyor...")
        println("DEBUG: Current user: ${currentUser?.uid}")
        println("DEBUG: Other user: ${otherUser?.uid}")
        
        val currUser = currentUser ?: return
        val othUser = otherUser ?: return
        
        loadMatchJob?.cancel()
        loadMatchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Mevcut eşleşmeyi kontrol et
                println("DEBUG: Eşleşme sorgusu başlatılıyor...")
                val matchQuery = firestore.collection("matches")
                    .whereEqualTo("user1Id", currUser.uid)
                    .whereEqualTo("user2Id", othUser.uid)
                    .whereEqualTo("status", "ACTIVE")
                    .get().await()
                
                val reverseMatchQuery = firestore.collection("matches")
                    .whereEqualTo("user1Id", othUser.uid)
                    .whereEqualTo("user2Id", currUser.uid)
                    .whereEqualTo("status", "ACTIVE")
                    .get().await()
                
                val allMatches = matchQuery.documents + reverseMatchQuery.documents
                println("DEBUG: ${allMatches.size} eşleşme bulundu")
                
                if (allMatches.isEmpty()) {
                    println("DEBUG: Eşleşme bulunamadı, yeni eşleşme oluşturuluyor...")
                    // Yeni eşleşme oluştur
                    createNewMatch()
                } else {
                    println("DEBUG: Mevcut eşleşme bulundu: ${allMatches.first().id}")
                    // Mevcut eşleşmeyi kullan
                    val matchDoc = allMatches.first()
                    matchId = matchDoc.id
                    val localMatchId = matchDoc.id
                    // Sohbet anahtarını al/yoksa oluştur
                    var existingKey = matchDoc.getString("chatKey")
                    if (existingKey.isNullOrEmpty()) {
                        println("DEBUG: chatKey eksik, oluşturuluyor...")
                        existingKey = AesGcm.generateKeyB64()
                        firestore.collection("matches").document(localMatchId)
                            .update("chatKey", existingKey)
                            .await()
                        println("DEBUG: chatKey oluşturuldu ve kaydedildi")
                    }
                    chatKey = existingKey
                    withContext(Dispatchers.Main) {
                        if (isAdded && _binding != null) {
                            loadMessages()
                        }
                    }
                }
                
            } catch (e: Exception) {
                println("DEBUG: Eşleşme yükleme hatası: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Eşleşme yüklenirken hata: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun validateParticipantsOrCleanup(): Boolean {
        return try {
            val otherId = otherUser?.uid ?: return false
            val otherDoc = firestore.collection("users").document(otherId).get().await()
            if (otherDoc.exists()) {
                true
            } else {
                // Diğer kullanıcı silinmiş: bu eşleşmeye ait mesajları ve eşleşmeyi temizle
                val localMatchId = matchId
                if (!localMatchId.isNullOrEmpty()) {
                    // Mesajları sil
                    val messages = firestore.collection("messages").whereEqualTo("matchId", localMatchId).get().await()
                    for (msg in messages.documents) {
                        try { firestore.collection("messages").document(msg.id).delete().await() } catch (_: Exception) {}
                    }
                    // Eşleşmeyi sil
                    try { firestore.collection("matches").document(localMatchId).delete().await() } catch (_: Exception) {}
                }
                // Bu eşleşmeye bağlı istek varsa (opsiyonel) durumunu REJECTED yap
                try {
                    val reqs = firestore.collection("matchRequests")
                        .whereEqualTo("matchId", matchId)
                        .get().await()
                    for (req in reqs.documents) {
                        try { firestore.collection("matchRequests").document(req.id).delete().await() } catch (_: Exception) {}
                    }
                } catch (_: Exception) {}
                false
            }
        } catch (e: Exception) {
            println("DEBUG: validateParticipantsOrCleanup hata: ${e.message}")
            true
        }
    }
    
    private suspend fun createNewMatch() {
        try {
            println("DEBUG: Yeni eşleşme oluşturuluyor...")
            val generatedChatKey = AesGcm.generateKeyB64()
            val matchData = hashMapOf(
                "user1Id" to currentUser!!.uid,
                "user2Id" to otherUser!!.uid,
                "user1Name" to "${currentUser!!.firstName} ${currentUser!!.lastName}",
                "user2Name" to "${otherUser!!.firstName} ${otherUser!!.lastName}",
                "status" to "ACTIVE",
                "createdAt" to Date(),
                "expiresAt" to Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000), // 24 saat
                "chatKey" to generatedChatKey
            )
            
            println("DEBUG: Match data: $matchData")
            val docRef = firestore.collection("matches").add(matchData).await()
            matchId = docRef.id
            chatKey = generatedChatKey
            println("DEBUG: Yeni eşleşme oluşturuldu, ID: $matchId")
            
            withContext(Dispatchers.Main) {
                loadMessages()
            }
            
        } catch (e: Exception) {
            println("DEBUG: Eşleşme oluşturma hatası: ${e.message}")
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Eşleşme oluşturulurken hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadMessages() {
        if (matchId == null) {
            println("DEBUG: Match ID null, mesajlar yüklenemiyor")
            Toast.makeText(requireContext(), "Match ID bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }
        
        println("DEBUG: Mesajlar yükleniyor, Match ID: $matchId")
        
        try {
            messageListener = firestore.collection("messages")
                .whereEqualTo("matchId", matchId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("DEBUG: Firestore hatası: ${error.code} - ${error.message}")
                        println("DEBUG: Hata detayı: ${error.localizedMessage}")
                        Toast.makeText(requireContext(), "Mesajlar yüklenirken hata: ${error.message}", Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    
                    if (snapshot == null) {
                        println("DEBUG: Snapshot null")
                        return@addSnapshotListener
                    }
                    
                    println("DEBUG: ${snapshot.documents.size} mesaj dokümanı bulundu")
                    
                    val firebaseMessages = snapshot.documents.mapNotNull { doc ->
                        try {
                            val raw = doc.toObject(Message::class.java)?.copy(id = doc.id)
                            if (raw == null) return@mapNotNull null

                            // Soft delete kontrolü - eğer mesaj bu kullanıcı için silinmişse gösterme
                            val currentUserId = currentUser?.uid
                            if (currentUserId != null && raw.deletedForUsers.contains(currentUserId)) {
                                return@mapNotNull null
                            }

                            val resolved = if (raw.encVersion == 1 && raw.ciphertext != null && raw.nonce != null) {
                                val key = chatKey
                                if (!key.isNullOrEmpty()) {
                                    try {
                                        // Önce AAD olmadan dene, başarısızsa matchId ile dene (geri uyumluluk)
                                        val decrypted = try {
                                            AesGcm.decryptFromB64(key, raw.ciphertext, raw.nonce, null)
                                        } catch (e: Exception) {
                                            AesGcm.decryptFromB64(key, raw.ciphertext, raw.nonce, raw.matchId)
                                        }
                                        // IMAGE mesajları için hem content hem imageUrl'yi çözülmüş data ile doldur
                                        if (raw.messageType == MessageType.IMAGE) {
                                            raw.copy(content = decrypted, imageUrl = decrypted)
                                        } else {
                                            raw.copy(content = decrypted)
                                        }
                                    } catch (e: Exception) {
                                        println("DEBUG: Mesaj çözme hatası: ${e.message}")
                                        raw.copy(content = "[Şifreli mesaj]")
                                    }
                                } else {
                                    raw.copy(content = "[Şifreli mesaj]")
                                }
                            } else {
                                raw
                            }
                            println("DEBUG: Mesaj yüklendi: ${resolved.content}")
                            resolved
                        } catch (e: Exception) {
                            println("DEBUG: Mesaj parse hatası: ${e.message}")
                            println("DEBUG: Hatalı doküman: ${doc.data}")
                            null
                        }
                    }
                    // İstemci tarafında zamana göre sırala (null timestamp en sona)
                    .sortedWith(compareBy<Message> { it.timestamp == null }.thenBy { it.timestamp })
                    
                    // Geçici mesajları koruma: snapshot geldiğinde sadece sunucu mesajlarını göster
                    allMessages = firebaseMessages

                    println("DEBUG: ${firebaseMessages.size} Firebase mesajı, ${allMessages.size} toplam mesaj")
                    val filtered = applyMessageFilterInternal(currentSearchQuery)
                    val shouldScroll = filtered.size > lastMessageCount
                    messageAdapter.submitList(filtered) {
                        binding.recyclerViewMessages.post {
                            if (shouldScroll && filtered.isNotEmpty()) {
                                binding.recyclerViewMessages.smoothScrollToPosition(filtered.size - 1)
                            }
                        }
                        lastMessageCount = filtered.size
                    }

                    // Yeni bir mesaj geldiyse bildirim göster (karşı taraftan ve app açıkken arka plandayken anlamında basit kontrol)
                    if (shouldScroll) {
                        val last = allMessages.last()
                        if (last.senderId != currentUser?.uid && !last.content.isNullOrEmpty()) {
                            NotificationHelper.showMessageNotification(
                                requireContext(),
                                title = "Yeni mesaj",
                                body = last.content
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            println("DEBUG: Listener oluşturma hatası: ${e.message}")
            e.printStackTrace()
            Toast.makeText(requireContext(), "Mesaj listener oluşturulurken hata: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun filterMessages(query: String) {
        currentSearchQuery = query
        val filtered = applyMessageFilterInternal(query)
        messageAdapter.submitList(filtered)
        if (filtered.isNotEmpty()) {
            binding.recyclerViewMessages.scrollToPosition(filtered.size - 1)
        }
    }

    private fun applyMessageFilterInternal(query: String): List<Message> {
        return if (query.isBlank()) {
            allMessages
        } else {
            // Sadece TEXT mesajlarını ara, IMAGE mesajlarını hariç tut
            allMessages.filter {
                it.messageType == MessageType.TEXT && it.content.contains(query, ignoreCase = true)
            }
        }
    }
    
    private fun sendMessage(content: String) {
        val localMatchId = matchId
        val currUser = currentUser
        val othUser = otherUser
        
        if (localMatchId == null) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Match ID bulunamadı", Toast.LENGTH_SHORT).show()
            }
            return
        }
        
        if (currUser == null || othUser == null) return
        
        if (content.trim().isEmpty()) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Mesaj boş olamaz", Toast.LENGTH_SHORT).show()
            }
            return
        }
        
        // Mesajı anlık olarak UI'ye ekle (optimistic update)
        val tempMessage = Message(
            id = "temp_${System.currentTimeMillis()}",
            matchId = localMatchId,
            senderId = currUser.uid,
            receiverId = othUser.uid,
            content = content.trim(),
            messageType = MessageType.TEXT,
            timestamp = Date(),
            isRead = false
        )
        
        // Geçici mesajı listeye ekle
        if (_binding != null) {
            val currentMessages = messageAdapter.currentList.toMutableList()
            currentMessages.add(tempMessage)
            messageAdapter.submitList(currentMessages) {
                binding.recyclerViewMessages.post {
                    if (_binding != null) {
                        binding.recyclerViewMessages.smoothScrollToPosition(currentMessages.size - 1)
                    }
                }
            }
            
            // Input'u temizle
            binding.etMessage.setText("")
        }
        
        sendMessageJob?.cancel()
        sendMessageJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // chatKey hazır değilse Firestore'dan al/oluştur
                val key = ensureChatKey()
                val (cipherB64, ivB64) = if (!key.isNullOrEmpty()) {
                    AesGcm.encryptToB64(key, content.trim(), null)
                } else {
                    null to null
                }

                val message = if (cipherB64 != null && ivB64 != null) {
                    Message(
                        matchId = localMatchId,
                        senderId = currUser.uid,
                        receiverId = othUser.uid,
                        content = "",
                        messageType = MessageType.TEXT,
                        timestamp = Date(),
                        isRead = false,
                        encVersion = 1,
                        ciphertext = cipherB64,
                        nonce = ivB64
                    )
                } else {
                    Message(
                        matchId = localMatchId,
                        senderId = currUser.uid,
                        receiverId = othUser.uid,
                        content = content.trim(),
                        messageType = MessageType.TEXT,
                        timestamp = Date(),
                        isRead = false
                    )
                }
                
                println("DEBUG: Mesaj gönderiliyor: ${message.content}")
                val docRef = firestore.collection("messages").add(message).await()
                println("DEBUG: Mesaj gönderildi, ID: ${docRef.id}")
                
                // Eşleşmenin son mesaj zamanını güncelle
                firestore.collection("matches").document(localMatchId)
                    .update("lastMessageAt", Date()).await()
                
                println("DEBUG: Match güncellendi")
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    
                    // Hata durumunda geçici mesajı kaldır
                    val updatedMessages = messageAdapter.currentList.filter { it.id != tempMessage.id }
                    messageAdapter.submitList(updatedMessages)
                    
                    if (context != null) {
                        Toast.makeText(requireContext(), "Mesaj gönderilirken hata: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun ensureChatKey(): String? {
        if (!chatKey.isNullOrEmpty()) return chatKey
        val localMatchId = matchId ?: return null
        return try {
            val result = firestore.runTransaction { tx ->
                val ref = firestore.collection("matches").document(localMatchId)
                val snapshot = tx.get(ref)
                var key = snapshot.getString("chatKey")
                if (key.isNullOrEmpty()) {
                    key = AesGcm.generateKeyB64()
                    tx.update(ref, mapOf("chatKey" to key))
                }
                key
            }.await()
            chatKey = result
            if (result.isNullOrEmpty()) {
                println("DEBUG: ensureChatKey -> chatKey boş döndü")
            } else {
                println("DEBUG: ensureChatKey -> chatKey hazır")
            }
            result
        } catch (e: Exception) {
            println("DEBUG: ensureChatKey hatası: ${e.message}")
            null
        }
    }
    
    private fun verifyMatchSecurity() {
        val currUser = currentUser ?: return
        val othUser = otherUser ?: return
        
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Kullanıcılar arasında aktif eşleşme var mı kontrol et
                val matchQuery = firestore.collection("matches")
                    .whereEqualTo("user1Id", currUser.uid)
                    .whereEqualTo("user2Id", othUser.uid)
                    .whereEqualTo("status", "ACTIVE")
                    .get().await()
                
                val reverseMatchQuery = firestore.collection("matches")
                    .whereEqualTo("user1Id", othUser.uid)
                    .whereEqualTo("user2Id", currUser.uid)
                    .whereEqualTo("status", "ACTIVE")
                    .get().await()
                
                val allMatches = matchQuery.documents + reverseMatchQuery.documents
                
                if (allMatches.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        if (isAdded && context != null) {
                            Toast.makeText(requireContext(), "Bu kullanıcı ile aktif eşleşmeniz bulunmuyor", Toast.LENGTH_LONG).show()
                        }
                        activity?.finish()
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Güvenlik kontrolü hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun isAuthorizedToChat(): Boolean {
        // Basit güvenlik kontrolü - gerçek uygulamada daha kapsamlı olmalı
        return currentUser != null && otherUser != null && matchId != null
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, ChatFragment.REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ChatFragment.REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            if (selectedImageUri != null && matchId != null) {
                sendImageMessage(selectedImageUri)
            }
        }
    }

    private fun sendImageMessage(imageUri: Uri) {
        val localMatchId = matchId
        val currUser = currentUser
        val othUser = otherUser
        
        if (localMatchId == null) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Match ID bulunamadı", Toast.LENGTH_SHORT).show()
            }
            return
        }
        
        if (currUser == null || othUser == null) return

        // Geçici mesaj oluştur ve UI'ye ekle
        val tempMessage = Message(
            id = "temp_image_${System.currentTimeMillis()}",
            matchId = localMatchId,
            senderId = currUser.uid,
            receiverId = othUser.uid,
            content = "📷 Fotoğraf gönderiliyor...",
            messageType = MessageType.IMAGE,
            timestamp = Date(),
            isRead = false
        )

        if (_binding != null) {
            val currentMessages = messageAdapter.currentList.toMutableList()
            currentMessages.add(tempMessage)
            messageAdapter.submitList(currentMessages) {
                binding.recyclerViewMessages.post {
                    if (_binding != null) {
                        binding.recyclerViewMessages.smoothScrollToPosition(currentMessages.size - 1)
                    }
                }
            }
        }

        sendImageJob?.cancel()
        sendImageJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Context'i güvenli şekilde al
                val ctx = context ?: run {
                    withContext(Dispatchers.Main) {
                        removeTempMessage(tempMessage.id)
                    }
                    return@launch
                }
                
                // Resmi sıkıştır ve Base64'e çevir
                val compressedBytes = compressImage(imageUri)
                if (compressedBytes == null) {
                    withContext(Dispatchers.Main) {
                        if (isAdded && context != null) {
                            Toast.makeText(requireContext(), "Resim sıkıştırılırken hata oluştu", Toast.LENGTH_SHORT).show()
                        }
                        removeTempMessage(tempMessage.id)
                    }
                    return@launch
                }

                val base64Image = Base64.encodeToString(compressedBytes, Base64.DEFAULT)
                val mimeType = ctx.contentResolver.getType(imageUri) ?: "image/jpeg"
                val dataUrl = "data:$mimeType;base64,$base64Image"

                // chatKey hazır değilse Firestore'dan al/oluştur
                val key = ensureChatKey()

                // Şifrelenmiş mesaj oluştur (image mesajları için özel yaklaşım)
                val message = if (!key.isNullOrEmpty()) {
                    // Hem content hem imageUrl için aynı dataUrl'yi şifrele
                    val (cipherB64, ivB64) = AesGcm.encryptToB64(key, dataUrl, null)
                    Message(
                        matchId = localMatchId,
                        senderId = currUser.uid,
                        receiverId = othUser.uid,
                        content = "",  // Şifrelenmiş veri ciphertext'te
                        messageType = MessageType.IMAGE,
                        timestamp = Date(),
                        isRead = false,
                        encVersion = 1,
                        ciphertext = cipherB64,
                        nonce = ivB64,
                        imageUrl = dataUrl
                    )
                } else {
                    // Şifreleme anahtarı yoksa şifrelenmemiş gönder
                    Message(
                        matchId = localMatchId,
                        senderId = currUser.uid,
                        receiverId = othUser.uid,
                        content = dataUrl,
                        messageType = MessageType.IMAGE,
                        timestamp = Date(),
                        isRead = false,
                        imageUrl = dataUrl
                    )
                }

                println("DEBUG: Resim mesajı gönderiliyor")
                val docRef = firestore.collection("messages").add(message).await()
                println("DEBUG: Resim mesajı gönderildi, ID: ${docRef.id}")

                // Eşleşmenin son mesaj zamanını güncelle
                firestore.collection("matches").document(localMatchId)
                    .update("lastMessageAt", Date()).await()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Resim gönderilirken hata: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    removeTempMessage(tempMessage.id)
                    e.printStackTrace()
                }
            }
        }
    }

    private fun removeTempMessage(tempMessageId: String) {
        val updatedMessages = messageAdapter.currentList.filter { it.id != tempMessageId }
        messageAdapter.submitList(updatedMessages)
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            // CreatePostActivity'deki gibi daha güvenilir sıkıştırma
            val bitmap = android.provider.MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)

            // Maksimum boyut 1024x1024
            val maxDimension = 1024
            var width = bitmap.width
            var height = bitmap.height

            if (width > height) {
                if (width > maxDimension) {
                    height = (height * maxDimension.toFloat() / width).toInt()
                    width = maxDimension
                }
            } else {
                if (height > maxDimension) {
                    width = (width * maxDimension.toFloat() / height).toInt()
                    height = maxDimension
                }
            }

            // Yeniden boyutlandır
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            bitmap.recycle()

            // JPEG'e çevir ve compress et
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            scaledBitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Tüm listener ve job'ları temizle
        messageListener?.remove()
        messageListener = null
        loadMatchJob?.cancel()
        sendMessageJob?.cancel()
        sendImageJob?.cancel()
        deleteMessageJob?.cancel()
        _binding = null
    }

}
