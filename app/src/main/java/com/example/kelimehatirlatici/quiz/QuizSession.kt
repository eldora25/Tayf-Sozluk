package com.example.kelimehatirlatici.quiz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class QuizSession {
    var questions: List<Question> = emptyList()
    var currentQuestionIndex by mutableIntStateOf(0)
    var correctCount by mutableIntStateOf(0)
    var wrongCount by mutableIntStateOf(0)
    var isFinished by mutableStateOf(false)
    var isRunning by mutableStateOf(false)
    var elapsedTime by mutableLongStateOf(0L) // milisaniye cinsinden

    // ★ QuizScreen'de kullanılan computed property'ler ★
    val currentIndex: Int get() = currentQuestionIndex
    val totalQuestions: Int get() = questions.size
    val currentQuestionNumber: Int get() = currentQuestionIndex + 1

    val formattedTime: String
        get() {
            val totalSeconds = elapsedTime / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val currentQuestion: Question?
        get() = if (currentQuestionIndex < questions.size) questions[currentQuestionIndex] else null

    fun start(questions: List<Question>, count: Int) {
        this.questions = questions.shuffled().take(count)
        currentQuestionIndex = 0
        correctCount = 0
        wrongCount = 0
        isFinished = false
        isRunning = true
        elapsedTime = 0L
    }

    fun nextQuestion() {
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
        } else {
            isFinished = true
            isRunning = false
        }
    }
}
