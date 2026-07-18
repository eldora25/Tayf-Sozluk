package com.example.kelimehatirlatici

import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.StudyStats
import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.data.WordDao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LibraryInfo(
    val name: String,
    val totalCount: Int,
    val learnedCount: Int,
    val notLearnedCount: Int
)

class WordRepository(
    private val wordDao: WordDao
) {
    suspend fun addWord(word: Word) {
        wordDao.insertWord(word)
    }

    suspend fun addWords(words: List<Word>) {
        wordDao.insertWords(words)
    }

    suspend fun getWordsByLibraryAndLevel(
        library: String,
        level: String
    ): List<Word> {
        return wordDao.getWordsByLibraryAndLevel(library, level)
    }

    suspend fun getNextWord(
        library: String,
        level: String
    ): Word? {
        return wordDao.getNextWord(library, level)
    }

    suspend fun getLibraries(): List<String> {
        return wordDao.getLibraries()
    }

    // ──────── YENİ ────────

    suspend fun getTotalCount(library: String): Int {
        return wordDao.getTotalCount(library)
    }

    suspend fun getLibraryInfoList(): List<LibraryInfo> {
        val libraries = wordDao.getLibraries()
        return libraries.map { lib ->
            LibraryInfo(
                name = lib,
                totalCount = wordDao.getTotalCount(lib),
                learnedCount = wordDao.getLearnedCount(lib),
                notLearnedCount = wordDao.getTotalCount(lib) - wordDao.getLearnedCount(lib)
            )
        }
    }

    suspend fun deleteLibrary(library: String) {
        wordDao.deleteWordsByLibrary(library)
    }

    suspend fun renameLibrary(oldName: String, newName: String) {
        wordDao.updateLibraryName(oldName, newName)
    }

    // ──────── MEVCUT ────────

    suspend fun markKnown(word: Word) {
        wordDao.updateWord(
            word.copy(
                isLearned = true,
                repeatCount = word.repeatCount + 1,
                lastReviewedAt = System.currentTimeMillis()
            )
        )
        increaseTodayLearned()
    }

    suspend fun markWrong(word: Word) {
        wordDao.updateWord(
            word.copy(
                wrongCount = word.wrongCount + 1,
                repeatCount = word.repeatCount + 1,
                lastReviewedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getWrongWords(): List<Word> {
        return wordDao.getWrongWords()
    }

    suspend fun getWrongOptions(
        correctId: Int,
        limit: Int
    ): List<Word> {
        return wordDao.getWrongOptions(correctId, limit)
    }

    suspend fun getTodayGoal(): DailyGoal? {
        return wordDao.getGoalByDate(today())
    }

    suspend fun setTodayGoal(target: Int) {
        val current = getTodayGoal()
        if (current == null) {
            wordDao.insertGoal(
                DailyGoal(
                    date = today(),
                    targetCount = target,
                    completedCount = 0
                )
            )
        } else {
            wordDao.updateGoal(
                current.copy(targetCount = target)
            )
        }
    }

    suspend fun increaseTodayCompleted() {
        val current = getTodayGoal()
        if (current == null) {
            wordDao.insertGoal(
                DailyGoal(date = today(), targetCount = 10, completedCount = 1)
            )
        } else {
            wordDao.updateGoal(
                current.copy(completedCount = current.completedCount + 1)
            )
        }
    }

    suspend fun getStats(): List<StudyStats> {
        return wordDao.getStats()
    }

    suspend fun recordQuizResult(word: Word, correct: Boolean) {
        val today = today()
        val current = wordDao.getStatsByDate(today)
        if (correct) {
            wordDao.updateWord(
                word.copy(
                    repeatCount = word.repeatCount + 1,
                    lastReviewedAt = System.currentTimeMillis()
                )
            )
        } else {
            markWrong(word)
        }
        if (current == null) {
            wordDao.insertStats(
                StudyStats(
                    date = today,
                    learnedCount = 0,
                    quizCorrect = if (correct) 1 else 0,
                    quizWrong = if (correct) 0 else 1,
                    studyTimeMinute = 0
                )
            )
        } else {
            wordDao.updateStats(
                current.copy(
                    quizCorrect = current.quizCorrect + if (correct) 1 else 0,
                    quizWrong = current.quizWrong + if (correct) 0 else 1
                )
            )
        }
    }

    private suspend fun increaseTodayLearned() {
        val today = today()
        val current = wordDao.getStatsByDate(today)
        if (current == null) {
            wordDao.insertStats(
                StudyStats(date = today, learnedCount = 1, quizCorrect = 0, quizWrong = 0, studyTimeMinute = 0)
            )
        } else {
            wordDao.updateStats(current.copy(learnedCount = current.learnedCount + 1))
        }
    }

    private fun today(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
