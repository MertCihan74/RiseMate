package com.parthenios.risemate.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.parthenios.risemate.R
import com.parthenios.risemate.data.SkillQuizData
import com.parthenios.risemate.data.User
import com.parthenios.risemate.databinding.ActivitySkillVerificationBinding
import com.parthenios.risemate.ui.quiz.QuizActivity
import com.parthenios.risemate.utils.UserPreferences

class SkillVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySkillVerificationBinding
    private lateinit var userPreferences: UserPreferences
    private var user: User? = null
    private lateinit var adapter: VerifiableSkillsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkillVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        user = userPreferences.getUser()

        setupToolbar()
        setupRecyclerView()
        loadVerifiableSkills()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = VerifiableSkillsAdapter { skill ->
            startQuiz(skill)
        }

        binding.recyclerViewSkills.apply {
            layoutManager = LinearLayoutManager(this@SkillVerificationActivity)
            adapter = this@SkillVerificationActivity.adapter
        }
    }

    private fun loadVerifiableSkills() {
        val knownSkills = user?.knownSkills ?: emptyList()
        val verifiedSkills = user?.verifiedSkills ?: emptyList()

        if (knownSkills.isEmpty()) {
            showEmptyState("Henüz bildiğiniz yetenek eklenmemiş")
            return
        }

        // Doğrulanabilir yetenekleri filtrele
        val verifiableSkills = knownSkills.filter { skill ->
            !verifiedSkills.contains(skill) && SkillQuizData.hasQuizForSkill(skill)
        }

        if (verifiableSkills.isEmpty()) {
            val hasAnyVerified = knownSkills.any { verifiedSkills.contains(it) }
            val message = if (hasAnyVerified) {
                "Tüm yetenekleriniz zaten doğrulanmış! 🎉"
            } else {
                "Doğrulanabilir yetenek bulunamadı. Bazı yetenekler için henüz quiz hazırlanmamış olabilir."
            }
            showEmptyState(message)
            return
        }

        adapter.submitList(verifiableSkills)
    }

    private fun showEmptyState(message: String) {
        binding.recyclerViewSkills.visibility = android.view.View.GONE
        binding.emptyStateLayout.visibility = android.view.View.VISIBLE
        binding.tvEmptyMessage.text = message
    }

    private fun startQuiz(skillName: String) {
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra(QuizActivity.EXTRA_SKILL_NAME, skillName)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Quiz'den dönünce listeyi yenile (yeteneğin doğrulanması için)
        user = userPreferences.getUser()
        loadVerifiableSkills()
    }
}






