package com.parthenios.risemate.ui.group

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.data.SkillGroup
import com.parthenios.risemate.databinding.FragmentGroupListBinding
import com.parthenios.risemate.utils.GroupInitializer
import com.parthenios.risemate.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GroupListFragment : Fragment() {

    private var _binding: FragmentGroupListBinding? = null
    private val binding get() = _binding!!

    private lateinit var groupAdapter: GroupAdapter
    private lateinit var userPreferences: UserPreferences
    private val firestore = FirebaseFirestore.getInstance()
    
    private var filterType: String = "common" // "common", "known", "wanted"
    private var loadGroupsJob: Job? = null

    companion object {
        private const val ARG_FILTER_TYPE = "filter_type"

        fun newInstance(filterType: String): GroupListFragment {
            return GroupListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FILTER_TYPE, filterType)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filterType = it.getString(ARG_FILTER_TYPE, "common")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences(requireContext())
        setupRecyclerView()
        setupSwipeRefresh()
        loadGroups()
    }

    private fun setupRecyclerView() {
        groupAdapter = GroupAdapter(
            onGroupClick = { group -> showGroupDetail(group) },
            onJoinClick = { group -> joinGroup(group) }
        )

        binding.recyclerViewGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener { loadGroups() }
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary_color,
            R.color.secondary_color,
            R.color.gold_accent
        )
    }

    private fun loadGroups() {
        if (!isAdded || _binding == null) return

        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE

        loadGroupsJob?.cancel()
        loadGroupsJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val user = userPreferences.getUser()

                if (currentUserId == null || user == null) {
                    withContext(Dispatchers.Main) {
                        if (isAdded) showEmptyState("Kullanıcı bilgisi bulunamadı")
                    }
                    return@launch
                }

                // Filter tipine göre grupları yükle
                val allGroups = when (filterType) {
                    "common" -> loadCommonGroups(user.city)
                    "known" -> loadSkillGroups(user.knownSkills)
                    "wanted" -> loadSkillGroups(user.wantedSkills)
                    else -> loadCommonGroups(user.city)
                }

                // Üye olunan grupları kontrol et
                val userGroups = try {
                    val memberDocs = firestore.collection("groupMembers")
                        .whereEqualTo("userId", currentUserId)
                        .get()
                        .await()
                    memberDocs.documents.mapNotNull { it.getString("groupId") }.toSet()
                } catch (e: Exception) {
                    emptySet()
                }

                // Sonuçları üye sayısına göre sırala
                val sortedGroups = allGroups.sortedByDescending { it.memberCount }

                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext

                    groupAdapter.setJoinedGroups(userGroups)
                    groupAdapter.submitList(sortedGroups)

                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false

                    if (sortedGroups.isEmpty()) {
                        val message = when (filterType) {
                            "common" -> "Ortak gruplar yüklenirken bir sorun oluştu.\nLütfen tekrar deneyin."
                            "known" -> "Bildiğiniz yetenekler için henüz grup yok.\nProfilinizden yetenek ekleyebilirsiniz."
                            "wanted" -> "Öğrenmek istediğiniz yetenekler için henüz grup yok.\nProfilinizden yetenek ekleyebilirsiniz."
                            else -> "Grup bulunamadı."
                        }
                        showEmptyState(message)
                    } else {
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.recyclerViewGroups.visibility = View.VISIBLE
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    showEmptyState("Gruplar yüklenirken hata: ${e.message}")
                }
            }
        }
    }

    /**
     * Ortak grupları yükler: Proje İşbirliği + Kullanıcının şehir grubu
     */
    private suspend fun loadCommonGroups(userCity: String): List<SkillGroup> {
        return GroupInitializer.getCommonGroupsForUser(userCity)
    }

    /**
     * Yetenek bazlı grupları yükler
     */
    private suspend fun loadSkillGroups(skills: List<String>): List<SkillGroup> {
        if (skills.isEmpty()) return emptyList()

        val allGroups = mutableListOf<SkillGroup>()
        
        // Firestore whereIn sadece 10 eleman destekler, parçalayarak çekelim
        val skillChunks = skills.distinct().chunked(10)
        
        for (chunk in skillChunks) {
            try {
                val result = firestore.collection("groups")
                    .whereIn("skill", chunk)
                    .get()
                    .await()

                val groups = result.documents.mapNotNull { doc ->
                    try {
                        val group = doc.toObject(SkillGroup::class.java)?.copy(id = doc.id)
                        // Şehir ve proje gruplarını hariç tut (groupType kontrolü)
                        val groupType = doc.getString("groupType") ?: "skill"
                        if (groupType == "skill" || groupType.isEmpty()) group else null
                    } catch (e: Exception) {
                        null
                    }
                }
                allGroups.addAll(groups)
            } catch (e: Exception) {
                // Chunk hatası olursa devam et
            }
        }

        return allGroups
    }

    private fun showEmptyState(message: String) {
        if (!isAdded || _binding == null) return
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.recyclerViewGroups.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false
        
        val title = when (filterType) {
            "common" -> "Ortak Gruplar"
            "known" -> "Bildiğim Yetenekler"
            "wanted" -> "Öğrenmek İstediğim"
            else -> "Gruplar"
        }
        binding.tvEmptyTitle.text = title
        binding.tvEmptyMessage.text = message
    }

    private fun showGroupDetail(group: SkillGroup) {
        if (isAdded && context != null) {
            val intent = Intent(requireContext(), GroupChatActivity::class.java)
            intent.putExtra(GroupChatActivity.EXTRA_GROUP, group)
            startActivity(intent)
        }
    }

    private fun joinGroup(group: SkillGroup) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val user = userPreferences.getUser()

        if (currentUser == null || user == null) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Lütfen giriş yapın", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Bu yetenek kullanıcının bildiği mi yoksa öğrenmek istediği mi?
        // Ortak gruplar için "common" kullan
        val skillType = when {
            group.groupType == "project" -> "common"
            group.groupType == "city" -> "common"
            user.knownSkills.contains(group.skill) -> "known"
            user.wantedSkills.contains(group.skill) -> "wanted"
            else -> "unknown"
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Zaten üye mi kontrol et
                val existingMember = firestore.collection("groupMembers")
                    .whereEqualTo("groupId", group.id)
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                if (!existingMember.isEmpty) {
                    withContext(Dispatchers.Main) {
                        if (isAdded && context != null) {
                            Toast.makeText(requireContext(), "Bu gruba zaten üyesiniz", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@launch
                }

                // Gruba üye ekle
                val memberData = hashMapOf(
                    "groupId" to group.id,
                    "groupName" to group.name,
                    "userId" to currentUser.uid,
                    "userName" to "${user.firstName} ${user.lastName}",
                    "userPhotoUrl" to user.photoUrl,
                    "skillType" to skillType,
                    "joinedAt" to java.util.Date()
                )

                firestore.collection("groupMembers").add(memberData).await()

                // Üye sayısını artır
                firestore.collection("groups").document(group.id)
                    .update("memberCount", group.memberCount + 1)
                    .await()

                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    Toast.makeText(requireContext(), "${group.name} grubuna katıldınız!", Toast.LENGTH_SHORT).show()
                    loadGroups()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "Gruba katılırken hata: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadGroupsJob?.cancel()
        _binding = null
    }
}
