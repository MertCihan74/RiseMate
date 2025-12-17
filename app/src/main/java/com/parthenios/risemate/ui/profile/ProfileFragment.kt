package com.parthenios.risemate.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.MainActivity
import com.parthenios.risemate.R
import com.parthenios.risemate.auth.AuthRepository
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.FragmentProfileBinding
import com.parthenios.risemate.ui.auth.LoginActivity
import com.parthenios.risemate.utils.ImageCropHelper
import com.parthenios.risemate.utils.UserPreferences
import com.parthenios.risemate.data.SkillQuizData
import com.parthenios.risemate.data.FollowRepository
import com.parthenios.risemate.ui.quiz.QuizActivity
import com.parthenios.risemate.utils.ReportHelper
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var authRepository: AuthRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var followRepository: FollowRepository
    private var user: User? = null
    private var profileUser: User? = null // Görüntülenen profil sahibi
    private var isOwnProfile = false
    private val firestore = FirebaseFirestore.getInstance()
    
    // Coroutine job'ları
    private var loadUserJob: Job? = null
    private var followStatsJob: Job? = null
    private var followActionJob: Job? = null
    private var blockUserJob: Job? = null
    private var uploadPhotoJob: Job? = null
    private var updateProfileJob: Job? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            startUCrop(it)
        }
    }
    
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val firstName = data.getStringExtra("firstName") ?: ""
                val lastName = data.getStringExtra("lastName") ?: ""
                val age = data.getIntExtra("age", 0)
                val city = data.getStringExtra("city") ?: ""
                val knownSkills = data.getStringArrayExtra("knownSkills")?.toList() ?: emptyList()
                val wantedSkills = data.getStringArrayExtra("wantedSkills")?.toList() ?: emptyList()
                
                updateProfile(firstName, lastName, city, age, knownSkills, wantedSkills)
            }
        }
    }
    
    private val uCropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data ?: return@registerForActivityResult)
            resultUri?.let {
                cropAndUploadPhoto(it)
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data ?: return@registerForActivityResult)
            Toast.makeText(requireContext(), "Fotoğraf kırpılırken hata: ${cropError?.message}", Toast.LENGTH_LONG).show()
            binding.cardProfilePhoto.isClickable = true
        } else {
            binding.cardProfilePhoto.isClickable = true
        }
    }
    
    private fun startUCrop(sourceUri: Uri) {
        try {
            val tempSourceFile = File(requireContext().cacheDir, "temp_source_${System.currentTimeMillis()}.jpg")
            val inputStream = requireContext().contentResolver.openInputStream(sourceUri)
                ?: throw Exception("Fotoğraf dosyası açılamadı")
            
            inputStream.use { input ->
                java.io.FileOutputStream(tempSourceFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            val sourceFileUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                tempSourceFile
            )
            
            val destinationFile = File(requireContext().cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
            val destinationUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                destinationFile
            )
            
            val uCrop = UCrop.of(sourceFileUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(1024, 1024)
                .withOptions(
                    UCrop.Options().apply {
                        setCompressionQuality(90)
                        setHideBottomControls(false)
                        setFreeStyleCropEnabled(false)
                        setCircleDimmedLayer(true)
                        setShowCropFrame(false)
                        setShowCropGrid(false)
                        setDimmedLayerColor(android.graphics.Color.argb(200, 0, 0, 0))
                    }
                )
            
            val intent = uCrop.getIntent(requireContext())
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            
            uCropLauncher.launch(intent)
        } catch (e: Exception) {
            if (isAdded) {
                binding.cardProfilePhoto.isClickable = true
                Toast.makeText(requireContext(), "Fotoğraf kırpma ekranı açılamadı: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun cropAndUploadPhoto(imageUri: Uri) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Kullanıcı giriş yapmamış", Toast.LENGTH_SHORT).show()
            }
            return
        }
        
        if (_binding != null) {
            binding.cardProfilePhoto.isClickable = false
        }
        
        uploadPhotoJob?.cancel()
        uploadPhotoJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val ctx = context ?: return@launch
                val base64Photo = ImageCropHelper.cropToCircleBase64(ctx, imageUri)
                uploadProfilePhoto(base64Photo)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        binding.cardProfilePhoto.isClickable = true
                        if (context != null) {
                            Toast.makeText(requireContext(), "Fotoğraf kırpılırken hata: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository()
        userPreferences = UserPreferences(requireContext())
        followRepository = FollowRepository()

        user = arguments?.getSerializable("user") as? User
        val userIdArg = arguments?.getString("userId")

        if (user == null && !userIdArg.isNullOrEmpty()) {
            loadOtherUserProfile(userIdArg)
            return
        }

        if (user == null) {
            user = userPreferences.getUser()
            if (user != null) {
                setupUI(isCurrentUser = true)
            } else {
                loadCurrentUser()
            }
        } else {
            val otherUserId = user?.uid
            if (otherUserId != null) {
                loadOtherUserProfile(otherUserId)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Quiz'den döndükten sonra kendi profilimizi güncelle
        val currentUser = authRepository.getCurrentUser()
        if (user?.uid == currentUser?.uid) {
            // Firestore'dan güncel verileri al
            refreshCurrentUserData()
        }
    }
    
    private fun refreshCurrentUserData() {
        val currentUser = authRepository.getCurrentUser() ?: return
        
        loadUserJob?.cancel()
        loadUserJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = authRepository.getUserFromFirestore(currentUser.uid)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess && isAdded && _binding != null) {
                        user = result.getOrNull()
                        user?.let { 
                            userPreferences.saveUser(it)
                            setupUI(isCurrentUser = true)
                        }
                    }
                }
            } catch (e: Exception) {
                // Sessizce devam et
            }
        }
    }
    
    private fun loadCurrentUser() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            loadUserJob?.cancel()
            loadUserJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val result = authRepository.getUserFromFirestore(currentUser.uid)
                    withContext(Dispatchers.Main) {
                        if (!isAdded || _binding == null) return@withContext
                        if (result.isSuccess) {
                            user = result.getOrNull()
                            user?.let { userPreferences.saveUser(it) }
                            setupUI(isCurrentUser = true)
                        } else {
                            setupUI(isCurrentUser = true)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isAdded && _binding != null) {
                            setupUI(isCurrentUser = true)
                        }
                    }
                }
            }
        } else {
            setupUI(isCurrentUser = true)
        }
    }
    
    private fun loadOtherUserProfile(userId: String) {
        loadUserJob?.cancel()
        loadUserJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = authRepository.getUserFromFirestore(userId)
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    if (result.isSuccess) {
                        user = result.getOrNull()
                        setupUI(isCurrentUser = false)
                    } else {
                        if (context != null) {
                            Toast.makeText(requireContext(), "Kullanıcı bilgileri yüklenemedi", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Kullanıcı bilgileri yüklenirken hata: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupUI(isCurrentUser: Boolean = true) {
        user?.let { userData ->
            binding.apply {
                tvName.text = "${userData.firstName} ${userData.lastName}"
                tvUsername.text = "@${userData.username}"
                tvCity.text = userData.city

                // Yaş bilgisini göster (sadece sayı)
                val age = if (userData.age > 0) userData.age else calculateAge(userData.birthday)
                tvAge.text = age.toString()

                // Yetenekleri chip olarak göster
                setupSkillChips(userData.knownSkills, userData.wantedSkills)

                // Takipçi ve takip edilen sayılarını göster
                loadFollowStats(userData.uid)

                // Takipçi ve takip edilen kartlarına tıklama özelliği
                setupFollowCardsClickListeners(userData.uid)
                
                // Profil fotoğrafını göster
                if (userData.photoUrl.isNotEmpty()) {
                    Glide.with(this@ProfileFragment)
                        .load(userData.photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile)
                        .into(ivProfilePhoto)
                } else {
                    ivProfilePhoto.setImageResource(R.drawable.ic_profile)
                }
                
                // Email'i sadece kendi profilinde göster
                layoutEmailRow.visibility = if (isCurrentUser) View.VISIBLE else View.GONE
                tvEmail.text = if (isCurrentUser) userData.email else ""
                
                // Kamera butonu ve fotoğraf etkileşimi
                if (isCurrentUser) {
                    btnChangePhoto.visibility = View.VISIBLE
                    btnChangePhoto.setOnClickListener {
                        imagePickerLauncher.launch("image/*")
                    }
                    cardProfilePhoto.setOnClickListener {
                        imagePickerLauncher.launch("image/*")
                    }
                } else {
                    btnChangePhoto.visibility = View.GONE
                    // Diğer kullanıcılarda fotoğrafı büyüt
                    if (userData.photoUrl.isNotEmpty()) {
                        cardProfilePhoto.isClickable = true
                        cardProfilePhoto.isFocusable = true
                        cardProfilePhoto.setOnClickListener {
                            val intent = android.content.Intent(requireContext(), com.parthenios.risemate.ui.social.PostImageViewerActivity::class.java).apply {
                                putExtra(com.parthenios.risemate.ui.social.PostImageViewerActivity.EXTRA_IMAGE_URL, userData.photoUrl)
                            }
                            startActivity(intent)
                        }
                    } else {
                        cardProfilePhoto.isClickable = false
                        cardProfilePhoto.isFocusable = false
                    }
                }
                
                // Düzenle ve çıkış butonları
                if (isCurrentUser) {
                    btnEditProfile.visibility = View.VISIBLE
                    btnEditProfile.setOnClickListener {
                        startEditProfileActivity()
                    }
                    
                    // Yetenek doğrulama butonu
                    btnVerifySkills.visibility = View.VISIBLE
                    btnVerifySkills.setOnClickListener {
                        showVerifySkillsDialog()
                    }
                    
                    btnLogout.visibility = View.VISIBLE
                    btnLogout.setOnClickListener {
                        performLogout()
                    }
                    
                    btnBlockUser.visibility = View.GONE
                    btnReportUser.visibility = View.GONE
                } else {
                    btnEditProfile.visibility = View.GONE
                    btnVerifySkills.visibility = View.GONE
                    btnLogout.visibility = View.GONE

                    // Takip butonu sadece başka kullanıcının profilinde görünsün
                    btnFollow.visibility = View.VISIBLE
                    checkFollowStatus()

                    // Şikayet butonu görünsün
                    btnReportUser.visibility = View.VISIBLE
                    btnReportUser.setOnClickListener {
                        showReportUserBottomSheet()
                    }

                    // Engelleme butonu da görünsün
                    btnBlockUser.visibility = View.VISIBLE
                    btnBlockUser.setOnClickListener {
                        showBlockUserDialog()
                    }
                }
            }
        } ?: run {
            val currentUser = authRepository.getCurrentUser()
            binding.apply {
                tvName.text = currentUser?.displayName ?: "Kullanıcı"
                tvUsername.text = "@${currentUser?.email?.split("@")?.get(0) ?: "kullanici"}"
                tvCity.text = "-"
                tvAge.text = "-"
            }
        }
    }
    
    private fun loadFollowStats(userId: String) {
        followStatsJob?.cancel()
        followStatsJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val followersCount = followRepository.getFollowersCount(userId)
                val followingCount = followRepository.getFollowingCount(userId)

                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        binding.tvFollowersCount.text = followersCount.toString()
                        binding.tvFollowingCount.text = followingCount.toString()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        binding.tvFollowersCount.text = "0"
                        binding.tvFollowingCount.text = "0"
                    }
                }
            }
        }
    }

    private fun checkFollowStatus() {
        val currentUser = authRepository.getCurrentUser()
        val profileUserId = user?.uid

        if (currentUser != null && profileUserId != null && currentUser.uid != profileUserId) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val isFollowing = followRepository.isFollowing(currentUser.uid, profileUserId)

                    withContext(Dispatchers.Main) {
                        if (isAdded && _binding != null) {
                            updateFollowButton(isFollowing)
                            setupFollowButtonClickListener(currentUser.uid, profileUserId)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isAdded && _binding != null) {
                            updateFollowButton(false)
                            setupFollowButtonClickListener(currentUser.uid, profileUserId)
                        }
                    }
                }
            }
        }
    }

    private fun updateFollowButton(isFollowing: Boolean) {
        binding.btnFollow.apply {
            if (isFollowing) {
                text = "✓  Takip Ediliyor"
                setBackgroundResource(R.drawable.button_outline)
                setTextColor(resources.getColor(R.color.primary_color, null))
            } else {
                text = "➕  Takip Et"
                setBackgroundResource(R.drawable.button_primary)
                setTextColor(resources.getColor(R.color.white, null))
            }
        }
    }

    private fun setupFollowButtonClickListener(currentUserId: String, profileUserId: String) {
        binding.btnFollow.setOnClickListener {
            followActionJob?.cancel()
            followActionJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val isCurrentlyFollowing = followRepository.isFollowing(currentUserId, profileUserId)

                    val result = if (isCurrentlyFollowing) {
                        followRepository.unfollowUser(currentUserId, profileUserId)
                    } else {
                        followRepository.followUser(currentUserId, profileUserId)
                    }

                    withContext(Dispatchers.Main) {
                        if (!isAdded || _binding == null) return@withContext
                        
                        if (result.isSuccess) {
                            val newFollowStatus = !isCurrentlyFollowing
                            updateFollowButton(newFollowStatus)

                            // Takipçi sayılarını güncelle
                            loadFollowStats(profileUserId)

                            val message = if (newFollowStatus) "Kullanıcıyı takip etmeye başladın" else "Kullanıcıyı takipten çıktın"
                            if (context != null) {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (context != null) {
                                Toast.makeText(requireContext(), "İşlem başarısız: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isAdded && context != null) {
                            Toast.makeText(requireContext(), "İşlem sırasında hata: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupFollowCardsClickListeners(userId: String) {
        // Takipçi kartına tıklandığında
        binding.tvFollowersCount.setOnClickListener {
            val intent = Intent(requireContext(), FollowersActivity::class.java)
            intent.putExtra(FollowersActivity.EXTRA_USER_ID, userId)
            startActivity(intent)
        }

        // Takip edilen kartına tıklandığında
        binding.tvFollowingCount.setOnClickListener {
            val intent = Intent(requireContext(), FollowingActivity::class.java)
            intent.putExtra(FollowingActivity.EXTRA_USER_ID, userId)
            startActivity(intent)
        }
    }

    private fun setupSkillChips(knownSkills: List<String>, wantedSkills: List<String>) {
        val verifiedSkills = user?.verifiedSkills ?: emptyList()
        val isCurrentUserProfile = user?.uid == authRepository.getCurrentUser()?.uid
        
        binding.apply {
            // Bilinen yetenekler
            flexKnownSkills.removeAllViews()
            if (knownSkills.isNotEmpty()) {
                tvKnownSkills.visibility = View.GONE
                knownSkills.forEach { skill ->
                    val isVerified = verifiedSkills.contains(skill)
                    val chip = createSkillChip(skill, true, isVerified, isCurrentUserProfile)
                    flexKnownSkills.addView(chip)
                }
            } else {
                tvKnownSkills.visibility = View.VISIBLE
                tvKnownSkills.text = "Henüz yetenek eklenmemiş"
            }
            
            // Öğrenmek istenen yetenekler
            flexWantedSkills.removeAllViews()
            if (wantedSkills.isNotEmpty()) {
                tvWantedSkills.visibility = View.GONE
                wantedSkills.forEach { skill ->
                    val chip = createSkillChip(skill, false, false, isCurrentUserProfile)
                    flexWantedSkills.addView(chip)
                }
            } else {
                tvWantedSkills.visibility = View.VISIBLE
                tvWantedSkills.text = "Henüz yetenek eklenmemiş"
            }
        }
    }
    
    private fun createSkillChip(skill: String, isKnown: Boolean, isVerified: Boolean, @Suppress("UNUSED_PARAMETER") isCurrentUser: Boolean): TextView {
        val chip = TextView(requireContext())
        
        // Eğer doğrulanmış ise ✓ ekle
        chip.text = if (isVerified) "$skill ✓" else skill
        chip.textSize = 14f
        chip.setPadding(32, 16, 32, 16)
        
        // Doğrulanmış yetenekler için özel stil
        if (isVerified && isKnown) {
            chip.setBackgroundResource(R.drawable.skill_chip_verified)
            chip.setTextColor(resources.getColor(R.color.white, null))
        } else {
            chip.setBackgroundResource(
                if (isKnown) R.drawable.skill_chip_known else R.drawable.skill_chip_wanted
            )
            chip.setTextColor(
                if (isKnown) 
                    resources.getColor(R.color.skill_known_text, null)
                else 
                    resources.getColor(R.color.skill_wanted_text, null)
            )
        }
        
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 16, 12)
        chip.layoutParams = params
        
        return chip
    }
    
    private fun showVerifySkillsDialog() {
        // Yeni SkillVerificationActivity'yi aç
        val intent = Intent(requireContext(), SkillVerificationActivity::class.java)
        startActivity(intent)
    }
    
    private fun showQuizConfirmDialog(skillName: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📝 $skillName Quiz")
            .setMessage("$skillName yeteneğini doğrulamak için bir quiz çözeceksiniz.\n\n• 5 soru sorulacak\n• 4 doğru cevap gerekli\n• Süre: 5 dakika\n\nHazır mısınız?")
            .setPositiveButton("Quiz'e Başla") { _, _ ->
                startQuiz(skillName)
            }
            .setNegativeButton("İptal", null)
            .show()
    }
    
    private fun startQuiz(skillName: String) {
        val intent = Intent(requireContext(), QuizActivity::class.java).apply {
            putExtra(QuizActivity.EXTRA_SKILL_NAME, skillName)
        }
        startActivity(intent)
    }
    
    private fun performLogout() {
        try {
            // Önce local verileri temizle (onPause/onStop'da Firestore'a erişim engellenir)
            userPreferences.clearUser()
            
            if (isAdded) {
                parentFragmentManager.beginTransaction()
                    .remove(this)
                    .commitAllowingStateLoss()
            }
            
            // Sonra Firebase'den çıkış yap
            authRepository.signOut()
            
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        } catch (e: Exception) {
            userPreferences.clearUser()
            authRepository.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            if (isAdded) {
                requireActivity().finish()
            }
        }
    }
    
    private fun showReportUserBottomSheet() {
        if (user == null || !isAdded) return
        
        val currentFirebaseUser = authRepository.getCurrentUser()
        val currentUserData = userPreferences.getUser()
        
        if (currentFirebaseUser == null || currentUserData == null) {
            Toast.makeText(requireContext(), "Oturum bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }
        
        ReportHelper.showReportBottomSheet(
            context = requireContext(),
            reporterId = currentFirebaseUser.uid,
            reporterName = "${currentUserData.firstName} ${currentUserData.lastName}",
            reportedUser = user!!,
            source = "profile",
            onBlockRequested = {
                blockUser()
            }
        )
    }
    
    private fun showBlockUserDialog() {
        if (user == null || !isAdded) return
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Kullanıcıyı Engelle")
            .setMessage("${user!!.firstName} ${user!!.lastName} kullanıcısını engellemek istediğinize emin misiniz? Engellediğiniz kullanıcıyı keşfet, eşleşme ve mesajlaşma bölümlerinde göremezsiniz.")
            .setPositiveButton("Engelle") { _, _ ->
                blockUser()
            }
            .setNegativeButton("İptal", null)
            .show()
    }
    
    private fun blockUser() {
        val currentFirebaseUser = authRepository.getCurrentUser()
        val targetUser = user
        
        if (currentFirebaseUser == null || targetUser == null) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Kullanıcı bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
            }
            return
        }
        
        blockUserJob?.cancel()
        blockUserJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = currentFirebaseUser.uid
                val blockedUserId = targetUser.uid
                
                // Mevcut blockedUsers listesini al
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                val currentBlocked = userDoc.get("blockedUsers") as? List<*> ?: emptyList<String>()
                val blockedList = (currentBlocked as? List<String>)?.toMutableList() ?: mutableListOf()
                
                // Engellenecek kullanıcı zaten engellenmiş mi kontrol et
                if (blockedList.contains(blockedUserId)) {
                    withContext(Dispatchers.Main) {
                        if (isAdded && context != null) {
                            Toast.makeText(requireContext(), "Bu kullanıcı zaten engellenmiş", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@launch
                }
                
                // İki kullanıcı arasındaki match'leri bul ve sil
                deleteMatchesBetweenUsers(userId, blockedUserId)
                
                // Engellenen kullanıcıyı listeye ekle
                blockedList.add(blockedUserId)
                
                // Firestore'da güncelle
                firestore.collection("users")
                    .document(userId)
                    .update("blockedUsers", blockedList)
                    .await()
                
                // Lokal kullanıcı bilgisini güncelle
                val localUser = userPreferences.getUser()
                if (localUser != null) {
                    val updatedUser = localUser.copy(blockedUsers = blockedList)
                    userPreferences.saveUser(updatedUser)
                }
                
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Kullanıcı engellendi", Toast.LENGTH_SHORT).show()
                        activity?.finish() // Profil ekranından çık
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "Kullanıcı engellenirken hata", e)
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Kullanıcı engellenirken hata: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    private suspend fun deleteMatchesBetweenUsers(userId: String, blockedUserId: String) {
        try {
            // Match'leri bul (user1Id = userId, user2Id = blockedUserId)
            val matches1 = firestore.collection("matches")
                .whereEqualTo("user1Id", userId)
                .whereEqualTo("user2Id", blockedUserId)
                .get()
                .await()
            
            // Match'leri bul (user1Id = blockedUserId, user2Id = userId)
            val matches2 = firestore.collection("matches")
                .whereEqualTo("user1Id", blockedUserId)
                .whereEqualTo("user2Id", userId)
                .get()
                .await()
            
            val allMatches = matches1.documents + matches2.documents
            
            for (matchDoc in allMatches) {
                val matchId = matchDoc.id
                
                // Bu match'e ait tüm mesajları sil
                val messages = firestore.collection("messages")
                    .whereEqualTo("matchId", matchId)
                    .get()
                    .await()
                
                for (msgDoc in messages.documents) {
                    try {
                        firestore.collection("messages").document(msgDoc.id).delete().await()
                    } catch (_: Exception) {}
                }
                
                // Match'i sil
                try {
                    firestore.collection("matches").document(matchId).delete().await()
                } catch (_: Exception) {}
            }
            
            android.util.Log.d("ProfileFragment", "Deleted ${allMatches.size} matches between $userId and $blockedUserId")
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Match silme hatası", e)
        }
    }

    private fun uploadProfilePhoto(base64Photo: String) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Kullanıcı giriş yapmamış", Toast.LENGTH_SHORT).show()
            }
            if (_binding != null) {
                binding.cardProfilePhoto.isClickable = true
            }
            return
        }
        
        if (_binding != null) {
            binding.cardProfilePhoto.isClickable = false
        }
        
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = currentUser.uid
                
                firestore.collection("users").document(userId)
                    .update("photoUrl", base64Photo)
                    .await()
                
                user = user?.copy(photoUrl = base64Photo)
                user?.let { userPreferences.saveUser(it) }
                
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        Glide.with(this@ProfileFragment)
                            .load(base64Photo)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .into(binding.ivProfilePhoto)
                        
                        binding.cardProfilePhoto.isClickable = true
                        if (context != null) {
                            Toast.makeText(requireContext(), "Profil fotoğrafı güncellendi", Toast.LENGTH_SHORT).show()
                        }
                        
                        if (activity is MainActivity) {
                            (activity as MainActivity).onProfilePhotoUpdated()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        binding.cardProfilePhoto.isClickable = true
                        if (context != null) {
                            Toast.makeText(requireContext(), "Fotoğraf kaydedilirken hata: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    
    private fun calculateAge(birthday: String): Int {
        return try {
            val parts = birthday.split("-")
            if (parts.size != 3) return 0
            
            val birthYear = parts[0].toInt()
            val birthMonth = parts[1].toInt()
            val birthDay = parts[2].toInt()
            
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            
            var age = currentYear - birthYear
            
            if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
                age--
            }
            
            age
        } catch (e: Exception) {
            0
        }
    }
    
    private fun startEditProfileActivity() {
        if (user == null) {
            Toast.makeText(requireContext(), "Kullanıcı bilgileri yüklenemedi", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = Intent(requireContext(), EditProfileActivity::class.java)
        intent.putExtra("user", user)
        editProfileLauncher.launch(intent)
    }
    
    private fun updateProfile(
        firstName: String,
        lastName: String,
        city: String,
        age: Int,
        knownSkills: List<String>,
        wantedSkills: List<String>
    ) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Kullanıcı giriş yapmamış", Toast.LENGTH_SHORT).show()
            }
            return
        }
        
        updateProfileJob?.cancel()
        updateProfileJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = currentUser.uid
                
                val updates = hashMapOf<String, Any>(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "city" to city,
                    "age" to age,
                    "knownSkills" to knownSkills,
                    "wantedSkills" to wantedSkills
                )
                
                firestore.collection("users")
                    .document(userId)
                    .update(updates)
                    .await()
                
                user = user?.copy(
                    firstName = firstName,
                    lastName = lastName,
                    city = city,
                    age = age,
                    knownSkills = knownSkills,
                    wantedSkills = wantedSkills
                )
                user?.let { userPreferences.saveUser(it) }
                
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        setupUI(isCurrentUser = true)
                        if (context != null) {
                            Toast.makeText(requireContext(), "Profil güncellendi", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Profil güncellenirken hata: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Tüm coroutine job'ları iptal et
        loadUserJob?.cancel()
        followStatsJob?.cancel()
        followActionJob?.cancel()
        blockUserJob?.cancel()
        uploadPhotoJob?.cancel()
        updateProfileJob?.cancel()
        _binding = null
    }
}
