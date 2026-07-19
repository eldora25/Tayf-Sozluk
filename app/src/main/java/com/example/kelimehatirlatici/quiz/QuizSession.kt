package com.example.kelimehatirlatici.quiz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.kelimehatirlatici.quiz.Question
class QuizSession {
    var questions: List<Question> = emptyList()
    var currentQuestionIndex by mutableIntStateOf(0)
    var correctCount by mutableIntStateOf(0)
    var wrongCount by mutableIntStateOf(0)
    var isFinished by mutableStateOf(false)
    var isRunning by mutableStateOf(false)
    var elapsedTime by mutableLongStateOf(0L)

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

    fun answerCorrect() {
        correctCount++
    }

    fun answerWrong() {
        wrongCount++
    }
}
