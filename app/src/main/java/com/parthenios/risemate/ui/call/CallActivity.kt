package com.parthenios.risemate.ui.call

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.parthenios.risemate.R
import com.parthenios.risemate.auth.AuthRepository
import com.parthenios.risemate.data.CallType
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.ActivityCallBinding
import com.parthenios.risemate.utils.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CallActivity : AppCompatActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.updateBaseContextLocale(newBase))
    }
    
    private lateinit var binding: ActivityCallBinding
    private var otherUser: User? = null
    private var callType: String = CallType.VOICE
    private var isIncoming: Boolean = false
    private var callId: String? = null
    
    private val firestore = FirebaseFirestore.getInstance()
    private val authRepository = AuthRepository()
    private var callListener: ListenerRegistration? = null
    
    // Call state
    private var isCallActive = false
    private var isMuted = false
    private var isSpeakerOn = false
    private var callDurationSeconds = 0
    private val handler = Handler(Looper.getMainLooper())
    
    private val durationRunnable = object : Runnable {
        override fun run() {
            if (isCallActive) {
                callDurationSeconds++
                updateDurationText()
                handler.postDelayed(this, 1000)
            }
        }
    }
    
    companion object {
        const val EXTRA_OTHER_USER = "other_user"
        const val EXTRA_CALL_TYPE = "call_type"
        const val EXTRA_IS_INCOMING = "is_incoming"
        const val EXTRA_CALL_ID = "call_id"
        
        private const val PERMISSION_REQUEST_CODE = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get extras
        otherUser = intent.getSerializableExtra(EXTRA_OTHER_USER) as? User
        callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: CallType.VOICE
        isIncoming = intent.getBooleanExtra(EXTRA_IS_INCOMING, false)
        callId = intent.getStringExtra(EXTRA_CALL_ID)
        
        if (otherUser == null) {
            Toast.makeText(this, "Kullanıcı bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupUI()
        checkPermissions()
    }
    
    private fun setupUI() {
        // User info
        binding.tvCallerName.text = "${otherUser?.firstName} ${otherUser?.lastName}"
        
        // Load profile photo
        if (!otherUser?.photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(otherUser?.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(binding.ivCallerPhoto)
        }
        
        // Call type
        if (callType == CallType.VIDEO) {
            binding.tvCallType.text = "Görüntülü Arama"
            binding.ivCallTypeIcon.setImageResource(R.drawable.ic_videocam)
        } else {
            binding.tvCallType.text = "Sesli Arama"
            binding.ivCallTypeIcon.setImageResource(R.drawable.ic_call)
        }
        
        // Call status
        if (isIncoming) {
            binding.tvCallStatus.text = "Gelen Arama..."
            binding.layoutIncomingActions.visibility = View.VISIBLE
            binding.layoutCallActions.visibility = View.GONE
        } else {
            binding.tvCallStatus.text = "Aranıyor..."
            binding.layoutIncomingActions.visibility = View.GONE
            binding.layoutCallActions.visibility = View.VISIBLE
            initiateCall()
        }
        
        // Button listeners
        binding.btnEndCall.setOnClickListener { endCall() }
        binding.btnAcceptCall.setOnClickListener { acceptCall() }
        binding.btnDeclineCall.setOnClickListener { declineCall() }
        binding.btnMute.setOnClickListener { toggleMute() }
        binding.btnSpeaker.setOnClickListener { toggleSpeaker() }
    }
    
    private fun checkPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (callType == CallType.VIDEO) {
            permissions.add(Manifest.permission.CAMERA)
        }
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!allGranted) {
                Toast.makeText(this, "Arama için izinler gerekli", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun initiateCall() {
        val currentUser = authRepository.getCurrentUser() ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val callData = hashMapOf(
                    "callerId" to currentUser.uid,
                    "receiverId" to otherUser!!.uid,
                    "callType" to callType,
                    "status" to "ringing",
                    "startTime" to System.currentTimeMillis()
                )
                
                val docRef = firestore.collection("calls").add(callData).await()
                callId = docRef.id
                
                // Listen for call status changes
                listenForCallStatus()
                
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@CallActivity, "Arama başlatılamadı", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
    
    private fun listenForCallStatus() {
        if (callId == null) return
        
        callListener = firestore.collection("calls")
            .document(callId!!)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                val status = snapshot.getString("status") ?: return@addSnapshotListener
                
                when (status) {
                    "accepted" -> {
                        isCallActive = true
                        binding.tvCallStatus.text = "Bağlandı"
                        binding.layoutIncomingActions.visibility = View.GONE
                        binding.layoutCallActions.visibility = View.VISIBLE
                        handler.post(durationRunnable)
                    }
                    "declined", "ended", "missed" -> {
                        val message = when (status) {
                            "declined" -> "Arama reddedildi"
                            "ended" -> "Arama sonlandırıldı"
                            else -> "Arama cevapsız"
                        }
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
    }
    
    private fun acceptCall() {
        if (callId == null) return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("calls")
                    .document(callId!!)
                    .update("status", "accepted")
                    .await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun declineCall() {
        if (callId == null) {
            finish()
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("calls")
                    .document(callId!!)
                    .update("status", "declined")
                    .await()
            } catch (e: Exception) {
                // Handle error
            }
            
            CoroutineScope(Dispatchers.Main).launch {
                finish()
            }
        }
    }
    
    private fun endCall() {
        if (callId == null) {
            finish()
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("calls")
                    .document(callId!!)
                    .update(
                        mapOf(
                            "status" to "ended",
                            "endTime" to System.currentTimeMillis(),
                            "duration" to callDurationSeconds
                        )
                    )
                    .await()
            } catch (e: Exception) {
                // Handle error
            }
            
            CoroutineScope(Dispatchers.Main).launch {
                finish()
            }
        }
    }
    
    private fun toggleMute() {
        isMuted = !isMuted
        binding.btnMute.setImageResource(
            if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic
        )
        // Actual mute implementation would go here
    }
    
    private fun toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn
        binding.btnSpeaker.setImageResource(
            if (isSpeakerOn) R.drawable.ic_volume_up else R.drawable.ic_volume_off
        )
        
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = isSpeakerOn
    }
    
    private fun updateDurationText() {
        val minutes = callDurationSeconds / 60
        val seconds = callDurationSeconds % 60
        binding.tvCallDuration.text = String.format("%02d:%02d", minutes, seconds)
        binding.tvCallDuration.visibility = View.VISIBLE
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(durationRunnable)
        callListener?.remove()
        
        // End call if still active
        if (isCallActive && callId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firestore.collection("calls")
                        .document(callId!!)
                        .update("status", "ended")
                        .await()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
    
    override fun onBackPressed() {
        // Confirm before ending call
        if (isCallActive) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Aramayı Sonlandır")
                .setMessage("Aramayı sonlandırmak istiyor musunuz?")
                .setPositiveButton("Evet") { _, _ -> endCall() }
                .setNegativeButton("Hayır", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
}

