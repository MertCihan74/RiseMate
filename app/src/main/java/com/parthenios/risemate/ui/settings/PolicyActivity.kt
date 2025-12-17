package com.parthenios.risemate.ui.settings

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.parthenios.risemate.databinding.ActivityPolicyBinding

class PolicyActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPolicyBinding
    
    companion object {
        const val EXTRA_POLICY_TYPE = "policy_type"
        const val TYPE_PRIVACY = "privacy"
        const val TYPE_TERMS = "terms"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val policyType = intent.getStringExtra(EXTRA_POLICY_TYPE) ?: TYPE_PRIVACY
        
        setupToolbar(policyType)
        loadPolicy(policyType)
    }
    
    private fun setupToolbar(policyType: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = when (policyType) {
                TYPE_PRIVACY -> "Gizlilik Politikası"
                TYPE_TERMS -> "Kullanım Koşulları"
                else -> "Politika"
            }
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun loadPolicy(policyType: String) {
        binding.webView.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            
            val fileName = when (policyType) {
                TYPE_PRIVACY -> "privacy_policy.html"
                TYPE_TERMS -> "terms_of_service.html"
                else -> "privacy_policy.html"
            }
            
            loadUrl("file:///android_asset/$fileName")
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}








