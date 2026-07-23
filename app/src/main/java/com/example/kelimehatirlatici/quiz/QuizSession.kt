package com.example.kelimehatirlatici.quiz

class QuizSession {
    var questions: MutableList<Question> = mutableListOf()
    var currentIndex: Int = 0
    var correctCount: Int = 0
    var wrongCount: Int = 0
}
