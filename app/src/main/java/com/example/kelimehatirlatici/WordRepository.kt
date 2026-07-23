package com.example.kelimehatirlatici

import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.data.WordDao

class WordRepository(private val wordDao: WordDao) {

    fun getAllWords(): List<Word> {
        return wordDao.getAllWords()
    }

    fun getWordsByLibraryAndLevel(library: String, level: String): List<Word> {
        return wordDao.getWordsByLibraryAndLevel(library, level)
    }

    fun addWord(word: Word) {
        wordDao.addWord(word)
    }

    fun getAllLibraries(): List<String> {
        return wordDao.getAllLibraries()
    }

    fun getAllLevels(): List<String> {
        return wordDao.getAllLevels()
    }

    fun getWordById(wordId: Int): Word? {
        return wordDao.getWordById(wordId)
    }

    fun updateIncorrectCount(wordId: Int) {
        val word = wordDao.getWordById(wordId)
        if (word != null) {
            wordDao.updateIncorrectCount(wordId, word.wrongCount + 1)
        }
    }

    fun resetIncorrectCount(wordId: Int) {
        wordDao.updateIncorrectCount(wordId, 0)
    }

    fun updateQuizCorrectCount(wordId: Int) {
        val word = wordDao.getWordById(wordId)
        if (word != null) {
            wordDao.updateQuizCorrectCount(wordId, word.quizCorrectCount + 1)
        }
    }

    fun updateQuizWrongCount(wordId: Int) {
        val word = wordDao.getWordById(wordId)
        if (word != null) {
            wordDao.updateQuizWrongCount(wordId, word.quizWrongCount + 1)
        }
    }

    fun updateWord(id: Int, word: String, meaning: String, example: String, level: String) {
        wordDao.updateWord(id, word, meaning, example, level)
    }

    fun deleteWord(id: Int) {
        wordDao.deleteWord(id)
    }
}
