package com.example.kelimehatirlatici

import com.example.kelimehatirlatici.data.*
import java.text.SimpleDateFormat
import java.util.*

data class LibraryInfo(
    val name: String,
    val totalCount: Int,
    val learnedCount: Int,
    val notLearnedCount: Int
)

class WordRepository(private val wordDao: WordDao) {

    suspend fun addWord(word: Word) = wordDao.insertWord(word)
    suspend fun addWords(words: List<Word>) = wordDao.insertWords(words)

    suspend fun getWordsByLibraryAndLevel(library: String, level: String) =
        wordDao.getWordsByLibraryAndLevel(library, level)

    suspend fun getNextWord(library: String, level: String) =
        wordDao.getNextWord(library, level)

    suspend fun getLibraries() = wordDao.getLibraries()

    suspend fun getTotalCount(library: String) = wordDao.getTotalCount(library)

    suspend fun getLibraryInfoList(): List<LibraryInfo> {
        return wordDao.getLibraryStatRows().map {
            LibraryInfo(name = it.name, totalCount = it.totalCount, learnedCount = it.learnedCount, notLearnedCount = it.notLearnedCount)
        }
    }

    suspend fun deleteLibrary(library: String) = wordDao.deleteWordsByLibrary(library)
    suspend fun renameLibrary(oldName: String, newName: String) = wordDao.updateLibraryName(oldName, newName)

    suspend fun markKnown(word: Word) {
        wordDao.updateWord(word.copy(isLearned = true, repeatCount = word.repeatCount + 1, lastReviewedAt = System.currentTimeMillis()))
        increaseTodayLearned()
    }

    suspend fun markWrong(word: Word) {
        wordDao.updateWord(word.copy(wrongCount = word.wrongCount + 1, repeatCount = word.repeatCount + 1, lastReviewedAt = System.currentTimeMillis()))
    }

    suspend fun getWrongWords() = wordDao.getWrongWords()
    suspend fun getWrongOptions(correctId: Int, limit: Int) = wordDao.getWrongOptions(correctId, limit)

    // ──────── QUIZ ────────
    suspend fun getUnlearnedWords(library: String, randomOrder: Boolean): List<Word> {
        return if (randomOrder) wordDao.getUnlearnedWordsRandom(library)
        else wordDao.getUnlearnedWordsAlphabetical(library)
    }

    suspend fun incrementQuizCorrect(word: Word) {
        wordDao.updateWord(word.copy(quizCorrectCount = word.quizCorrectCount + 1, lastReviewedAt = System.currentTimeMillis()))
    }

    suspend fun markAsLearned(word: Word) {
        wordDao.updateWord(word.copy(isLearned = true))
    }

    suspend fun recordQuizWrong(word: Word) {
        wordDao.updateWord(word.copy(wrongCount = word.wrongCount + 1, lastReviewedAt = System.currentTimeMillis()))
    }

    // ──────── GOAL ────────
    suspend fun getTodayGoal() = wordDao.getGoalByDate(today())

    suspend fun setTodayGoal(target: Int) {
        val current = getTodayGoal()
        if (current == null) wordDao.insertGoal(DailyGoal(date = today(), targetCount = target, completedCount = 0))
        else wordDao.updateGoal(current.copy(targetCount = target))
    }

    suspend fun increaseTodayCompleted() {
        val current = getTodayGoal()
        if (current == null) wordDao.insertGoal(DailyGoal(date = today(), targetCount = 10, completedCount = 1))
        else wordDao.updateGoal(current.copy(completedCount = current.completedCount + 1))
    }

    // ──────── STATS ────────
    suspend fun getStats() = wordDao.getStats()

    suspend fun recordQuizStat(correct: Boolean) {
        val today = today()
        val current = wordDao.getStatsByDate(today)
        if (current == null) {
            wordDao.insertStats(StudyStats(date = today, learnedCount = 0, quizCorrect = if (correct) 1 else 0, quizWrong = if (correct) 0 else 1, studyTimeMinute = 0))
        } else {
            if (correct) wordDao.updateStats(current.copy(quizCorrect = current.quizCorrect + 1))
            else wordDao.updateStats(current.copy(quizWrong = current.quizWrong + 1))
        }
    }

    // ──────── ÖZET ────────
    suspend fun getTotalLibraryCount(): Int = getLibraries().size
    suspend fun getTotalWordCount(): Int = wordDao.getLibraryStatRows().sumOf { it.totalCount }
    suspend fun getTotalLearnedCount(): Int = wordDao.getLibraryStatRows().sumOf { it.learnedCount }

    private suspend fun increaseTodayLearned() {
        val today = today()
        val current = wordDao.getStatsByDate(today)
        if (current == null) wordDao.insertStats(StudyStats(date = today, learnedCount = 1, quizCorrect = 0, quizWrong = 0, studyTimeMinute = 0))
        else wordDao.updateStats(current.copy(learnedCount = current.learnedCount + 1))
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}
