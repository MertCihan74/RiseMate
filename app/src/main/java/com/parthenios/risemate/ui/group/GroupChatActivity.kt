package com.parthenios.risemate.ui.group

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.parthenios.risemate.R
import com.parthenios.risemate.data.GroupMessage
import com.parthenios.risemate.data.SkillGroup
import com.parthenios.risemate.databinding.ActivityGroupChatBinding
import com.parthenios.risemate.ui.chat.ImageViewerActivity
import com.parthenios.risemate.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class GroupChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupChatBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var messageAdapter: GroupMessageAdapter
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var group: SkillGroup? = null
    private var messageListener: ListenerRegistration? = null
    private var isMember = false
    
    private val COOLDOWN_DURATION = 10000L
    private var canSendMessage = true
    private var cooldownTimer: CountDownTimer? = null
    
    companion object {
        const val EXTRA_GROUP = "extra_group"
        private const val REQUEST_IMAGE_PICK = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        window.statusBarColor = resources.getColor(R.color.primary_color, theme)

        @Suppress("DEPRECATION")
        group = intent.getSerializableExtra(EXTRA_GROUP) as? SkillGroup
        
        if (group == null) {
            Toast.makeText(this, "Grup bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupInputArea()
        
        // Önce üyelik kontrolü yap
        checkMembershipAndLoadMessages()
    }

    private fun setupToolbar() {
        binding.tvGroupName.text = group?.name ?: "Grup"
        binding.tvMemberCount.text = "${group?.memberCount ?: 0} üye"
        
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        binding.tvGroupName.setOnClickListener { showGroupMembers() }
        binding.tvMemberCount.setOnClickListener { showGroupMembers() }
        binding.ivGroupIcon.setOnClickListener { showGroupMembers() }
    }

    private fun showGroupMembers() {
        val groupId = group?.id ?: return
        val groupName = group?.name ?: "Grup"
        
        val intent = Intent(this, GroupMembersActivity::class.java)
        intent.putExtra("group_id", groupId)
        intent.putExtra("group_name", groupName)
        startActivity(intent)
    }

    private fun setupRecyclerView() {
        val currentUserId = auth.currentUser?.uid ?: ""
        
        messageAdapter = GroupMessageAdapter(currentUserId) { imageUrl ->
            val intent = Intent(this, ImageViewerActivity::class.java)
            intent.putExtra("image_url", imageUrl)
            startActivity(intent)
        }

        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@GroupChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupInputArea() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty() && canSendMessage) {
                sendMessage(message)
            } else if (!canSendMessage) {
                Toast.makeText(this, "Lütfen bekleyin...", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAttachImage.setOnClickListener {
            if (canSendMessage) {
                pickImage()
            } else {
                Toast.makeText(this, "Lütfen bekleyin...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkMembershipAndLoadMessages() {
        val currentUserId = auth.currentUser?.uid ?: return
        val groupId = group?.id ?: return

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val memberDoc = firestore.collection("groupMembers")
                    .whereEqualTo("groupId", groupId)
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .await()

                isMember = !memberDoc.isEmpty

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    
                    if (isMember) {
                        // Üye - mesajları yükle ve input göster
                        binding.layoutInput.visibility = View.VISIBLE
                        loadMessages()
                    } else {
                        // Üye değil - mesajları gösterme
                        binding.layoutInput.visibility = View.GONE
                        binding.recyclerViewMessages.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        
                        // Empty state mesajlarını güncelle
                        binding.ivEmptyIcon.setImageResource(R.drawable.ic_group)
                        binding.tvEmptyTitle.text = "Gruba Katılın"
                        binding.tvEmptyMessage.text = "Bu grubun mesajlarını görmek için\nönce gruba katılmalısınız."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@GroupChatActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadMessages() {
        val groupId = group?.id ?: return

        messageListener = firestore.collection("groupMessages")
            .whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("GroupChat", "Listener error: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot == null) return@addSnapshotListener

                val messages = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        val timestamp = doc.getTimestamp("timestamp")?.toDate()
                        val localTimestamp = (data["localTimestamp"] as? Long)?.let { java.util.Date(it) }
                        
                        GroupMessage(
                            id = doc.id,
                            groupId = data["groupId"] as? String ?: "",
                            senderId = data["senderId"] as? String ?: "",
                            senderName = data["senderName"] as? String ?: "",
                            senderPhotoUrl = data["senderPhotoUrl"] as? String ?: "",
                            message = data["message"] as? String ?: "",
                            imageUrl = data["imageUrl"] as? String ?: "",
                            messageType = data["messageType"] as? String ?: "text",
                            timestamp = timestamp ?: localTimestamp ?: java.util.Date(),
                            isDeleted = data["isDeleted"] as? Boolean ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.timestamp }

                runOnUiThread {
                    binding.recyclerViewMessages.visibility = View.VISIBLE
                    messageAdapter.submitList(messages.toList()) {
                        if (messages.isNotEmpty()) {
                            binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
                            binding.layoutEmptyState.visibility = View.GONE
                        } else {
                            binding.layoutEmptyState.visibility = View.VISIBLE
                        }
                    }
                }
            }
    }

    private fun sendMessage(text: String, imageBase64: String = "") {
        val currentUser = auth.currentUser ?: return
        val user = userPreferences.getUser() ?: return
        val groupId = group?.id ?: return

        // Hemen textbox'ı temizle
        val messageText = text
        binding.etMessage.text?.clear()
        binding.etMessage.clearFocus()

        val messageType = if (imageBase64.isNotEmpty()) "image" else "text"

        val messageData = hashMapOf(
            "groupId" to groupId,
            "senderId" to currentUser.uid,
            "senderName" to "${user.firstName} ${user.lastName}",
            "senderPhotoUrl" to user.photoUrl,
            "message" to messageText,
            "imageUrl" to imageBase64,
            "messageType" to messageType,
            "timestamp" to FieldValue.serverTimestamp(),
            "localTimestamp" to System.currentTimeMillis(),
            "isDeleted" to false
        )

        // Cooldown'u hemen başlat
        startCooldown()
        
        firestore.collection("groupMessages")
            .add(messageData)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Mesaj gönderilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startCooldown() {
        canSendMessage = false
        binding.btnSend.isEnabled = false
        binding.btnAttachImage.isEnabled = false
        binding.cardCooldown.visibility = View.VISIBLE

        cooldownTimer?.cancel()
        cooldownTimer = object : CountDownTimer(COOLDOWN_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                binding.tvCooldown.text = "Sonraki mesaj için $seconds saniye bekleyin"
            }

            override fun onFinish() {
                canSendMessage = true
                binding.btnSend.isEnabled = true
                binding.btnAttachImage.isEnabled = true
                binding.cardCooldown.visibility = View.GONE
            }
        }.start()
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data?.data != null) {
            processAndSendImage(data.data!!)
        }
    }

    private fun processAndSendImage(uri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSend.isEnabled = false
        binding.btnAttachImage.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                
                val maxSize = 800
                val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
                val newWidth = (bitmap.width * ratio).toInt()
                val newHeight = (bitmap.height * ratio).toInt()
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                
                val baos = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                val imageBytes = baos.toByteArray()
                val base64Image = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    sendMessage("", base64Image)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSend.isEnabled = canSendMessage
                    binding.btnAttachImage.isEnabled = canSendMessage
                    Toast.makeText(this@GroupChatActivity, "Fotoğraf işlenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messageListener?.remove()
        cooldownTimer?.cancel()
    }
}
