package com.parthenios.risemate.ui.explore

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.slider.RangeSlider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.parthenios.risemate.R
import com.parthenios.risemate.data.MatchFilter
import com.parthenios.risemate.data.MatchRequest
import com.parthenios.risemate.data.MatchRequestStatus
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.FragmentExploreBinding
import com.parthenios.risemate.ui.profile.ProfileActivity
import com.parthenios.risemate.utils.MatchAlgorithm
import com.parthenios.risemate.utils.UserPreferences
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class ExploreFragment : Fragment() {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var userAdapter: UserCardAdapter
    private val users = mutableListOf<User>()
    private lateinit var userPreferences: UserPreferences
    private val allUsers = mutableListOf<User>() // Tüm kullanıcılar
    private lateinit var firestore: FirebaseFirestore
    private var loadUsersJob: Job? = null
    private var sendMatchRequestJob: Job? = null
    
    // Filtre
    private var currentFilter = MatchFilter.DEFAULT
    private val availableCities = mutableListOf<String>()
    
    // Filtre değişikliği callback'i (MainActivity için)
    var onFilterChanged: ((Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userPreferences = UserPreferences(requireContext())
        firestore = FirebaseFirestore.getInstance()
        setupRecyclerView()
        setupFilterUI()
        loadUsersFromFirebase()
    }
    
    private fun setupFilterUI() {
        updateFilterUI()
    }
    
    private fun updateFilterUI() {
        val activeCount = currentFilter.activeFilterCount()
        // MainActivity'ye bildir (toolbar badge için)
        onFilterChanged?.invoke(activeCount)
    }
    
    fun clearFilters() {
        currentFilter = MatchFilter.DEFAULT
        updateFilterUI()
        loadUsersFromFirebase()
    }
    
    fun showFilterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        
        val currentUser = userPreferences.getUser()
        
        // Kendi şehrimdekileri bul
        val checkMyCity = bottomSheetView.findViewById<MaterialCheckBox>(R.id.checkMyCity)
        val tvMyCityName = bottomSheetView.findViewById<android.widget.TextView>(R.id.tvMyCityName)
        val cardMyCity = bottomSheetView.findViewById<View>(R.id.cardMyCity)
        
        // Kullanıcının şehrini göster
        val userCity = currentUser?.city ?: ""
        tvMyCityName.text = if (userCity.isNotBlank()) userCity else "Şehir belirtilmemiş"
        
        // Şehir dropdown
        val actvCity = bottomSheetView.findViewById<AutoCompleteTextView>(R.id.actvCity)
        val tilCity = bottomSheetView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilCity)
        
        // Şehir listesi
        val cities = listOf("Tümü") + availableCities.sorted()
        val cityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
        actvCity.setAdapter(cityAdapter)
        
        // Mevcut seçili şehir
        val isMyCitySelected = currentFilter.city == userCity && userCity.isNotBlank()
        checkMyCity.isChecked = isMyCitySelected
        tilCity.isEnabled = !isMyCitySelected
        
        if (isMyCitySelected) {
            actvCity.setText("", false)
        } else {
            val currentCity = currentFilter.city ?: "Tümü"
            actvCity.setText(currentCity, false)
        }
        
        // Kendi şehrim checkbox'ı
        checkMyCity.setOnCheckedChangeListener { _, isChecked ->
            tilCity.isEnabled = !isChecked
            if (isChecked) {
                actvCity.setText("", false)
            }
        }
        
        cardMyCity.setOnClickListener {
            checkMyCity.isChecked = !checkMyCity.isChecked
        }
        
        // Yaş RangeSlider
        val rangeSliderAge = bottomSheetView.findViewById<RangeSlider>(R.id.rangeSliderAge)
        val tvAgeRange = bottomSheetView.findViewById<android.widget.TextView>(R.id.tvAgeRange)
        
        // Mevcut yaş aralığını ayarla
        val minAge = currentFilter.minAge?.toFloat() ?: 18f
        val maxAge = currentFilter.maxAge?.toFloat() ?: 65f
        rangeSliderAge.values = listOf(minAge, maxAge)
        tvAgeRange.text = "${minAge.toInt()} - ${if (maxAge >= 65) "65+" else maxAge.toInt().toString()}"
        
        rangeSliderAge.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            val min = values[0].toInt()
            val max = values[1].toInt()
            tvAgeRange.text = "$min - ${if (max >= 65) "65+" else max.toString()}"
        }
        
        // Birlikte Öğrenelim switch
        val switchLearnTogether = bottomSheetView.findViewById<SwitchMaterial>(R.id.switchLearnTogether)
        val layoutLearnTogetherInfo = bottomSheetView.findViewById<View>(R.id.layoutLearnTogetherInfo)
        
        switchLearnTogether.isChecked = currentFilter.learnTogetherMode
        layoutLearnTogetherInfo.visibility = if (currentFilter.learnTogetherMode) View.VISIBLE else View.GONE
        
        switchLearnTogether.setOnCheckedChangeListener { _, isChecked ->
            layoutLearnTogetherInfo.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // Temizle butonu - direkt uygula ve kapat
        bottomSheetView.findViewById<View>(R.id.btnClearFilter).setOnClickListener {
            currentFilter = MatchFilter.DEFAULT
            updateFilterUI()
            loadUsersFromFirebase()
            bottomSheetDialog.dismiss()
        }
        
        // Uygula butonu
        bottomSheetView.findViewById<View>(R.id.btnApplyFilter).setOnClickListener {
            // Şehir
            val city = when {
                checkMyCity.isChecked && userCity.isNotBlank() -> userCity
                actvCity.text.toString() == "Tümü" -> null
                actvCity.text.toString().isBlank() -> null
                else -> actvCity.text.toString()
            }
            
            // Yaş
            val ageValues = rangeSliderAge.values
            val selectedMinAge = ageValues[0].toInt()
            val selectedMaxAge = ageValues[1].toInt()
            
            // Eğer tam aralık seçildiyse null yap (filtre yok)
            val finalMinAge = if (selectedMinAge == 18 && selectedMaxAge == 65) null else selectedMinAge
            val finalMaxAge = if (selectedMinAge == 18 && selectedMaxAge == 65) null else if (selectedMaxAge >= 65) null else selectedMaxAge
            
            // Birlikte Öğrenelim
            val learnTogether = switchLearnTogether.isChecked
            
            // Filtreyi güncelle
            currentFilter = MatchFilter(
                city = city,
                minAge = finalMinAge,
                maxAge = finalMaxAge,
                learnTogetherMode = learnTogether
            )
            
            updateFilterUI()
            loadUsersFromFirebase()
            
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetDialog.show()
    }

    private fun setupRecyclerView() {
        userAdapter = UserCardAdapter(
            onUserClick = { user ->
                // Kullanıcı kartına tıklandığında profil sayfasına git
                val intent = Intent(requireContext(), ProfileActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            },
            onSwipeLeft = { user ->
                // Sola kaydırma - kullanıcıyı reddet
                onUserSwipedLeft(user)
            },
            onSwipeRight = { user ->
                // Sağa kaydırma - eşleşme isteği gönder
                onUserSwipedRight(user)
            },
            onSwipeDown = { user ->
                // Aşağı kaydırma - kullanıcı bilgilerini göster
                onUserSwipedDown(user)
            }
        )
        
        binding.recyclerView.apply {
            // Yatay kaydırma - tek kullanıcı görüntüleme
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = userAdapter
            
            // PagerSnapHelper - her seferinde tam bir kart göster
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
            
            // Dikey scroll devre dışı (ileride fotoğraf galerisi için kullanılacak)
            isNestedScrollingEnabled = false
        }
    }
    
    private fun onUserSwipedLeft(user: User) {
        // Kullanıcıyı reddetti - listeden çıkar
        users.remove(user)
        userAdapter.notifyDataSetChanged()
        
        // Eğer kart kalmadıysa yeni eşleşmeler yükle
        if (users.isEmpty()) {
            loadUsersFromFirebase()
        }
    }
    
    private fun onUserSwipedRight(user: User) {
        // Kullanıcıyı beğendi - eşleşme isteği gönder
        val currentUser = userPreferences.getUser()
        if (currentUser != null) {
            val matchScore = MatchAlgorithm.calculateMatchScore(currentUser, user)
            sendMatchRequest(currentUser, user)
            // Bilgilendirme sessiz; toast kaldırıldı
        }
        
        // Kullanıcıyı listeden çıkar
        users.remove(user)
        userAdapter.notifyDataSetChanged()
        
        // Eğer kart kalmadıysa yeni eşleşmeler yükle
        if (users.isEmpty()) {
            loadUsersFromFirebase()
        }
    }
    
    private fun sendMatchRequest(fromUser: User, toUser: User) {
        sendMatchRequestJob?.cancel()
        sendMatchRequestJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Yalnızca PENDING durumdaki aynı hedefe gönderilmiş istek var mı? Varsa sessizce çık
                val existingPending = firestore.collection("matchRequests")
                    .whereEqualTo("fromUserId", fromUser.uid)
                    .whereEqualTo("toUserId", toUser.uid)
                    .whereEqualTo("status", MatchRequestStatus.PENDING.name)
                    .get().await()
                if (!existingPending.isEmpty) return@launch

                val matchRequest = MatchRequest(
                    fromUserId = fromUser.uid,
                    toUserId = toUser.uid,
                    fromUserName = "${fromUser.firstName} ${fromUser.lastName}",
                    toUserName = "${toUser.firstName} ${toUser.lastName}",
                    status = MatchRequestStatus.PENDING
                )
                
                firestore.collection("matchRequests").add(matchRequest).await()
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Eşleşme isteği gönderilirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun onUserSwipedDown(user: User) {
        // Aşağı kaydırma - kullanıcı bilgilerini göster
        if (isAdded && context != null) {
            Toast.makeText(requireContext(), "${user.firstName} ${user.lastName} - ${user.city}, ${user.age} yaşında", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadUsersFromFirebase() {
        val currentUser = userPreferences.getUser()
        if (currentUser == null) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
            }
            return
        }
        
        loadUsersJob?.cancel()
        loadUsersJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Firestore'dan tüm kullanıcıları çek
                val result = firestore.collection("users").get().await()
                
                val firebaseUsers = result.documents.mapNotNull { document ->
                    try {
                        val user = document.toObject(User::class.java)
                        user?.copy(uid = document.id) // UID'yi document ID'den al
                    } catch (e: Exception) {
                        null
                    }
                }.filter { user ->
                    // Mevcut kullanıcıyı hariç tut
                    user.uid != currentUser.uid
                }
                
                // Şehir listesini güncelle (filtre için)
                val cities = firebaseUsers
                    .map { it.city }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                
                // currentUser'ın PENDING gönderdiği isteklerin hedeflerini çek (sadece bekleyenleri gizle)
                val pendingSent = firestore.collection("matchRequests")
                    .whereEqualTo("fromUserId", currentUser.uid)
                    .whereEqualTo("status", MatchRequestStatus.PENDING.name)
                    .get().await()
                val hiddenToUserIds = pendingSent.documents.mapNotNull { it.getString("toUserId") }.toSet()

                // currentUser'ın daha önce eşleştiği kullanıcıları çıkar
                val matches1 = firestore.collection("matches")
                    .whereEqualTo("user1Id", currentUser.uid)
                    .get().await()
                val matches2 = firestore.collection("matches")
                    .whereEqualTo("user2Id", currentUser.uid)
                    .get().await()
                val previouslyMatchedIds = buildSet {
                    matches1.documents.forEach { doc ->
                        doc.getString("user2Id")?.let { add(it) }
                    }
                    matches2.documents.forEach { doc ->
                        doc.getString("user1Id")?.let { add(it) }
                    }
                }
                
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    
                    allUsers.clear()
                    allUsers.addAll(firebaseUsers)
                    
                    // Şehir listesini güncelle
                    availableCities.clear()
                    availableCities.addAll(cities)
                    
                    // Engellenen kullanıcıları al
                    val blockedUserIds = currentUser.blockedUsers.toSet()
                    
                    // Eşleşme algoritması ile potansiyel eşleşmeleri bul (filtreyle birlikte)
                    val potentialMatches = MatchAlgorithm.findPotentialMatchesWithFilter(currentUser, allUsers, currentFilter)
                        .filter { candidate ->
                            candidate.uid !in hiddenToUserIds && 
                            candidate.uid !in previouslyMatchedIds &&
                            candidate.uid !in blockedUserIds
                        }
                    
                    users.clear()
                    users.addAll(potentialMatches)
                    userAdapter.submitList(users.toList())
                    
                    // Empty state güncelle
                    updateEmptyState()
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded || context == null) return@withContext
                    Toast.makeText(requireContext(), "Kullanıcılar yüklenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateEmptyState() {
        binding.layoutEmptyState.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
    }
    

    override fun onDestroyView() {
        super.onDestroyView()
        // Coroutine job'ları iptal et
        loadUsersJob?.cancel()
        sendMatchRequestJob?.cancel()
        _binding = null
    }
}
