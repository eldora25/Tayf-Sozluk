package com.example.kelimehatirlatici.quiz

import com.example.kelimehatirlatici.WordRepository
import com.example.kelimehatirlatici.data.Word

class QuizGenerator(private val repository: WordRepository) {

    fun generateQuestions(words: List<Word>): List<Question> {
        val questions = mutableListOf<Question>()
        val wordPool = words.toMutableList()

        for (word in words) {
            val wrongOptions = wordPool
                .filter { it.id != word.id }
                .shuffled()
                .take(3)
                .map { it.meaning }

            val options = (wrongOptions + word.meaning).shuffled()
            questions.add(Question(word = word, options = options, correctAnswer = word.meaning))
        }

        return questions.shuffled()
    }
}
