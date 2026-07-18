package com.example.kelimehatirlatici.quiz

import com.example.kelimehatirlatici.WordRepository
import com.example.kelimehatirlatici.data.Word

class QuizGenerator(private val repository: WordRepository) {

    suspend fun generateQuestions(
        words: List<Word>
    ): List<QuizQuestion> {
        return words.map { word ->
            val wrongOptions = repository.getWrongOptions(word.id, 3).map { it.meaning }
            val options = (wrongOptions + word.meaning).distinct().shuffled()
            QuizQuestion(word = word, options = options, correctAnswer = word.meaning)
        }
    }
}
