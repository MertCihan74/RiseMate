package com.parthenios.risemate.ui.quiz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.parthenios.risemate.R
import com.parthenios.risemate.auth.AuthRepository
import com.parthenios.risemate.data.QuizQuestion
import com.parthenios.risemate.data.SkillQuizData
import com.parthenios.risemate.databinding.ActivityQuizBinding
import com.parthenios.risemate.utils.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class QuizActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQuizBinding
    private lateinit var skillName: String
    private lateinit var questions: List<QuizQuestion>
    private lateinit var progressSteps: List<View>
    private lateinit var optionCards: List<View>
    private lateinit var optionLetters: List<android.widget.TextView>
    private lateinit var optionTexts: List<android.widget.TextView>
    
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var selectedOptionIndex = -1
    private var isAnswerRevealed = false
    private var timer: CountDownTimer? = null
    
    companion object {
        const val EXTRA_SKILL_NAME = "skill_name"
        private const val QUIZ_TIME_MS = 5 * 60 * 1000L // 5 dakika
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        skillName = intent.getStringExtra(EXTRA_SKILL_NAME) ?: run {
            Toast.makeText(this, "Yetenek bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Soruları yükle
        questions = SkillQuizData.getRandomQuestionsForQuiz(skillName)
        if (questions.isEmpty()) {
            Toast.makeText(this, "Bu yetenek için quiz bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupViews()
        setupToolbar()
        setupOptions()
        startTimer()
        showQuestion(0)
    }
    
    private fun setupViews() {
        binding.tvSkillName.text = "$skillName Quiz"
        
        progressSteps = listOf(
            binding.progressStep1,
            binding.progressStep2,
            binding.progressStep3,
            binding.progressStep4,
            binding.progressStep5
        )
        
        optionCards = listOf(
            binding.cardOption1,
            binding.cardOption2,
            binding.cardOption3,
            binding.cardOption4
        )
        
        optionLetters = listOf(
            binding.tvOptionLetter1,
            binding.tvOptionLetter2,
            binding.tvOptionLetter3,
            binding.tvOptionLetter4
        )
        
        optionTexts = listOf(
            binding.tvOption1,
            binding.tvOption2,
            binding.tvOption3,
            binding.tvOption4
        )
        
        binding.btnNext.setOnClickListener {
            if (!isAnswerRevealed) {
                revealAnswer()
            } else {
                nextQuestion()
            }
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            showExitConfirmDialog()
        }
    }
    
    private fun setupOptions() {
        optionCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                if (!isAnswerRevealed) {
                    selectOption(index)
                }
            }
        }
    }
    
    private fun startTimer() {
        timer = object : CountDownTimer(QUIZ_TIME_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvTimer.text = String.format("%d:%02d", minutes, seconds)
            }
            
            override fun onFinish() {
                binding.tvTimer.text = "0:00"
                finishQuiz()
            }
        }.start()
    }
    
    private fun showQuestion(index: Int) {
        if (index >= questions.size) {
            finishQuiz()
            return
        }
        
        val question = questions[index]
        currentQuestionIndex = index
        selectedOptionIndex = -1
        isAnswerRevealed = false
        
        // UI güncelle
        binding.tvQuestionNumber.text = "Soru ${index + 1}/${questions.size}"
        binding.tvQuestion.text = question.question
        
        // Seçenekleri güncelle
        question.options.forEachIndexed { optIndex, option ->
            if (optIndex < optionTexts.size) {
                optionTexts[optIndex].text = option
            }
        }
        
        // Seçenek stillerini sıfırla
        resetOptionStyles()
        
        // İlerleme çubuğu güncelle
        updateProgress()
        
        // Buton durumu
        binding.btnNext.isEnabled = false
        binding.btnNext.text = "Cevabı Onayla"
    }
    
    private fun selectOption(index: Int) {
        if (isAnswerRevealed) return
        
        selectedOptionIndex = index
        
        // Tüm seçenekleri sıfırla
        optionCards.forEachIndexed { i, card ->
            val cardView = card as com.google.android.material.card.MaterialCardView
            if (i == index) {
                cardView.strokeColor = getColor(R.color.primary_color)
                cardView.strokeWidth = 4
                optionLetters[i].setBackgroundResource(R.drawable.option_selected)
                optionLetters[i].setTextColor(getColor(R.color.white))
            } else {
                cardView.strokeColor = getColor(android.R.color.transparent)
                cardView.strokeWidth = 0
                optionLetters[i].setBackgroundResource(R.drawable.option_circle)
                optionLetters[i].setTextColor(getColor(R.color.primary_color))
            }
        }
        
        binding.btnNext.isEnabled = true
    }
    
    private fun revealAnswer() {
        if (selectedOptionIndex == -1) return
        
        isAnswerRevealed = true
        val question = questions[currentQuestionIndex]
        val isCorrect = selectedOptionIndex == question.correctIndex
        
        if (isCorrect) {
            correctAnswers++
        }
        
        // Seçili cevabı işaretle
        optionCards.forEachIndexed { index, card ->
            val cardView = card as com.google.android.material.card.MaterialCardView
            when {
                index == question.correctIndex -> {
                    // Doğru cevap - yeşil
                    cardView.setCardBackgroundColor(getColor(R.color.skill_known_bg))
                    cardView.strokeColor = getColor(R.color.skill_known_bg)
                    optionLetters[index].setBackgroundResource(R.drawable.option_correct)
                    optionLetters[index].setTextColor(getColor(R.color.white))
                }
                index == selectedOptionIndex && !isCorrect -> {
                    // Yanlış seçim - kırmızı
                    cardView.setCardBackgroundColor(getColor(R.color.skill_wanted_bg))
                    cardView.strokeColor = getColor(R.color.skill_wanted_bg)
                    optionLetters[index].setBackgroundResource(R.drawable.option_wrong)
                    optionLetters[index].setTextColor(getColor(R.color.white))
                }
            }
        }
        
        // İlerleme çubuğunu güncelle
        val stepDrawable = if (isCorrect) R.drawable.progress_step_correct else R.drawable.progress_step_wrong
        progressSteps[currentQuestionIndex].setBackgroundResource(stepDrawable)
        
        // Buton güncelle
        binding.btnNext.text = if (currentQuestionIndex < questions.size - 1) "Sonraki Soru" else "Sonucu Gör"
    }
    
    private fun nextQuestion() {
        if (currentQuestionIndex < questions.size - 1) {
            showQuestion(currentQuestionIndex + 1)
        } else {
            finishQuiz()
        }
    }
    
    private fun resetOptionStyles() {
        optionCards.forEachIndexed { index, card ->
            val cardView = card as com.google.android.material.card.MaterialCardView
            cardView.setCardBackgroundColor(getColor(R.color.white))
            cardView.strokeColor = getColor(android.R.color.transparent)
            cardView.strokeWidth = 0
            optionLetters[index].setBackgroundResource(R.drawable.option_circle)
            optionLetters[index].setTextColor(getColor(R.color.primary_color))
        }
    }
    
    private fun updateProgress() {
        progressSteps.forEachIndexed { index, step ->
            when {
                index < currentQuestionIndex -> {
                    // Zaten cevaplanmış - durumu koru
                }
                index == currentQuestionIndex -> {
                    step.setBackgroundResource(R.drawable.progress_step_active)
                }
                else -> {
                    step.setBackgroundResource(R.drawable.progress_step_inactive)
                }
            }
        }
    }
    
    private fun finishQuiz() {
        timer?.cancel()
        
        val passed = SkillQuizData.isQuizPassed(correctAnswers)
        
        // Firestore'a kaydet
        if (passed) {
            saveVerifiedSkill()
        }
        
        // Sonuç ekranına git
        val intent = Intent(this, QuizResultActivity::class.java).apply {
            putExtra(QuizResultActivity.EXTRA_SKILL_NAME, skillName)
            putExtra(QuizResultActivity.EXTRA_CORRECT_ANSWERS, correctAnswers)
            putExtra(QuizResultActivity.EXTRA_TOTAL_QUESTIONS, questions.size)
            putExtra(QuizResultActivity.EXTRA_PASSED, passed)
        }
        startActivity(intent)
        finish()
    }
    
    private fun saveVerifiedSkill() {
        val authRepository = AuthRepository()
        val currentUser = authRepository.getCurrentUser() ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .update("verifiedSkills", FieldValue.arrayUnion(skillName))
                    .await()
                
                // Local user'ı da güncelle
                val userPreferences = UserPreferences(this@QuizActivity)
                val user = userPreferences.getUser()
                user?.let {
                    val updatedSkills = it.verifiedSkills.toMutableList()
                    if (!updatedSkills.contains(skillName)) {
                        updatedSkills.add(skillName)
                    }
                    userPreferences.saveUser(it.copy(verifiedSkills = updatedSkills))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@QuizActivity, "Yetenek kaydedilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showExitConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Quiz'den Çık")
            .setMessage("Quiz'den çıkmak istediğinize emin misiniz? İlerlemeniz kaydedilmeyecek.")
            .setPositiveButton("Çık") { _, _ ->
                timer?.cancel()
                finish()
            }
            .setNegativeButton("İptal", null)
            .show()
    }
    
    override fun onBackPressed() {
        showExitConfirmDialog()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}







