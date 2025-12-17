package com.parthenios.risemate.ui.call

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.parthenios.risemate.R
import com.parthenios.risemate.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class IncomingCallService : Service() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var callListener: ListenerRegistration? = null
    
    companion object {
        private const val CHANNEL_ID = "incoming_calls"
        private const val NOTIFICATION_ID = 1001
        
        fun start(context: Context) {
            val intent = Intent(context, IncomingCallService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, IncomingCallService::class.java))
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createServiceNotification())
        listenForIncomingCalls()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        callListener?.remove()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Gelen Aramalar",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gelen arama bildirimleri"
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createServiceNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("RiseMate")
        .setContentText("Gelen aramalar dinleniyor")
        .setSmallIcon(R.drawable.ic_notification)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()
    
    private fun listenForIncomingCalls() {
        val currentUserId = auth.currentUser?.uid ?: return
        
        callListener = firestore.collection("calls")
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("status", "ringing")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                for (change in snapshot.documentChanges) {
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val callDoc = change.document
                        val callerId = callDoc.getString("callerId") ?: continue
                        val callType = callDoc.getString("callType") ?: "voice"
                        val callId = callDoc.id
                        
                        // Get caller info and show notification
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val callerDoc = firestore.collection("users")
                                    .document(callerId)
                                    .get()
                                    .await()
                                
                                val caller = callerDoc.toObject(User::class.java)
                                if (caller != null) {
                                    showIncomingCallNotification(caller, callType, callId)
                                }
                            } catch (e: Exception) {
                                // Handle error
                            }
                        }
                    }
                }
            }
    }
    
    private fun showIncomingCallNotification(caller: User, callType: String, callId: String) {
        val intent = Intent(this, CallActivity::class.java).apply {
            putExtra(CallActivity.EXTRA_OTHER_USER, caller)
            putExtra(CallActivity.EXTRA_CALL_TYPE, callType)
            putExtra(CallActivity.EXTRA_IS_INCOMING, true)
            putExtra(CallActivity.EXTRA_CALL_ID, callId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val callTypeText = if (callType == "video") "Görüntülü Arama" else "Sesli Arama"
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("${caller.firstName} ${caller.lastName}")
            .setContentText("$callTypeText geliyor...")
            .setSmallIcon(R.drawable.ic_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(callId.hashCode(), notification)
    }
}

