package com.parthenios.risemate.ui.chat

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.parthenios.risemate.R
import com.parthenios.risemate.data.Match
import com.parthenios.risemate.databinding.ItemChatBinding
import android.os.Handler
import android.os.Looper
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(
    private val onChatClick: (Match) -> Unit,
    private val currentUserId: String
) : ListAdapter<Match, ChatListAdapter.ChatViewHolder>(ChatDiffCallback()) {
    
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            // Her saniye kalan süreleri güncelle
            notifyDataSetChanged()
            handler.postDelayed(this, 1000) // 1 saniye
        }
    }
    
    init {
        handler.post(updateRunnable)
    }
    
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        handler.removeCallbacks(updateRunnable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(
        private val binding: ItemChatBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private var onlineStatusListener: ListenerRegistration? = null

        fun bind(match: Match) {
            // Önceki listener'ı temizle
            onlineStatusListener?.remove()
            onlineStatusListener = null
            binding.apply {
                // Kullanıcı adını ayarla
                val otherUserId = if (match.user1Id == currentUserId) {
                    match.user2Id
                } else {
                    match.user1Id
                }
                
                tvUserName.text = if (match.user1Id == currentUserId) {
                    match.user2Name
                } else {
                    match.user1Name
                }
                
                // Profil fotoğrafını yükle
                loadProfilePhoto(otherUserId)
                
                // Son mesaj zamanını gizle (sadece çevrimiçi durumu gösterilecek)
                tvLastMessageTime.visibility = View.GONE
                
                // Eşleşme durumunu göster
                tvMatchStatus.text = "Aktif Eşleşme"
                tvMatchStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_green_dark))
                
                // Çevrimiçi durumunu yükle
                loadOnlineStatus(otherUserId)
                
                // Kalan süreyi hesapla ve göster
                val remainingTime = calculateRemainingTime(match)
                if (remainingTime.isNotEmpty()) {
                    tvRemainingTime.text = remainingTime
                    cardRemainingTime.visibility = View.VISIBLE
                } else {
                    cardRemainingTime.visibility = View.GONE
                }
                
                // Chat'e tıklama
                root.setOnClickListener {
                    onChatClick(match)
                }
            }
        }
        
        private fun loadProfilePhoto(userId: String) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { userDoc ->
                    val context = binding.root.context
                    // Activity destroy edilmişse işlem yapma
                    if (context is Activity && (context.isFinishing || context.isDestroyed)) {
                        return@addOnSuccessListener
                    }
                    
                    val photoUrl = userDoc.getString("photoUrl") ?: ""
                    
                    if (photoUrl.isNotEmpty()) {
                        try {
                            Glide.with(context)
                                .load(photoUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .into(binding.ivProfilePhoto)
                        } catch (e: Exception) {
                            binding.ivProfilePhoto.setImageResource(R.drawable.ic_profile)
                        }
                    } else {
                        binding.ivProfilePhoto.setImageResource(R.drawable.ic_profile)
                    }
                }
                .addOnFailureListener {
                    binding.ivProfilePhoto.setImageResource(R.drawable.ic_profile)
                }
        }
        
        private fun loadOnlineStatus(userId: String) {
            // Önceki listener'ı temizle
            onlineStatusListener?.remove()
            
            // Firestore listener ile gerçek zamanlı güncellemeleri dinle
            onlineStatusListener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, _ ->
                    val context = binding.root.context
                    // Activity destroy edilmişse işlem yapma
                    if (context is Activity && (context.isFinishing || context.isDestroyed)) {
                        onlineStatusListener?.remove()
                        onlineStatusListener = null
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null && snapshot.exists()) {
                        updateOnlineStatusUI(snapshot.getBoolean("isOnline") ?: false, snapshot.getLong("lastSeen") ?: 0L)
                    }
                }
        }
        
        private fun updateOnlineStatusUI(isOnline: Boolean, lastSeen: Long) {
            val now = System.currentTimeMillis()
            val timeSinceLastSeen = now - lastSeen
            
            // Çevrimiçi olması için hem isOnline=true hem de lastSeen 5 dakikadan yeni olmalı
            val isActuallyOnline = isOnline && lastSeen > 0 && timeSinceLastSeen < (5 * 60 * 1000) // 5 dakika
            
            // Online indicator'ı güncelle
            binding.viewOnlineIndicator.visibility = if (isActuallyOnline) View.VISIBLE else View.GONE
            
            binding.tvOnlineStatus.text = if (isActuallyOnline) {
                "Çevrimiçi"
            } else {
                if (lastSeen > 0) {
                    val diff = now - lastSeen
                    val minutesAgo = (diff / (60 * 1000)).toInt()
                    
                    when {
                        minutesAgo < 1 -> "Şimdi"
                        minutesAgo < 60 -> "$minutesAgo dk önce"
                        minutesAgo < 1440 -> "${minutesAgo / 60} saat önce"
                        else -> "${minutesAgo / 1440} gün önce"
                    }
                } else {
                    ""
                }
            }
            
            // Online ise yeşil, değilse gri renk
            binding.tvOnlineStatus.setTextColor(
                if (isActuallyOnline) 
                    binding.root.context.getColor(R.color.success_color)
                else 
                    binding.root.context.getColor(android.R.color.darker_gray)
            )
        }
        
        private fun formatTimestamp(timestamp: Date?): String {
            return if (timestamp != null) {
                val now = Date()
                val diff = now.time - timestamp.time
                
                when {
                    diff < 60 * 1000 -> "Az önce"
                    diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} dk önce"
                    diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} saat önce"
                    else -> {
                        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                        sdf.format(timestamp)
                    }
                }
            } else {
                "Henüz mesaj yok"
            }
        }
        
        private fun calculateRemainingTime(match: Match): String {
            // Eğer mesaj atılmışsa (lastMessageAt varsa), sayaç tamamen kalkacak
            val lastMessageTime = match.lastMessageAt
            if (lastMessageTime != null) {
                // Mesaj atılmış, sayaç gösterilmez
                return ""
            }
            
            // Eğer hiç mesaj yoksa, createdAt'den itibaren say
            val createdAt = match.createdAt
            if (createdAt != null) {
                val now = Date()
                val expiresAt = Date(createdAt.time + 24 * 60 * 60 * 1000)
                val diff = expiresAt.time - now.time
                
                if (diff <= 0) {
                    return "Süre doldu"
                } else {
                    val hours = diff / (60 * 60 * 1000)
                    val minutes = (diff % (60 * 60 * 1000)) / (60 * 1000)
                    val seconds = (diff % (60 * 1000)) / 1000
                    
                    return when {
                        hours > 0 -> "⏰ ${hours}s ${minutes}dk"
                        minutes > 0 -> "⏰ ${minutes}dk ${seconds}sn"
                        else -> "⏰ ${seconds}sn"
                    }
                }
            } else {
                return ""
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(oldItem: Match, newItem: Match): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Match, newItem: Match): Boolean {
            return oldItem == newItem
        }
    }
}
