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

    suspend fun isWordDuplicate(library: String, word: String): Boolean {
        return wordDao.countByLibraryAndWord(library, word) > 0
    }

    suspend fun getWordsByLibraryAndLevel(library: String, level: String) =
        wordDao.getWordsByLibraryAndLevel(library, level)

    suspend fun getNextWord(library: String, level: String) =
        wordDao.getNextWord(library, level)

    suspend fun getLibraries() = wordDao.getLibraries()

    suspend fun getTotalCount(library: String) = wordDao.getTotalCount(library)

    suspend fun getLibraryInfoList(): List<LibraryInfo> {
        return wordDao.getLibraries().map { lib ->
            LibraryInfo(
                name = lib,
                totalCount = wordDao.getTotalCount(lib),
                learnedCount = wordDao.getLearnedCount(lib),
                notLearnedCount = wordDao.getTotalCount(lib) - wordDao.getLearnedCount(lib)
            )
        }
    }

    suspend fun deleteLibrary(library: String) = wordDao.deleteWordsByLibrary(library)

    suspend fun renameLibrary(oldName: String, newName: String) =
        wordDao.updateLibraryName(oldName, newName)

    suspend fun markKnown(word: Word) {
        wordDao.updateWord(word.copy(isLearned = true, repeatCount = word.repeatCount + 1, lastReviewedAt = System.currentTimeMillis()))
        increaseTodayLearned()
    }

    suspend fun markWrong(word: Word) {
        wordDao.updateWord(word.copy(wrongCount = word.wrongCount + 1, repeatCount = word.repeatCount + 1, lastReviewedAt = System.currentTimeMillis()))
    }

    suspend fun getWrongWords() = wordDao.getWrongWords()

    suspend fun getWrongOptions(correctId: Int, limit: Int) =
        wordDao.getWrongOptions(correctId, limit)

    suspend fun getUnlearnedWords(library: String, randomOrder: Boolean): List<Word> {
        return if (randomOrder) wordDao.getUnlearnedWordsRandom(library)
        else wordDao.getUnlearnedWordsAlphabetical(library)
    }

    suspend fun incrementQuizCorrect(word: Word) {
        wordDao.updateWord(word.copy(quizCorrectCount = word.quizCorrectCount + 1, lastReviewedAt = System.currentTimeMillis()))
    }

    suspend fun markAsLearned(word: Word) {
        wordDao.updateWord(word.copy(isLearned = true))
        val learnedLib = "✅ Öğrenilmiş"
        if (!isWordDuplicate(learnedLib, word.word)) {
            wordDao.insertWord(
                Word(
                    word = word.word,
                    meaning = word.meaning,
                    example = word.example,
                    library = learnedLib,
                    level = word.level,
                    isLearned = true
                )
            )
        }
    }

    suspend fun recordQuizWrong(word: Word) {
        wordDao.updateWord(word.copy(wrongCount = word.wrongCount + 1, lastReviewedAt = System.currentTimeMillis()))
    }

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

    suspend fun getTotalLibraryCount(): Int = getLibraries().size

    suspend fun getTotalWordCount(): Int = getLibraryInfoList().sumOf { it.totalCount }

    suspend fun getTotalLearnedCount(): Int = getLibraryInfoList().sumOf { it.learnedCount }

    suspend fun exportLibraryAsCsv(library: String): String {
        val words = wordDao.getWordsByLibrary(library)
        val sb = StringBuilder()
        sb.appendLine("Word,Meaning,Example,Level")
        words.forEach { w ->
            val escaped = listOf(w.word, w.meaning, w.example, w.level).joinToString(",") {
                "\"${it.replace("\"", "\"\"")}\""
            }
            sb.appendLine(escaped)
        }
        return sb.toString()
    }

    suspend fun updateWordDetails(id: Int, newWord: String, newMeaning: String, newExample: String) {
        val existing = wordDao.getWordById(id) ?: return
        wordDao.updateWord(existing.copy(word = newWord, meaning = newMeaning, example = newExample))
    }

    private suspend fun increaseTodayLearned() {
        val today = today()
        val current = wordDao.getStatsByDate(today)
        if (current == null) wordDao.insertStats(StudyStats(date = today, learnedCount = 1, quizCorrect = 0, quizWrong = 0, studyTimeMinute = 0))
        else wordDao.updateStats(current.copy(learnedCount = current.learnedCount + 1))
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // ═══════════════════════════════════════════════════════════════════════
    // YENİ: KELİME DÜZENLEME METODLARI
    // ═══════════════════════════════════════════════════════════════════════

    suspend fun getWordById(id: Int): Word? = wordDao.getWordById(id)

    suspend fun updateWordFull(
        id: Int,
        newWord: String,
        newMeaning: String,
        newExample: String,
        newLevel: String,
        newLibrary: String
    ) {
        wordDao.updateWordFull(id, newWord, newMeaning, newExample, newLevel, newLibrary)
    }

    suspend fun copyWordToLibrary(word: Word, targetLibrary: String): Boolean {
        if (wordDao.countByLibraryAndWord(targetLibrary, word.word) > 0) {
            return false
        }
        val newWord = word.copy(
            id = 0,
            library = targetLibrary,
            isLearned = false,
            quizCorrectCount = 0,
            wrongCount = 0,
            repeatCount = 0,
            lastReviewedAt = 0L
        )
        wordDao.insertWord(newWord)
        return true
    }

    suspend fun moveWordToLibrary(word: Word, targetLibrary: String) {
        wordDao.updateWordFull(
            id = word.id,
            newWord = word.word,
            newMeaning = word.meaning,
            newExample = word.example,
            newLevel = word.level,
            newLibrary = targetLibrary
        )
    }
}
