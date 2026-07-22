package com.example.kelimehatirlatici.quiz

import com.example.kelimehatirlatici.WordRepository
import com.example.kelimehatirlatici.data.Word

// DİKKAT: QuizQuestion burada tanımlı DEĞİL!
// Ayrı bir QuizQuestion.kt dosyasında tanımlı olduğu için buraya EKLENMEYECEK.

/**
 * Yardımcı: ||| ile ayrılmış çoklu anlamlardan sadece ilk anlamı alır.
 * Quiz'de kullanıcıya sadece ilk anlam gösterilir.
 */
private fun getFirstMeaning(meaning: String): String {
    return meaning.split("|||").firstOrNull()?.trim()?.ifBlank { meaning } ?: meaning
}

class QuizGenerator(private val repository: WordRepository) {

    suspend fun generateQuestions(words: List<Word>): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()

        for (word in words) {
            // ★ Değişiklik: Çoklu anlamdan sadece ilkini al
            val correctMeaning = getFirstMeaning(word.meaning)

            // 3 yanlış şık al (diğer kelimelerden)
            val wrongWords = repository.getWrongOptions(word.id, 3)
            val wrongOptions = wrongWords.map {
                // Yanlış şıklarda da sadece ilk anlamı göster
                getFirstMeaning(it.meaning)
            }

            // Doğru cevabı da ekle
            val allOptions = (wrongOptions + correctMeaning).toMutableList()
            allOptions.shuffle()

            questions.add(
                QuizQuestion(
                    word = word,
                    options = allOptions,
                    correctAnswer = correctMeaning
                )
            )
        }

        return questions
    }
}
