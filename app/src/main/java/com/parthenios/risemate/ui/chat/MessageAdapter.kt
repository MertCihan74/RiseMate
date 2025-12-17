package com.parthenios.risemate.ui.chat

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.util.Base64
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.data.Message
import com.parthenios.risemate.data.MessageType
import com.parthenios.risemate.databinding.ItemMessageReceivedBinding
import com.parthenios.risemate.databinding.ItemMessageSentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val currentUserId: String,
    private val otherUserId: String? = null,
    private val currentUserPhotoUrl: String? = null,
    private val onMessageDelete: ((Message) -> Unit)? = null,
    private val onMessageDeleteForMe: ((Message) -> Unit)? = null,
    private val onMessageCopy: ((Message) -> Unit)? = null
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private var adapterContext: Context? = null

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        adapterContext = parent.context
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SentMessageViewHolder(binding)
            }
            VIEW_TYPE_RECEIVED -> {
                val binding = ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    inner class SentMessageViewHolder(
        private val binding: ItemMessageSentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            val context = binding.root.context
            val isActivityValid = context !is Activity || (!context.isFinishing && !context.isDestroyed)
            
            binding.apply {
                // Fotoğraf mesajı kontrolü
                if (message.messageType == MessageType.IMAGE && !message.imageUrl.isNullOrEmpty()) {
                    ivMessageImage.visibility = View.VISIBLE
                    tvMessage.visibility = View.GONE
                    if (isActivityValid) {
                        try {
                            Glide.with(context)
                                .load(message.imageUrl)
                                .override(800, 1200)
                                .fitCenter()
                                .placeholder(R.drawable.ic_profile)
                                .into(ivMessageImage)
                        } catch (e: Exception) {
                            ivMessageImage.setImageResource(R.drawable.ic_profile)
                        }
                    }
                    
                    // Fotoğrafa tıklayınca tam ekranda aç (hata güvenli)
                    ivMessageImage.setOnClickListener {
                        openImageSafe(message.imageUrl)
                    }
                } else {
                    ivMessageImage.visibility = View.GONE
                    tvMessage.visibility = View.VISIBLE
                    tvMessage.text = message.content
                    ivMessageImage.setOnClickListener(null)
                }
                
                tvTimestamp.text = formatTimestamp(message.timestamp)
                
                // Profil fotoğrafını yükle
                if (currentUserPhotoUrl != null && currentUserPhotoUrl.isNotEmpty() && isActivityValid) {
                    try {
                        Glide.with(context)
                            .load(currentUserPhotoUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .into(ivProfilePhoto)
                    } catch (e: Exception) {
                        ivProfilePhoto.setImageResource(R.drawable.ic_profile)
                    }
                } else {
                    loadProfilePhoto(currentUserId, ivProfilePhoto)
                }
                
                // Uzun basma ile mesaj işlemleri
                root.setOnLongClickListener {
                    showMessageOptions(binding.root.context, message)
                    true
                }
            }
        }
    }

    inner class ReceivedMessageViewHolder(
        private val binding: ItemMessageReceivedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            val context = binding.root.context
            val isActivityValid = context !is Activity || (!context.isFinishing && !context.isDestroyed)
            
            binding.apply {
                // Fotoğraf mesajı kontrolü
                if (message.messageType == MessageType.IMAGE && !message.imageUrl.isNullOrEmpty()) {
                    ivMessageImage.visibility = View.VISIBLE
                    tvMessage.visibility = View.GONE
                    if (isActivityValid) {
                        try {
                            Glide.with(context)
                                .load(message.imageUrl)
                                .override(800, 1200)
                                .fitCenter()
                                .placeholder(R.drawable.ic_profile)
                                .into(ivMessageImage)
                        } catch (e: Exception) {
                            ivMessageImage.setImageResource(R.drawable.ic_profile)
                        }
                    }
                    
                    // Fotoğrafa tıklayınca tam ekranda aç (hata güvenli)
                    ivMessageImage.setOnClickListener {
                        openImageSafe(message.imageUrl)
                    }
                } else {
                    ivMessageImage.visibility = View.GONE
                    tvMessage.visibility = View.VISIBLE
                    tvMessage.text = message.content
                    ivMessageImage.setOnClickListener(null)
                }
                
                tvTimestamp.text = formatTimestamp(message.timestamp)
                
                // Profil fotoğrafını yükle
                val senderId = message.senderId
                if (senderId != null && senderId != currentUserId) {
                    loadProfilePhoto(senderId, ivProfilePhoto)
                } else {
                    ivProfilePhoto.setImageResource(R.drawable.ic_profile)
                }
                
                // Uzun basma ile mesaj işlemleri
                root.setOnLongClickListener {
                    showMessageOptions(binding.root.context, message)
                    true
                }
            }
        }
    }
    
    /**
     * Tam ekran görüntülemeyi hata güvenli başlatır; olası TransactionTooLarge vb. durumlarda
     * kullanıcıya kısa uyarı gösterir ve çökme engellenir.
     */
    private fun openImageSafe(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) return
        try {
            val context = adapterContext ?: return
            
            val intent = android.content.Intent(context, ImageViewerActivity::class.java)
            if (imageUrl.startsWith("data:image")) {
                // Büyük Base64 verisini Binder'a yüklememek için cache'e yaz
                val file = writeDataUrlToCache(context, imageUrl)
                if (file != null) {
                    intent.putExtra("imageFilePath", file.absolutePath)
                } else {
                    Toast.makeText(context, "Görsel açılamadı", Toast.LENGTH_SHORT).show()
                    return
                }
            } else {
                intent.putExtra("imageUrl", imageUrl)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            adapterContext?.let {
                Toast.makeText(it, "Görsel açılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun writeDataUrlToCache(context: Context, dataUrl: String): File? {
        return try {
            val base64Part = dataUrl.substringAfter(",", missingDelimiterValue = "")
            if (base64Part.isEmpty()) return null
            val bytes = Base64.decode(base64Part, Base64.DEFAULT)
            val file = File(context.cacheDir, "chat_img_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { it.write(bytes) }
            file
        } catch (_: Exception) {
            null
        }
    }
    
    private fun showMessageOptions(context: Context, message: Message) {
        val options = mutableListOf<String>()

        // Metin mesajları için kopyala seçeneği
        if (message.messageType == MessageType.TEXT) {
            options.add("Kopyala")
        }

        // Gönderen kullanıcı için silme seçenekleri
        if (message.senderId == currentUserId) {
            options.add("Benden Sil")
            options.add("Herkesten Sil")
        }

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Kopyala" -> {
                        copyMessageToClipboard(context, message)
                        onMessageCopy?.invoke(message)
                    }
                    "Benden Sil" -> {
                        // Sadece kullanıcı için mesajı sil (soft delete)
                        onMessageDeleteForMe?.invoke(message)
                    }
                    "Herkesten Sil" -> {
                        // Tüm kullanıcılar için mesajı sil
                        onMessageDelete?.invoke(message)
                    }
                }
            }
            .show()
    }
    
    private fun copyMessageToClipboard(context: Context, message: Message) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Mesaj", message.content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Mesaj kopyalandı", Toast.LENGTH_SHORT).show()
    }
    
    private fun loadProfilePhoto(userId: String, imageView: android.widget.ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                val photoUrl = userDoc.getString("photoUrl") ?: ""
                
                withContext(Dispatchers.Main) {
                    if (photoUrl.isNotEmpty()) {
                        Glide.with(imageView.context)
                            .load(photoUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .into(imageView)
                    } else {
                        imageView.setImageResource(R.drawable.ic_profile)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(R.drawable.ic_profile)
                }
            }
        }
    }

    private fun formatTimestamp(timestamp: Date?): String {
        return if (timestamp != null) {
            val now = Date()
            val diff = now.time - timestamp.time
            val daysDiff = diff / (24 * 60 * 60 * 1000)
            
            when {
                diff < 60 * 1000 -> "Şimdi" // 1 dakikadan az
                diff < 60 * 60 * 1000 -> {
                    val minutes = (diff / (60 * 1000)).toInt()
                    "$minutes dk önce"
                }
                daysDiff == 0L -> {
                    // Bugün - sadece saat göster
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    sdf.format(timestamp)
                }
                daysDiff == 1L -> {
                    // Dün
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    "Dün ${sdf.format(timestamp)}"
                }
                daysDiff < 7 -> {
                    // Bu hafta içinde
                    val sdf = SimpleDateFormat("EEE HH:mm", Locale.getDefault())
                    sdf.format(timestamp)
                }
                else -> {
                    // Daha eski
                    val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                    sdf.format(timestamp)
                }
            }
        } else {
            ""
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
    
    // Geçici mesajları filtrele (gerçek mesajlar yüklendiğinde)
    fun filterTempMessages(): List<Message> {
        return currentList.filter { !it.id.startsWith("temp_") }
    }
}
