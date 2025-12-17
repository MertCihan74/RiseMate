package com.parthenios.risemate.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.net.Uri
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageCropHelper {
    
    private const val MAX_SIZE = 512 // Maksimum boyut (Firestore limiti için küçük tutuyoruz)
    
    /**
     * Fotoğrafı Base64 string olarak döndürür (WhatsApp benzeri boyutlarda)
     */
    suspend fun imageToBase64(context: Context, imageUri: Uri, maxSize: Int = 1600, quality: Int = 90): String = withContext(Dispatchers.IO) {
        // Önce sample size'ı hesapla
        val sampleSize = calculateInSampleSize(imageUri, context, maxSize * 2)
        
        // EXIF orientation'ı kontrol et
        val orientation = try {
            val exifInputStream = context.contentResolver.openInputStream(imageUri)
            if (exifInputStream != null) {
                val exif = ExifInterface(exifInputStream)
                val orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                exifInputStream.close()
                orient
            } else {
                ExifInterface.ORIENTATION_NORMAL
            }
        } catch (e: Exception) {
            android.util.Log.w("ImageCropHelper", "EXIF okunamadı: ${e.message}")
            ExifInterface.ORIENTATION_NORMAL
        }
        
        // Bitmap'i yükle
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw Exception("Fotoğraf dosyası açılamadı")
        
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = sampleSize
        }
        
        var bitmap = BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()
        
        if (bitmap == null) {
            throw Exception("Fotoğraf yüklenemedi")
        }
        
        // EXIF orientation'a göre bitmap'i döndür
        bitmap = rotateBitmapIfNeeded(bitmap, orientation)
        
        // Orijinal boyutları koru, sadece çok büyükse küçült
        val finalBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val scale = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            bitmap.recycle()
            resized
        } else {
            bitmap
        }
        
        // Base64'e çevir (WhatsApp benzeri kalite)
        val width = finalBitmap.width
        val height = finalBitmap.height
        val outputStream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        outputStream.close()
        
        finalBitmap.recycle()
        
        android.util.Log.d("ImageCropHelper", "Görsel boyutu: ${width}x${height}, Base64 boyutu: ${byteArray.size / 1024}KB")
        
        // Base64 string oluştur
        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        
        // Data URL formatında döndür
        "data:image/jpeg;base64,$base64String"
    }
    
    /**
     * Fotoğrafı yuvarlak şekilde kırpar ve Base64 string olarak döndürür
     */
    suspend fun cropToCircleBase64(context: Context, imageUri: Uri): String = withContext(Dispatchers.IO) {
        // Önce sample size'ı hesapla
        val sampleSize = calculateInSampleSize(imageUri, context, MAX_SIZE * 2) // Biraz daha büyük al ki kalite kaybı olmasın
        
        // EXIF orientation'ı kontrol et
        val orientation = try {
            val exifInputStream = context.contentResolver.openInputStream(imageUri)
            if (exifInputStream != null) {
                val exif = ExifInterface(exifInputStream)
                val orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                exifInputStream.close()
                orient
            } else {
                ExifInterface.ORIENTATION_NORMAL
            }
        } catch (e: Exception) {
            android.util.Log.w("ImageCropHelper", "EXIF okunamadı: ${e.message}")
            ExifInterface.ORIENTATION_NORMAL
        }
        
        // Bitmap'i yükle
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw Exception("Fotoğraf dosyası açılamadı")
        
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = sampleSize
        }
        
        var originalBitmap = BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()
        
        if (originalBitmap == null) {
            throw Exception("Fotoğraf yüklenemedi")
        }
        
        // EXIF orientation'a göre bitmap'i döndür
        originalBitmap = rotateBitmapIfNeeded(originalBitmap, orientation)
        
        android.util.Log.d("ImageCropHelper", "Orijinal bitmap boyutu: ${originalBitmap.width}x${originalBitmap.height}")
        
        // Kare boyut belirle (en küçük kenar, ama maksimum MAX_SIZE)
        val originalSize = minOf(originalBitmap.width, originalBitmap.height)
        val size = minOf(originalSize, MAX_SIZE)
        
        android.util.Log.d("ImageCropHelper", "Kırpılacak boyut: $size x $size")
        
        // Merkezden kare kırp
        val x = (originalBitmap.width - originalSize) / 2
        val y = (originalBitmap.height - originalSize) / 2
        
        android.util.Log.d("ImageCropHelper", "Kırpma koordinatları: x=$x, y=$y, size=$originalSize")
        
        val croppedBitmap = if (originalSize != size) {
            // Önce kare kırp, sonra resize et
            val squareBitmap = Bitmap.createBitmap(originalBitmap, x, y, originalSize, originalSize)
            val resized = Bitmap.createScaledBitmap(squareBitmap, size, size, true)
            squareBitmap.recycle()
            resized
        } else {
            Bitmap.createBitmap(originalBitmap, x, y, size, size)
        }
        
        // Orijinal bitmap'i temizle
        originalBitmap.recycle()
        
        // Yuvarlak bitmap oluştur (transparent background)
        val circleBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(circleBitmap)
        
        // Canvas'ı transparent yap
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
        
        // Yuvarlak mask için paint
        val maskPaint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            color = 0xFF000000.toInt() // Siyah renk
        }
        
        // Yuvarlak mask çiz
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, maskPaint)
        
        // Xfermode ile sadece yuvarlak içindeki kısmı göster
        val imagePaint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }
        canvas.drawBitmap(croppedBitmap, Rect(0, 0, size, size), Rect(0, 0, size, size), imagePaint)
        
        // Cropped bitmap'i temizle
        croppedBitmap.recycle()
        
        // Base64'e çevir (JPEG formatında, daha küçük boyut)
        val outputStream = ByteArrayOutputStream()
        circleBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream) // %85 kalite, boyutu küçültür
        val byteArray = outputStream.toByteArray()
        outputStream.close()
        
        // Circle bitmap'i temizle
        circleBitmap.recycle()
        
        android.util.Log.d("ImageCropHelper", "Base64 string uzunluğu: ${byteArray.size} bytes")
        
        // Base64 string oluştur
        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        
        // Data URL formatında döndür (Glide ile direkt kullanılabilir)
        "data:image/jpeg;base64,$base64String"
    }
    
    /**
     * EXIF orientation'a göre bitmap'i döndürür
     */
    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(bitmap, horizontal = true, vertical = false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipBitmap(bitmap, horizontal = false, vertical = true)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                val rotated = rotateBitmap(bitmap, 90f)
                flipBitmap(rotated, horizontal = true, vertical = false)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                val rotated = rotateBitmap(bitmap, 270f)
                flipBitmap(rotated, horizontal = true, vertical = false)
            }
            else -> bitmap
        }
    }
    
    /**
     * Bitmap'i belirtilen açıyla döndürür
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return rotated
    }
    
    /**
     * Bitmap'i yatay veya dikey olarak çevirir
     */
    private fun flipBitmap(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix().apply {
            if (horizontal) {
                postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
            }
            if (vertical) {
                postScale(1f, -1f, bitmap.width / 2f, bitmap.height / 2f)
            }
        }
        val flipped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return flipped
    }
    
    /**
     * Bitmap yükleme için uygun sample size'ı hesaplar (memory optimizasyonu)
     */
    private fun calculateInSampleSize(uri: Uri, context: Context, maxSize: Int): Int {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return 1
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()
        
        var inSampleSize = 1
        val width = options.outWidth
        val height = options.outHeight
        
        if (width > maxSize || height > maxSize) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            
            while ((halfWidth / inSampleSize) >= maxSize && (halfHeight / inSampleSize) >= maxSize) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
}

