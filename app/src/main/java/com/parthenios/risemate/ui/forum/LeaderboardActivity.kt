package com.parthenios.risemate.ui.forum

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.data.LeaderboardEntry
import com.parthenios.risemate.databinding.ActivityLeaderboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class LeaderboardActivity : AppCompatActivity() {

    companion object {
        // Türkiye saat dilimi (UTC+3)
        private val TIMEZONE_TURKEY = TimeZone.getTimeZone("Europe/Istanbul")
    }

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = resources.getColor(R.color.primary_color, theme)

        setupToolbar()
        setupRecyclerView()
        setupWeekRange()
        loadLeaderboard()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter()
        binding.recyclerViewLeaderboard.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = leaderboardAdapter
        }
    }

    private fun setupWeekRange() {
        val (weekStart, weekEnd) = getWeekRange()
        val dateFormat = SimpleDateFormat("dd MMM", Locale("tr"))
        dateFormat.timeZone = TIMEZONE_TURKEY
        
        binding.tvWeekRange.text = "${dateFormat.format(weekStart)} - ${dateFormat.format(weekEnd)}"
    }

    /**
     * Türkiye saat dilimine göre hafta aralığını hesaplar
     * Her hafta Pazartesi 00:00'da başlar, Pazar 23:59'da biter
     */
    private fun getWeekRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance(TIMEZONE_TURKEY, Locale("tr"))
        
        // Haftanın ilk gününü Pazartesi olarak ayarla (locale bağımsız)
        calendar.firstDayOfWeek = Calendar.MONDAY
        
        // Bu haftanın Pazartesi'sini bul
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.time
        
        // Pazar'ı bul (Pazartesi + 6 gün)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val weekEnd = calendar.time
        
        return Pair(weekStart, weekEnd)
    }

    private fun loadLeaderboard() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Türkiye saat dilimine göre haftanın başlangıç ve bitiş tarihini al
                val (weekStart, weekEnd) = getWeekRange()

                // Kabul edilen cevapları çek (index gerektirmez)
                val answersSnapshot = firestore.collection("answers")
                    .whereEqualTo("isAccepted", true)
                    .get()
                    .await()

                // Kullanıcı bazında sayım yap (client-side hafta filtresi)
                val userCounts = mutableMapOf<String, Int>()
                val userNames = mutableMapOf<String, String>()
                val userPhotos = mutableMapOf<String, String>()
                val userLastAcceptedTime = mutableMapOf<String, Long>() // Son cevap onay zamanı

                answersSnapshot.documents.forEach { doc ->
                    // Hafta filtresi (client-side) - Türkiye saat dilimine göre
                    val createdAt = doc.getTimestamp("createdAt")?.toDate()
                    if (createdAt == null || createdAt.before(weekStart) || createdAt.after(weekEnd)) {
                        return@forEach
                    }
                    
                    val userId = doc.getString("userId") ?: return@forEach
                    val userName = doc.getString("userName") ?: ""
                    val userPhoto = doc.getString("userPhotoUrl") ?: ""
                    val timestamp = createdAt.time
                    
                    userCounts[userId] = (userCounts[userId] ?: 0) + 1
                    userNames[userId] = userName
                    userPhotos[userId] = userPhoto
                    
                    // En son kabul edilen cevabın zamanını tut
                    // (aynı puan olduğunda daha erken bitiren üstte olacak)
                    val currentLastTime = userLastAcceptedTime[userId] ?: 0L
                    if (timestamp > currentLastTime) {
                        userLastAcceptedTime[userId] = timestamp
                    }
                }

                // Sırala: Önce cevap sayısına göre (çoktan aza)
                // Aynı sayıda ise, son cevabı daha erken verene göre (küçük timestamp üstte)
                val leaderboard = userCounts.entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String, Int>> { it.value }
                            .thenBy { userLastAcceptedTime[it.key] ?: Long.MAX_VALUE }
                    )
                    .take(10)
                    .mapIndexed { index, entry ->
                        LeaderboardEntry(
                            userId = entry.key,
                            userName = userNames[entry.key] ?: "",
                            userPhotoUrl = userPhotos[entry.key] ?: "",
                            acceptedAnswerCount = entry.value,
                            rank = index + 1
                        )
                    }

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    displayLeaderboard(leaderboard)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@LeaderboardActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayLeaderboard(leaderboard: List<LeaderboardEntry>) {
        if (leaderboard.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            return
        }

        // Top 3
        if (leaderboard.isNotEmpty()) {
            binding.layout1stPlace.visibility = View.VISIBLE
            val first = leaderboard[0]
            binding.tv1stName.text = first.userName
            binding.tv1stCount.text = "${first.acceptedAnswerCount} cevap"
            loadPhoto(first.userPhotoUrl, binding.iv1stPhoto)
        }

        if (leaderboard.size >= 2) {
            binding.layout2ndPlace.visibility = View.VISIBLE
            val second = leaderboard[1]
            binding.tv2ndName.text = second.userName
            binding.tv2ndCount.text = "${second.acceptedAnswerCount} cevap"
            loadPhoto(second.userPhotoUrl, binding.iv2ndPhoto)
        }

        if (leaderboard.size >= 3) {
            binding.layout3rdPlace.visibility = View.VISIBLE
            val third = leaderboard[2]
            binding.tv3rdName.text = third.userName
            binding.tv3rdCount.text = "${third.acceptedAnswerCount} cevap"
            loadPhoto(third.userPhotoUrl, binding.iv3rdPhoto)
        }

        // 4-10 arası
        if (leaderboard.size > 3) {
            val restOfList = leaderboard.drop(3)
            leaderboardAdapter.submitList(restOfList)
        }
    }

    private fun loadPhoto(photoUrl: String, imageView: ImageView) {
        if (photoUrl.startsWith("data:image")) {
            try {
                val base64Data = photoUrl.substringAfter("base64,")
                val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.default_avatar)
            }
        } else {
            imageView.setImageResource(R.drawable.default_avatar)
        }
    }
}

