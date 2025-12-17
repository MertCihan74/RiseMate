package com.parthenios.risemate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.data.Post
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.FragmentUserPostsBinding
import com.parthenios.risemate.ui.social.PostsAdapter

class UserPostsFragment : Fragment() {

    private var _binding: FragmentUserPostsBinding? = null
    private val binding get() = _binding!!
    private lateinit var postsAdapter: PostsAdapter
    private var user: User? = null
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Kullanıcı bilgilerini al
        user = arguments?.getSerializable("user") as? User
        val userIdArg = arguments?.getString("userId")

        // Eğer user null ise kendi profilimiz
        if (user == null && userIdArg.isNullOrEmpty()) {
            // Kendi profilimiz için current user'ı kullan
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null) {
                loadUserPosts(currentUserId)
            }
        } else if (!userIdArg.isNullOrEmpty()) {
            loadUserPosts(userIdArg)
        } else {
            // Başka kullanıcının gönderileri
            loadUserPosts(user!!.uid)
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter(
            onLikeClick = { post -> handleLikeClick(post) },
            onCommentClick = { post -> handleCommentClick(post) },
            onShareClick = { post -> handleShareClick(post) },
            onDeleteClick = { post -> handleDeleteClick(post) }
        )
        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postsAdapter
        }
    }

    private fun loadUserPosts(userId: String) {
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.mapNotNull { document ->
                    try {
                        document.toObject(Post::class.java).copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.timestamp }
                postsAdapter.submitList(posts)
                binding.progressBar.visibility = View.GONE

                if (posts.isEmpty()) {
                    binding.recyclerViewPosts.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.recyclerViewPosts.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gönderiler yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun handleLikeClick(post: Post) {
        // Profil sayfasında beğeni değişikliği yapma
        Toast.makeText(requireContext(), "Beğeni özelliği ana sayfadan kullanılabilir", Toast.LENGTH_SHORT).show()
    }

    private fun handleCommentClick(post: Post) {
        // Profil sayfasında yorum yapma
        Toast.makeText(requireContext(), "Yorum özelliği ana sayfadan kullanılabilir", Toast.LENGTH_SHORT).show()
    }

    private fun handleShareClick(post: Post) {
        // Paylaşma işlemi
        try {
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, "\"${post.content}\" - ${post.userName}\n\nRiseMate uygulaması ile paylaşıldı")
                type = "text/plain"
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Gönderiyi paylaş"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Paylaşım yapılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleDeleteClick(post: Post) {
        // Sadece kendi gönderilerini silebilir
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Oturum açmalısınız", Toast.LENGTH_SHORT).show()
            return
        }

        if (post.userId != currentUserId) {
            Toast.makeText(requireContext(), "Sadece kendi gönderilerinizi silebilirsiniz", Toast.LENGTH_SHORT).show()
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
        firestore.collection("posts").document(post.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Gönderi silindi", Toast.LENGTH_SHORT).show()
                // Listeyi yenile
                val userId = user?.uid ?: FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    loadUserPosts(userId)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gönderi silinirken hata oluştu", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
