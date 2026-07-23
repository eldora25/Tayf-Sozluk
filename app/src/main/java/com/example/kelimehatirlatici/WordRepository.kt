package com.example.kelimehatirlatici

import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.data.WordDao

class WordRepository(private val wordDao: WordDao) {

    suspend fun addWord(word: Word) {
        wordDao.insert(word)
    }

    suspend fun deleteWord(word: Word) {
        wordDao.delete(word)
    }

    suspend fun updateWord(word: Word) {
        wordDao.update(word)
    }

    suspend fun getWordsByLibraryAndLevel(library: String, level: String): List<Word> {
        return if (library.isBlank() && level.isBlank()) {
            wordDao.getAllWords()
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

    fun getWordCountByLibrary(library: String): Int {
        return wordDao.getWordCountByLibrary(library)
    }

    fun getWordCountByLibraryAndLevel(library: String, level: String): Int {
        return wordDao.getWordCountByLibraryAndLevel(library, level)
    }
}
