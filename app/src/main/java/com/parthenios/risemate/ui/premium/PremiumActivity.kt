package com.parthenios.risemate.ui.premium

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.databinding.ActivityPremiumBinding
import com.parthenios.risemate.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PremiumActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var binding: ActivityPremiumBinding
    private lateinit var billingClient: BillingClient
    private lateinit var userPreferences: UserPreferences
    private val firestore = FirebaseFirestore.getInstance()
    
    private var selectedPlan = "yearly" // Default: yearly
    
    // Google Play Console'da tanımlanacak ürün ID'leri
    companion object {
        const val PRODUCT_MONTHLY = "risemate_premium_monthly"
        const val PRODUCT_YEARLY = "risemate_premium_yearly"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        
        window.statusBarColor = resources.getColor(R.color.primary_color, theme)

        setupUI()
        setupBillingClient()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        // Premium özellikleri ayarla
        setupFeatures()

        // Paket seçimi
        binding.cardMonthly.setOnClickListener { selectPlan("monthly") }
        binding.cardYearly.setOnClickListener { selectPlan("yearly") }
        binding.rbMonthly.setOnClickListener { selectPlan("monthly") }
        binding.rbYearly.setOnClickListener { selectPlan("yearly") }

        // Varsayılan olarak yıllık seçili
        selectPlan("yearly")

        // Satın alma butonu
        binding.btnSubscribe.setOnClickListener {
            startPurchase()
        }
    }

    private fun setupFeatures() {
        val features = listOf(
            "Yetenek gruplarına sınırsız erişim",
            "Grup sohbetlerinde mesajlaşma",
            "Özel grup rozetleri",
            "Reklamsız deneyim"
        )

        val featureViews = listOf(
            binding.feature1,
            binding.feature2,
            binding.feature3,
            binding.feature4
        )

        features.forEachIndexed { index, text ->
            if (index < featureViews.size) {
                featureViews[index].root.findViewById<TextView>(R.id.tvFeatureText).text = text
            }
        }
    }

    private fun selectPlan(plan: String) {
        selectedPlan = plan

        // UI güncelle
        binding.rbMonthly.isChecked = plan == "monthly"
        binding.rbYearly.isChecked = plan == "yearly"

        binding.cardMonthly.strokeColor = if (plan == "monthly") 
            getColor(R.color.primary_color) else getColor(android.R.color.transparent)
        binding.cardYearly.strokeColor = if (plan == "yearly") 
            getColor(R.color.primary_color) else getColor(android.R.color.transparent)

        // Buton metnini güncelle
        val priceText = if (plan == "monthly") "₺49,99/ay" else "₺499,99/yıl"
        binding.btnSubscribe.text = "Premium'a Geç - $priceText"
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Billing client hazır
                    queryProducts()
                    checkExistingPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Yeniden bağlanmayı dene
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Fiyatları güncelle
                productDetailsList.forEach { productDetails ->
                    val price = productDetails.subscriptionOfferDetails?.firstOrNull()
                        ?.pricingPhases?.pricingPhaseList?.firstOrNull()
                        ?.formattedPrice ?: return@forEach

                    runOnUiThread {
                        when (productDetails.productId) {
                            PRODUCT_MONTHLY -> binding.tvMonthlyPrice.text = "$price/ay"
                            PRODUCT_YEARLY -> binding.tvYearlyPrice.text = "$price/yıl"
                        }
                    }
                }
            }
        }
    }

    private fun checkExistingPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchasesList) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        // Aktif abonelik var
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }

    private fun startPurchase() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubscribe.isEnabled = false

        val productId = if (selectedPlan == "monthly") PRODUCT_MONTHLY else PRODUCT_YEARLY

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            binding.progressBar.visibility = View.GONE
            binding.btnSubscribe.isEnabled = true

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken

                if (offerToken != null) {
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    billingClient.launchBillingFlow(this, billingFlowParams)
                }
            } else {
                Toast.makeText(this, "Ürün bilgisi alınamadı", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Toast.makeText(this, "Satın alma iptal edildi", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Satın alma hatası: ${billingResult.debugMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Satın alma onayı (acknowledge)
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // Firebase'e kaydet
                        savePremiumToFirebase(purchase)
                    }
                }
            } else {
                // Zaten onaylı, Firebase'i güncelle
                savePremiumToFirebase(purchase)
            }
        }
    }

    private fun savePremiumToFirebase(purchase: Purchase) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        
        val premiumType = if (purchase.products.contains(PRODUCT_MONTHLY)) "monthly" else "yearly"
        val now = System.currentTimeMillis()
        val endDate = if (premiumType == "monthly") {
            now + (30L * 24 * 60 * 60 * 1000) // 30 gün
        } else {
            now + (365L * 24 * 60 * 60 * 1000) // 1 yıl
        }

        val premiumData = mapOf(
            "isPremium" to true,
            "premiumType" to premiumType,
            "premiumStartDate" to now,
            "premiumEndDate" to endDate,
            "purchaseToken" to purchase.purchaseToken
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("users").document(currentUser.uid)
                    .update(premiumData)

                // Local user'ı da güncelle
                val user = userPreferences.getUser()
                if (user != null) {
                    val updatedUser = user.copy(
                        isPremium = true,
                        premiumType = premiumType,
                        premiumStartDate = now,
                        premiumEndDate = endDate,
                        purchaseToken = purchase.purchaseToken
                    )
                    userPreferences.saveUser(updatedUser)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PremiumActivity, "Premium aktif edildi! 🎉", Toast.LENGTH_LONG).show()
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PremiumActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }
}


