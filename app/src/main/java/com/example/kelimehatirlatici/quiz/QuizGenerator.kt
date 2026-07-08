package com.example.kelimehatirlatici.quiz

import com.example.kelimehatirlatici.WordRepository
import com.example.kelimehatirlatici.data.Word

class QuizGenerator(
    private val repository: WordRepository
) {
    suspend fun generateQuestion(word: Word): QuizQuestion {
        val wrongOptions = repository
            .getWrongOptions(word.id, 3)
            .map { it.meaning }

        val options = (wrongOptions + word.meaning)
            .distinct()
            .shuffled()

        return QuizQuestion(
            word = word,
            options = options,
            correctAnswer = word.meaning
        )
    }
}
