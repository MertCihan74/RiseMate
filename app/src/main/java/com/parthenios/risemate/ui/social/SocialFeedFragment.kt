package com.parthenios.risemate.ui.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.parthenios.risemate.R
import com.parthenios.risemate.databinding.FragmentSocialFeedBinding
import com.parthenios.risemate.data.Post
import com.parthenios.risemate.utils.UserPreferences

class SocialFeedFragment : Fragment() {

    private var _binding: FragmentSocialFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var postsAdapter: PostsAdapter
    private var currentSortType: SortType = SortType.TRENDING

    // Her tab için scroll pozisyonunu sakla
    private val scrollPositions = mutableMapOf<SortType, Int>()
    private val scrollOffsets = mutableMapOf<SortType, Int>()

    enum class SortType {
        RECENT, // Akış - en yeni
        TRENDING, // Trendler - en çok beğeni
        MY_POSTS // Gönderilerim - kendi gönderilerim
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadPosts()
        }

        // Set refresh colors
        binding.swipeRefreshLayout.setColorSchemeResources(
            com.parthenios.risemate.R.color.primary_color,
            com.parthenios.risemate.R.color.secondary_color,
            com.parthenios.risemate.R.color.gold_accent
        )

        setupRecyclerView()
        setupTabLayoutFromActivity()
        setupScrollListener()
        loadPosts()
    }

    private fun setupTabLayoutFromActivity() {
        // TabLayout'ı activity'den al
        val activity = requireActivity() as SocialActivity
        val tabLayout = activity.findViewById<com.google.android.material.tabs.TabLayout>(com.parthenios.risemate.R.id.tabLayout)

        tabLayout.addTab(tabLayout.newTab().setText("Trendler"))
        tabLayout.addTab(tabLayout.newTab().setText("Akış"))
        tabLayout.addTab(tabLayout.newTab().setText("Gönderilerim"))

        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                // Mevcut tab'ın scroll pozisyonunu kaydet
                saveCurrentScrollPosition()

                val newSortType = when (tab.position) {
                    0 -> SortType.TRENDING
                    1 -> SortType.RECENT
                    2 -> SortType.MY_POSTS
                    else -> SortType.RECENT
                }

                currentSortType = newSortType
                loadPosts()

                // Yeni tab'ın scroll pozisyonunu geri yükle
                restoreScrollPosition(newSortType)
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })
    }

    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter(
            onLikeClick = { post -> handleLikeClick(post) },
            onCommentClick = { post -> handleCommentClick(post) },
            onShareClick = { post -> handleShareClick(post) },
            onDeleteClick = { post -> handleDeleteClick(post) },
            onUserClick = { post -> openUserProfile(post) }
        )
        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postsAdapter
        }
    }

    private fun setupScrollListener() {
        binding.recyclerViewPosts.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Scroll pozisyonunu sürekli güncelle
                saveCurrentScrollPosition()
            }
        })
    }

    private fun saveCurrentScrollPosition() {
        val layoutManager = binding.recyclerViewPosts.layoutManager as? LinearLayoutManager ?: return
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val firstVisibleView = layoutManager.findViewByPosition(firstVisibleItemPosition)

        val offset = firstVisibleView?.top ?: 0

        scrollPositions[currentSortType] = firstVisibleItemPosition
        scrollOffsets[currentSortType] = offset
    }

    private fun restoreScrollPosition(sortType: SortType) {
        val position = scrollPositions[sortType] ?: 0
        val offset = scrollOffsets[sortType] ?: 0

        binding.recyclerViewPosts.post {
            val layoutManager = binding.recyclerViewPosts.layoutManager as? LinearLayoutManager ?: return@post
            layoutManager.scrollToPositionWithOffset(position, offset)
        }
    }

    private fun handleLikeClick(post: Post) {
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            android.widget.Toast.makeText(requireContext(), "Oturum açmalısınız", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val postRef = firestore.collection("posts").document(post.id)

        // Kullanıcı zaten beğenmiş mi kontrol et
        val isLiked = post.likes.contains(currentUserId)

        if (isLiked) {
            // Beğeniyi kaldır
            postRef.update(
                "likes", com.google.firebase.firestore.FieldValue.arrayRemove(currentUserId),
                "likeCount", post.likeCount - 1
            ).addOnSuccessListener {
                android.widget.Toast.makeText(requireContext(), "Beğeni kaldırıldı", android.widget.Toast.LENGTH_SHORT).show()
                loadPosts() // Listeyi yenile
            }.addOnFailureListener {
                android.widget.Toast.makeText(requireContext(), "Hata oluştu", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            // Beğeni ekle
            postRef.update(
                "likes", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId),
                "likeCount", post.likeCount + 1
            ).addOnSuccessListener {
                android.widget.Toast.makeText(requireContext(), "Beğenildi!", android.widget.Toast.LENGTH_SHORT).show()
                loadPosts() // Listeyi yenile
            }.addOnFailureListener {
                android.widget.Toast.makeText(requireContext(), "Hata oluştu", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCommentClick(post: Post) {
        // X'teki gibi yorum sayfasını aç
        val intent = android.content.Intent(requireContext(), CommentActivity::class.java).apply {
            putExtra(CommentActivity.EXTRA_POST, post)
        }
        startActivity(intent)
    }


    private fun handleShareClick(post: Post) {
        // Paylaşma işlemi - gerçek paylaşım
        try {
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, "\"${post.content}\" - ${post.userName}\n\nRiseMate uygulaması ile paylaşıldı")
                type = "text/plain"
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Gönderiyi paylaş"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "Paylaşım yapılamadı", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUserProfile(post: Post) {
        if (post.userId.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "Kullanıcı bilgisi bulunamadı", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        val intent = android.content.Intent(requireContext(), com.parthenios.risemate.ui.profile.ProfileActivity::class.java)
        
        // Kendi profili değilse userId gönder, kendi profili ise gönderme (kendi profil olarak açılsın)
        if (post.userId != currentUserId) {
            intent.putExtra("userId", post.userId)
        }
        
        startActivity(intent)
    }

    private fun handleDeleteClick(post: Post) {
        // Sadece kendi gönderilerini silebilir
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            android.widget.Toast.makeText(requireContext(), "Oturum açmalısınız", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (post.userId != currentUserId) {
            android.widget.Toast.makeText(requireContext(), "Sadece kendi gönderilerinizi silebilirsiniz", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // Silme onayı al
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Gönderiyi Sil")
            .setMessage("Bu gönderiyi silmek istediğinizden emin misiniz?")
            .setPositiveButton("Sil") { _, _ ->
                deletePost(post)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun deletePost(post: Post) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("posts").document(post.id)
            .delete()
            .addOnSuccessListener {
                android.widget.Toast.makeText(requireContext(), "Gönderi silindi", android.widget.Toast.LENGTH_SHORT).show()
                loadPosts() // Listeyi yenile
            }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(requireContext(), "Gönderi silinirken hata oluştu", android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupTabLayout() {
        // TabLayout'ı activity'den al
        val activity = requireActivity() as SocialActivity
        val tabLayout = activity.findViewById<TabLayout>(com.parthenios.risemate.R.id.tabLayout)

        tabLayout.addTab(tabLayout.newTab().setText("Akış"))
        tabLayout.addTab(tabLayout.newTab().setText("Trendler"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentSortType = when (tab.position) {
                    0 -> SortType.RECENT
                    1 -> SortType.TRENDING
                    else -> SortType.RECENT
                }
                loadPosts()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadPosts() {
        if (!isAdded || _binding == null) return
        
        binding.progressBar.visibility = View.VISIBLE

        val firestore = FirebaseFirestore.getInstance()
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

        val baseQuery = firestore.collection("posts")

        val query = when (currentSortType) {
            SortType.RECENT -> baseQuery
                .orderBy("timestamp", Query.Direction.DESCENDING)
            SortType.TRENDING -> baseQuery
                .orderBy("likeCount", Query.Direction.DESCENDING)
            SortType.MY_POSTS -> {
                if (currentUserId != null && currentUserId.isNotEmpty()) {
                    // Sadece filtrele; sıralamayı istemci tarafında yapacağız (index zorunluluğunu kaldırır)
                    firestore.collection("posts")
                        .whereEqualTo("userId", currentUserId)
                } else {
                    // Kullanıcı giriş yapmamışsa boş sonuç döndür
                    firestore.collection("posts").whereEqualTo("userId", "none")
                }
            }
        }

        query.get()
            .addOnSuccessListener { documents ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                
                val posts = documents.mapNotNull { document ->
                    try {
                        document.toObject(Post::class.java).copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                }.let { list ->
                    if (currentSortType == SortType.MY_POSTS) {
                        // İstemci tarafında zamana göre sırala
                        list.sortedByDescending { it.timestamp }
                    } else list
                }
                postsAdapter.submitList(posts)
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false

                if (posts.isEmpty()) {
                    binding.recyclerViewPosts.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.recyclerViewPosts.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded || _binding == null || context == null) return@addOnFailureListener
                android.widget.Toast.makeText(requireContext(), "Gönderiler yüklenirken hata oluştu", android.widget.Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
