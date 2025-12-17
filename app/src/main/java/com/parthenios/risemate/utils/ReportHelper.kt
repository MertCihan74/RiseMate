package com.parthenios.risemate.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object ReportHelper {
    
    private val firestore = FirebaseFirestore.getInstance()
    
    // Report reasons
    private val REPORT_REASONS = listOf(
        "Uygunsuz içerik",
        "Spam veya sahte profil",
        "Taciz veya zorbalık",
        "Nefret söylemi",
        "Dolandırıcılık",
        "Diğer"
    )
    
    /**
     * Show report bottom sheet dialog
     */
    fun showReportBottomSheet(
        context: Context,
        reporterId: String,
        reporterName: String,
        reportedUser: User,
        source: String,
        onBlockRequested: (() -> Unit)? = null
    ) {
        val bottomSheet = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_report, null)
        bottomSheet.setContentView(view)
        
        // Setup radio buttons for reasons
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupReasons)
        radioGroup.removeAllViews()
        
        REPORT_REASONS.forEachIndexed { index, reason ->
            val radioButton = RadioButton(context).apply {
                id = index
                text = reason
                setPadding(16, 24, 16, 24)
            }
            radioGroup.addView(radioButton)
        }
        
        // Select first option by default
        if (radioGroup.childCount > 0) {
            (radioGroup.getChildAt(0) as? RadioButton)?.isChecked = true
        }
        
        val etDetails = view.findViewById<TextInputEditText>(R.id.etReportDetails)
        val btnSubmit = view.findViewById<MaterialButton>(R.id.btnSubmitReport)
        val btnBlockUser = view.findViewById<MaterialButton>(R.id.btnBlockUser)
        
        btnSubmit.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(context, "Lütfen bir sebep seçin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val reason = REPORT_REASONS.getOrNull(selectedId) ?: "Diğer"
            val details = etDetails.text?.toString() ?: ""
            
            submitReport(
                context = context,
                reporterId = reporterId,
                reporterName = reporterName,
                reportedUserId = reportedUser.uid,
                reportedUserName = "${reportedUser.firstName} ${reportedUser.lastName}",
                reason = reason,
                details = details,
                source = source
            )
            
            bottomSheet.dismiss()
        }
        
        btnBlockUser?.setOnClickListener {
            bottomSheet.dismiss()
            onBlockRequested?.invoke()
        }
        
        bottomSheet.show()
    }
    
    /**
     * Submit report to Firestore
     */
    private fun submitReport(
        context: Context,
        reporterId: String,
        reporterName: String,
        reportedUserId: String,
        reportedUserName: String,
        reason: String,
        details: String,
        source: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val report = hashMapOf(
                    "reporterId" to reporterId,
                    "reporterName" to reporterName,
                    "reportedUserId" to reportedUserId,
                    "reportedUserName" to reportedUserName,
                    "reason" to reason,
                    "details" to details,
                    "source" to source,
                    "status" to "pending",
                    "timestamp" to System.currentTimeMillis()
                )
                
                firestore.collection("reports")
                    .add(report)
                    .await()
                
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Rapor gönderildi. İncelememiz yapılacak.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Rapor gönderilirken hata: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

