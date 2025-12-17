package com.parthenios.risemate.ui.call

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * BroadcastReceiver for handling call decline action from notification
 */
class DeclineCallReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_DECLINE_CALL = "com.parthenios.risemate.ACTION_DECLINE_CALL"
        const val EXTRA_CALL_ID = "call_id"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DECLINE_CALL) {
            val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: return
            
            // Dismiss the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(callId.hashCode())
            
            // Update call status in Firestore
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    FirebaseFirestore.getInstance()
                        .collection("calls")
                        .document(callId)
                        .update("status", "declined")
                        .await()
                } catch (e: Exception) {
                    // Handle error silently
                }
            }
        }
    }
}

