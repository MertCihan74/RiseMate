package com.parthenios.risemate.ui.forum

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.parthenios.risemate.data.Question
import com.parthenios.risemate.databinding.FragmentQuestionListBinding
import com.parthenios.risemate.utils.UserPreferences
import java.util.Date

class QuestionListFragment : Fragment() {

    private var _binding: FragmentQuestionListBinding? = null
    private val binding get() = _binding!!
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private lateinit var questionAdapter: QuestionAdapter
    private lateinit var userPreferences: UserPreferences
    private var questionsListener: ListenerRegistration? = null
    
    private var currentFilterMode = FilterMode.ALL
    private var allQuestions: List<Question> = emptyList()
    
    enum class FilterMode {
        ALL,      // Tüm sorular
        FOR_ME    // Benim yeteneklerimle ilgili
    }

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userPreferences = UserPreferences(requireContext())
        
        setupRecyclerView()
        setupSwipeRefresh()
        loadQuestions()
    }

    private fun setupRecyclerView() {
        val currentUserId = auth.currentUser?.uid ?: ""
        
        questionAdapter = QuestionAdapter(
            currentUserId = currentUserId,
            onQuestionClick = { question -> openQuestionDetail(question) },
            onLikeClick = { question -> toggleLike(question) }
        )

        binding.recyclerViewQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = questionAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadQuestions()
        }
    }

    fun refreshQuestions() {
        loadQuestions()
    }

    fun setFilterMode(mode: FilterMode) {
        currentFilterMode = mode
        applyFilter()
    }

    private fun applyFilter() {
        val user = userPreferences.getUser()
        val userSkills = (user?.knownSkills ?: emptyList()) + (user?.wantedSkills ?: emptyList())
        
        val filtered = when (currentFilterMode) {
            FilterMode.ALL -> allQuestions
            FilterMode.FOR_ME -> {
                if (userSkills.isEmpty()) {
                    allQuestions
                } else {
                    allQuestions.filter { question ->
                        userSkills.contains(question.skill)
                    }
                }
            }
        }
        
        questionAdapter.submitList(filtered)
        binding.layoutEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadQuestions() {
        if (!isAdded) return
        
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE

        questionsListener?.remove()
        
        questionsListener = firestore.collection("questions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (!isAdded || _binding == null) return@addSnapshotListener
                
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                
                if (error != null) {
                    android.util.Log.e("QuestionListFragment", "Error loading questions", error)
                    return@addSnapshotListener
                }
                
                if (snapshot == null) return@addSnapshotListener
                
                // Client-side filtering: isDeleted = false olanları filtrele
                allQuestions = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        
                        // Silinmiş soruları atla
                        val isDeleted = data["isDeleted"] as? Boolean ?: false
                        if (isDeleted) return@mapNotNull null
                        
                        @Suppress("UNCHECKED_CAST")
                        Question(
                            id = doc.id,
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "",
                            userPhotoUrl = data["userPhotoUrl"] as? String ?: "",
                            title = data["title"] as? String ?: "",
                            content = data["content"] as? String ?: "",
                            skill = data["skill"] as? String ?: "",
                            category = data["category"] as? String ?: "",
                            imageUrl = data["imageUrl"] as? String ?: "",
                            answerCount = (data["answerCount"] as? Long)?.toInt() ?: 0,
                            viewCount = (data["viewCount"] as? Long)?.toInt() ?: 0,
                            likeCount = (data["likeCount"] as? Long)?.toInt() ?: 0,
                            likedBy = data["likedBy"] as? List<String> ?: emptyList(),
                            isResolved = data["isResolved"] as? Boolean ?: false,
                            acceptedAnswerId = data["acceptedAnswerId"] as? String ?: "",
                            createdAt = doc.getTimestamp("createdAt")?.toDate() ?: Date(),
                            updatedAt = doc.getTimestamp("updatedAt")?.toDate() ?: Date(),
                            isDeleted = false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                
                applyFilter()
            }
    }

    private fun openQuestionDetail(question: Question) {
        // View count artır
        firestore.collection("questions").document(question.id)
            .update("viewCount", FieldValue.increment(1))
        
        val intent = Intent(requireContext(), QuestionDetailActivity::class.java)
        intent.putExtra("question", question)
        startActivity(intent)
    }

    private fun toggleLike(question: Question) {
        val currentUserId = auth.currentUser?.uid ?: return
        val questionRef = firestore.collection("questions").document(question.id)
        
        val isLiked = question.likedBy.contains(currentUserId)
        
        if (isLiked) {
            // Unlike
            questionRef.update(
                mapOf(
                    "likedBy" to FieldValue.arrayRemove(currentUserId),
                    "likeCount" to FieldValue.increment(-1)
                )
            )
        } else {
            // Like
            questionRef.update(
                mapOf(
                    "likedBy" to FieldValue.arrayUnion(currentUserId),
                    "likeCount" to FieldValue.increment(1)
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        questionsListener?.remove()
        _binding = null
    }
}
