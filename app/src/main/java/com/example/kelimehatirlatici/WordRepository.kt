package com.example.kelimehatirlatici

import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.StudyStats
import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.data.WordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WordRepository(private val wordDao: WordDao) {

    // Temel CRUD
    suspend fun addWord(word: Word) = withContext(Dispatchers.IO) {
        wordDao.insertWord(word)
    }

    suspend fun deleteWord(word: Word) = withContext(Dispatchers.IO) {
        wordDao.deleteWord(word)
    }

    suspend fun updateWordDetails(
        id: Long,
        newWord: String,
        newMeaning: String,
        newExample: String?
    ) = withContext(Dispatchers.IO) {
        wordDao.updateWordDetails(id, newWord, newMeaning, newExample)
    }

    // ===== YENİ: Kelimeyi tamamen güncelle (word, meaning, example, level, library) =====
    suspend fun updateWordFull(
        id: Long,
        newWord: String,
        newMeaning: String,
        newExample: String?,
        newLevel: String,
        newLibrary: String
    ) = withContext(Dispatchers.IO) {
        wordDao.updateWordFull(id, newWord, newMeaning, newExample, newLevel, newLibrary)
    }

    // ===== YENİ: Kelimeyi başka bir kütüphaneye KOPYALA =====
    suspend fun copyWordToLibrary(word: Word, targetLibrary: String): Boolean = withContext(Dispatchers.IO) {
        // Önce hedef kütüphanede aynı kelime var mı kontrol et
        val existing = wordDao.isWordDuplicateInLibrary(targetLibrary, word.word)
        if (existing) {
            false // zaten var, kopyalama
        } else {
            val newWord = word.copy(
                id = 0, // yeni kayıt olarak eklenecek
                library = targetLibrary,
                isLearned = false,
                quizCorrectCount = 0,
                wrongCount = 0
            )
            wordDao.insertWord(newWord)
            true
        }
    }

    // ===== YENİ: Kelimeyi başka bir kütüphaneye TAŞI =====
    suspend fun moveWordToLibrary(word: Word, targetLibrary: String) = withContext(Dispatchers.IO) {
        wordDao.updateWordLibrary(word.id, targetLibrary)
    }

    // ===== YENİ: Kelime ID'sine göre getir (düzenleme dialogu için) =====
    suspend fun getWordById(id: Long): Word? = withContext(Dispatchers.IO) {
        wordDao.getWordById(id)
    }

    // Kelime sorgulama
    suspend fun getWordsByLibraryAndLevel(library: String, level: String): List<Word> = withContext(Dispatchers.IO) {
        wordDao.getWordsByLibraryAndLevel(library, level)
    }

    suspend fun getNextWord(library: String, level: String): Word? = withContext(Dispatchers.IO) {
        wordDao.getNextWordByLibraryAndLevel(library, level)
    }

    suspend fun getUnlearnedWords(library: String, randomOrder: Boolean): List<Word> = withContext(Dispatchers.IO) {
        wordDao.getUnlearnedWordsByLibrary(library, randomOrder)
    }

    suspend fun getWrongWords(): List<Word> = withContext(Dispatchers.IO) {
        wordDao.getWrongWords()
    }

    suspend fun isWordDuplicate(library: String, word: String): Boolean = withContext(Dispatchers.IO) {
        wordDao.isWordDuplicateInLibrary(library, word)
    }

    // Kütüphane işlemleri
    suspend fun getLibraries(): List<String> = withContext(Dispatchers.IO) {
        wordDao.getDistinctLibraries()
    }

    data class LibraryInfo(
        val library: String,
        val totalCount: Int,
        val learnedCount: Int,
        val unlearnedCount: Int
    )

    suspend fun getLibraryInfoList(): List<LibraryInfo> = withContext(Dispatchers.IO) {
        val libraries = wordDao.getDistinctLibraries()
        libraries.map { lib ->
            val words = wordDao.getWordsByLibrary(lib)
            LibraryInfo(
                library = lib,
                totalCount = words.size,
                learnedCount = words.count { it.isLearned },
                unlearnedCount = words.count { !it.isLearned }
            )
        }
    }

    suspend fun getTotalCount(library: String): Int = withContext(Dispatchers.IO) {
        wordDao.getTotalWordCountByLibrary(library)
    }

    suspend fun getTotalLibraryCount(): Int = withContext(Dispatchers.IO) {
        wordDao.getDistinctLibraries().size
    }

    suspend fun getTotalWordCount(): Int = withContext(Dispatchers.IO) {
        wordDao.getTotalWordCount()
    }

    suspend fun getTotalLearnedCount(): Int = withContext(Dispatchers.IO) {
        wordDao.getTotalLearnedCount()
    }

    // Kütüphane yönetimi
    suspend fun deleteLibrary(library: String) = withContext(Dispatchers.IO) {
        wordDao.deleteByLibrary(library)
    }

    suspend fun renameLibrary(oldName: String, newName: String) = withContext(Dispatchers.IO) {
        wordDao.updateLibraryName(oldName, newName)
    }

    // Öğrenme işlemleri
    suspend fun markKnown(word: Word) = withContext(Dispatchers.IO) {
        wordDao.markAsLearned(word.id, true)
    }

    suspend fun markWrong(word: Word) = withContext(Dispatchers.IO) {
        wordDao.markWrong(word)
    }

    suspend fun markAsLearned(word: Word) = withContext(Dispatchers.IO) {
        wordDao.markAsLearned(word.id, true)
    }

    suspend fun incrementQuizCorrect(word: Word) = withContext(Dispatchers.IO) {
        wordDao.incrementQuizCorrect(word.id)
    }

    suspend fun recordQuizWrong(word: Word) = withContext(Dispatchers.IO) {
        wordDao.incrementWrongCount(word.id)
    }

    // Günlük hedef
    suspend fun getTodayGoal(): DailyGoal? = withContext(Dispatchers.IO) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        wordDao.getDailyGoal(today)
    }

    suspend fun setTodayGoal(count: Int) = withContext(Dispatchers.IO) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        wordDao.insertOrUpdateDailyGoal(today, count)
    }

    suspend fun increaseTodayCompleted() = withContext(Dispatchers.IO) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        wordDao.incrementCompletedCount(today)
    }

    // Quiz istatistikleri
    suspend fun recordQuizStat(isCorrect: Boolean) = withContext(Dispatchers.IO) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existing = wordDao.getStudyStatsByDate(today)
        if (existing != null) {
            if (isCorrect) wordDao.incrementCorrectStats(today)
            else wordDao.incrementWrongStats(today)
        } else {
            wordDao.insertStudyStats(StudyStats(date = today, correctCount = if (isCorrect) 1 else 0, wrongCount = if (isCorrect) 0 else 1, learnedCount = 0))
        }
    }

    // Dışa aktarma
    suspend fun exportLibraryAsCsv(library: String): String = withContext(Dispatchers.IO) {
        val words = wordDao.getWordsByLibrary(library)
        val header = "word,meaning,example,level,isLearned"
        val rows = words.joinToString("\n") { word ->
            "${word.word},${word.meaning},${word.example ?: ""},${word.level},${word.isLearned}"
        }
        "$header\n$rows"
    }
}
