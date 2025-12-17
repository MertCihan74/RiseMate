package com.parthenios.risemate.ui.quiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.parthenios.risemate.data.SkillQuizData
import com.parthenios.risemate.databinding.ActivityQuizResultBinding

class QuizResultActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQuizResultBinding
    
    companion object {
        const val EXTRA_SKILL_NAME = "skill_name"
        const val EXTRA_CORRECT_ANSWERS = "correct_answers"
        const val EXTRA_TOTAL_QUESTIONS = "total_questions"
        const val EXTRA_PASSED = "passed"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val skillName = intent.getStringExtra(EXTRA_SKILL_NAME) ?: "Yetenek"
        val correctAnswers = intent.getIntExtra(EXTRA_CORRECT_ANSWERS, 0)
        val totalQuestions = intent.getIntExtra(EXTRA_TOTAL_QUESTIONS, SkillQuizData.QUESTIONS_PER_QUIZ)
        val passed = intent.getBooleanExtra(EXTRA_PASSED, false)
        
        setupUI(skillName, correctAnswers, totalQuestions, passed)
        setupButtons(skillName)
    }
    
    private fun setupUI(skillName: String, correctAnswers: Int, totalQuestions: Int, passed: Boolean) {
        val scorePercent = (correctAnswers * 100) / totalQuestions
        
        binding.apply {
            if (passed) {
                tvEmoji.text = "🎉"
                tvResultTitle.text = "TEBRİKLER!"
                tvSkillName.text = "$skillName yeteneğin doğrulandı!"
                btnRetry.visibility = View.GONE
            } else {
                tvEmoji.text = "😔"
                tvResultTitle.text = "BAŞARISIZ"
                tvSkillName.text = "$skillName yeteneğin doğrulanamadı"
                btnRetry.visibility = View.VISIBLE
            }
            
            tvScorePercent.text = "$scorePercent%"
            tvScoreDetails.text = "$correctAnswers/$totalQuestions Doğru Cevap"
            tvPassInfo.text = "Geçme kriteri: ${SkillQuizData.PASS_THRESHOLD}/${SkillQuizData.QUESTIONS_PER_QUIZ}"
            
            tvCorrectCount.text = correctAnswers.toString()
            tvWrongCount.text = (totalQuestions - correctAnswers).toString()
        }
    }
    
    private fun setupButtons(skillName: String) {
        binding.btnClose.setOnClickListener {
            finish()
        }
        
        binding.btnRetry.setOnClickListener {
            // Quiz'i yeniden başlat
            val intent = Intent(this, QuizActivity::class.java).apply {
                putExtra(QuizActivity.EXTRA_SKILL_NAME, skillName)
            }
            startActivity(intent)
            finish()
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}







