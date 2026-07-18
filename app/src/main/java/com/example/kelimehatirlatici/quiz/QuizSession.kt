package com.example.kelimehatirlatici.quiz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class QuizSession {
    var questions by mutableStateOf<List<QuizQuestion>>(emptyList())
    var currentIndex by mutableIntStateOf(0)
    var correctCount by mutableIntStateOf(0)
    var wrongCount by mutableIntStateOf(0)
    var elapsedSeconds by mutableLongStateOf(0L)
    var isRunning by mutableStateOf(false)
    var isFinished by mutableStateOf(false)
    var totalQuestions by mutableIntStateOf(0)

    fun reset() {
        questions = emptyList()
        currentIndex = 0
        correctCount = 0
        wrongCount = 0
        elapsedSeconds = 0L
        isRunning = false
        isFinished = false
        totalQuestions = 0
    }

    fun start(questionList: List<QuizQuestion>, count: Int) {
        reset()
        questions = questionList.take(count)
        totalQuestions = questions.size
        if (questions.isNotEmpty()) {
            isRunning = true
        }
    }

    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)

    val currentQuestionNumber: Int
        get() = (currentIndex + 1).coerceAtMost(totalQuestions)

    val formattedTime: String
        get() {
            val h = elapsedSeconds / 3600
            val m = (elapsedSeconds % 3600) / 60
            val s = elapsedSeconds % 60
            return String.format("%02d:%02d:%02d", h, m, s)
        }
}
