package com.parthenios.risemate.ui.social

import android.graphics.Bitmap
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
import com.parthenios.risemate.databinding.ActivityPostImageViewerBinding

class PostImageViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostImageViewerBinding

    companion object {
        const val EXTRA_IMAGE_URL = "imageUrl"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tam ekran modu
        hideSystemUI()

        binding = ActivityPostImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)

        if (!imageUrl.isNullOrEmpty()) {
            // Base64 image ise decode et ve yükle
            if (imageUrl.startsWith("data:image/")) {
                try {
                    // Base64 data URL'sinden gerçek Base64 string'i çıkar
                    val base64String = imageUrl.substringAfter(",")
                    val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    if (bitmap != null) {
                        binding.photoView.setImageBitmap(bitmap)
                    } else {
                        finish() // decode edilemediyse kapat
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    finish() // hata olursa kapat
                }
            } else {
                // Normal URL ise Glide ile yükle
                Glide.with(this)
                    .load(imageUrl)
                    .into(binding.photoView)
            }
        } else {
            finish() // veri yoksa ekranı kapat
        }

        // Geri dönmek için tıklama
        binding.photoView.setOnClickListener {
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


