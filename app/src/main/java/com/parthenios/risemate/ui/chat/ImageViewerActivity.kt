package com.parthenios.risemate.ui.chat

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.parthenios.risemate.databinding.ActivityImageViewerBinding

class ImageViewerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityImageViewerBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        hideSystemUI()
        
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Farklı extra key'leri kontrol et
        val imageUrl = intent.getStringExtra("imageUrl") 
            ?: intent.getStringExtra("image_url")
        val imageFilePath = intent.getStringExtra("imageFilePath")
        
        val imageSource = when {
            !imageFilePath.isNullOrEmpty() -> imageFilePath
            !imageUrl.isNullOrEmpty() -> imageUrl
            else -> null
        }
        
        if (imageSource != null) {
            loadImage(imageSource)
        } else {
            finish()
        }
        
        binding.photoView.setOnClickListener {
            finish()
        }
    }
    
    private fun loadImage(imageSource: String) {
        try {
            if (imageSource.startsWith("data:image")) {
                // Base64 image
                val base64Data = imageSource.substringAfter("base64,")
                val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.photoView.setImageBitmap(bitmap)
            } else {
                // URL or file path
                Glide.with(this)
                    .load(imageSource)
                    .into(binding.photoView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }
    
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = 
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
