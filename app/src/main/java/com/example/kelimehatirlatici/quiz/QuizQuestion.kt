package com.example.kelimehatirlatici.quiz

import com.example.kelimehatirlatici.data.Word

data class QuizQuestion(
    val word: Word,
    val options: List<String>,
    val correctAnswer: String
)
