package com.example.kelimehatirlatici

import androidx.lifecycle.LiveData
import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.data.WordDao

class WordRepository(private val wordDao: WordDao) {

    val allWords: LiveData<List<Word>> = wordDao.getAllWords()

    suspend fun addWord(word: Word) {
        // Aynı kelimeyi ve anlamı kontrol et
        val existingWords = wordDao.getWordByName(word.word)
        val exists = existingWords.any {
            it.word == word.word && it.meaning == word.meaning &&
            it.library == word.library
        }
        if (!exists) {
            wordDao.insert(word)
        }
    }

    suspend fun deleteWord(word: Word) {
        wordDao.delete(word)
    }

    suspend fun updateWord(word: Word) {
        wordDao.update(word)
    }

    suspend fun getWordsByLibraryAndLevel(library: String, level: String): List<Word> {
        return if (library.isBlank() && level.isBlank()) {
            wordDao.getAllWordsSync()
        } else if (library.isBlank()) {
            wordDao.getWordsByLevel(level)
        } else if (level.isBlank()) {
            wordDao.getWordsByLibrary(library)
        } else {
            wordDao.getWordsByLibraryAndLevel(library, level)
        }
    }

    suspend fun getAllLibraries(): List<String> {
        return wordDao.getAllLibraries()
    }

    suspend fun searchWords(query: String): List<Word> {
        return wordDao.searchWords("%$query%")
    }

    fun getWordCountByLibrary(library: String): Int {
        return wordDao.getWordCountByLibrary(library)
    }

    fun getWordCountByLibraryAndLevel(library: String, level: String): Int {
        return wordDao.getWordCountByLibraryAndLevel(library, level)
    }
}
