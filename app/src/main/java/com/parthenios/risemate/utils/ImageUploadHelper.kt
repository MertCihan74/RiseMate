package com.parthenios.risemate.utils

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.util.UUID

object ImageUploadHelper {
    private val storage = FirebaseStorage.getInstance()
    
    suspend fun uploadProfilePhoto(context: Context, userId: String, imageUri: Uri): String {
        try {
            val fileName = "profile_photos/${userId}_${UUID.randomUUID()}.png"
            android.util.Log.d("ImageUploadHelper", "Dosya adı oluşturuldu: $fileName, URI: $imageUri")
            
            // URI'nin geçerli olduğunu kontrol et
            val scheme = imageUri.scheme
            android.util.Log.d("ImageUploadHelper", "URI scheme: $scheme")
            
            if (scheme == null) {
                throw Exception("Geçersiz URI: scheme null")
            }
            
            val storageRef = storage.reference.child(fileName)
            android.util.Log.d("ImageUploadHelper", "Storage referansı oluşturuldu")
            
            // Metadata ile PNG formatında yükle
            val metadata = StorageMetadata.Builder()
                .setContentType("image/png")
                .build()
            android.util.Log.d("ImageUploadHelper", "Metadata oluşturuldu: image/png")
            
            // Dosyayı URI'den direkt yükle (putFile otomatik olarak InputStream'i yönetir)
            android.util.Log.d("ImageUploadHelper", "Dosya yükleniyor...")
            storageRef.putFile(imageUri, metadata).await()
            android.util.Log.d("ImageUploadHelper", "Dosya yükleme tamamlandı")
            
            // Download URL'i al
            android.util.Log.d("ImageUploadHelper", "Download URL alınıyor...")
            val downloadUrl = storageRef.downloadUrl.await()
            android.util.Log.d("ImageUploadHelper", "Download URL alındı: $downloadUrl")
            
            return downloadUrl.toString()
        } catch (e: Exception) {
            android.util.Log.e("ImageUploadHelper", "Fotoğraf yüklenirken hata", e)
            e.printStackTrace()
            throw Exception("Fotoğraf yüklenirken hata oluştu: ${e.message}", e)
        }
    }
    
    suspend fun deleteProfilePhoto(photoUrl: String) {
        if (photoUrl.isEmpty()) return
        
        try {
            // URL'den referans al
            val storageRef = storage.getReferenceFromUrl(photoUrl)
            
            // Dosyayı sil (yoksa hata vermez, sessizce devam eder)
            try {
                storageRef.delete().await()
            } catch (e: Exception) {
                // Dosya zaten yoksa veya erişilemiyorsa, hata yok sayılabilir
                // Bu durumda sessizce devam et
            }
        } catch (e: Exception) {
            // URL geçersizse veya başka bir hata varsa, sessizce devam et
            // Fotoğraf zaten silinmiş olabilir veya URL geçersiz olabilir
        }
    }
}

