package com.parthenios.risemate.ui.matching

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.data.MatchRequest
import com.parthenios.risemate.data.MatchRequestStatus
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.ItemMatchRequestBinding
import com.parthenios.risemate.ui.profile.ProfileActivity
import android.os.CountDownTimer

class MatchRequestAdapter(
    private val onAcceptClick: (MatchRequest) -> Unit,
    private val onRejectClick: (MatchRequest) -> Unit,
    private val isSentRequests: Boolean = false // Gönderilen istekler mi?
) : ListAdapter<MatchRequest, MatchRequestAdapter.MatchRequestViewHolder>(MatchRequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchRequestViewHolder {
        val binding = ItemMatchRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MatchRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchRequestViewHolder, position: Int) {
        holder.bind(getItem(position), isSentRequests)
    }

    inner class MatchRequestViewHolder(
        private val binding: ItemMatchRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(request: MatchRequest, isSentRequests: Boolean) {
            binding.apply {
                // Kullanıcı adını ayarla
                val otherUserId = if (isSentRequests) {
                    request.toUserId // Gönderilen isteklerde karşı tarafın ID'si
                } else {
                    request.fromUserId // Alınan isteklerde gönderenin ID'si
                }
                
                tvUserName.text = if (isSentRequests) {
                    request.toUserName // Gönderilen isteklerde karşı tarafın adı
                } else {
                    request.fromUserName // Alınan isteklerde gönderenin adı
                }
                
                // Profil fotoğrafını yükle
                loadProfilePhoto(otherUserId)
                
                tvRequestStatus.text = when (request.status) {
                    MatchRequestStatus.PENDING -> "Beklemede"
                    MatchRequestStatus.ACCEPTED -> "Kabul Edildi"
                    MatchRequestStatus.REJECTED -> "Reddedildi"
                    MatchRequestStatus.MATCHED -> "Eşleşme Tamamlandı"
                }
                
                // Gönderilen isteklerde butonları gizle, alınan isteklerde duruma göre göster
                if (isSentRequests) {
                    // Gönderilen isteklerde butonları her zaman gizle
                    btnAccept.visibility = android.view.View.GONE
                    btnReject.visibility = android.view.View.GONE
                } else {
                    // Alınan isteklerde duruma göre butonları göster/gizle
                    when (request.status) {
                        MatchRequestStatus.PENDING -> {
                            btnAccept.visibility = android.view.View.VISIBLE
                            btnReject.visibility = android.view.View.VISIBLE
                        }
                        else -> {
                            btnAccept.visibility = android.view.View.GONE
                            btnReject.visibility = android.view.View.GONE
                        }
                    }
                }
                
                // Durum rengini ayarla
                when (request.status) {
                    MatchRequestStatus.PENDING -> {
                        tvRequestStatus.setTextColor(android.graphics.Color.parseColor("#FF9800")) // Turuncu
                    }
                    MatchRequestStatus.ACCEPTED -> {
                        tvRequestStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Yeşil
                    }
                    MatchRequestStatus.REJECTED -> {
                        tvRequestStatus.setTextColor(android.graphics.Color.parseColor("#F44336")) // Kırmızı
                    }
                    MatchRequestStatus.MATCHED -> {
                        tvRequestStatus.setTextColor(android.graphics.Color.parseColor("#2196F3")) // Mavi
                    }
                }
                
                // Geri sayım şimdilik devre dışı (test amaçlı)
                binding.tvCountdown.text = ""
                
                // Buton click listener'ları (sadece alınan isteklerde)
                if (!isSentRequests) {
                    btnAccept.setOnClickListener {
                        onAcceptClick(request)
                    }
                    
                    btnReject.setOnClickListener {
                        onRejectClick(request)
                    }
                }
                
                // Profil görüntüleme - profil fotoğrafına ve kullanıcı adına tıklama
                val profileClickListener = View.OnClickListener {
                    openUserProfile(otherUserId)
                }
                
                cardProfilePhoto.setOnClickListener(profileClickListener)
                tvUserName.setOnClickListener(profileClickListener)
            }
        }
        
        private fun openUserProfile(userId: String) {
            val context = itemView.context
            // Activity destroy edilmişse işlem yapma
            if (context is Activity && (context.isFinishing || context.isDestroyed)) {
                return
            }
            
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { userDoc ->
                    // Tekrar kontrol et
                    if (context is Activity && (context.isFinishing || context.isDestroyed)) {
                        return@addOnSuccessListener
                    }
                    
                    val user = userDoc.toObject(User::class.java)
                    if (user != null) {
                        val intent = Intent(context, ProfileActivity::class.java)
                        intent.putExtra("user", user)
                        context.startActivity(intent)
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
    }

    class MatchRequestDiffCallback : DiffUtil.ItemCallback<MatchRequest>() {
        override fun areItemsTheSame(oldItem: MatchRequest, newItem: MatchRequest): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MatchRequest, newItem: MatchRequest): Boolean {
            return oldItem == newItem
        }
    }
}
