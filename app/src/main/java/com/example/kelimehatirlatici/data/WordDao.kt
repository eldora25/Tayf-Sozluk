package com.example.kelimehatirlatici.data

import androidx.room.*
import com.example.kelimehatirlatici.data.LibraryStatRow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    @Update
    suspend fun updateWord(word: Word)

    @Query("SELECT * FROM words WHERE library = :library AND level = :level ORDER BY id DESC")
    suspend fun getWordsByLibraryAndLevel(library: String, level: String): List<Word>

    @Query("SELECT * FROM words WHERE library = :library AND level = :level ORDER BY isLearned ASC, repeatCount ASC, lastReviewedAt ASC LIMIT 1")
    suspend fun getNextWord(library: String, level: String): Word?

    @Query("SELECT DISTINCT library FROM words ORDER BY library ASC")
    suspend fun getLibraries(): List<String>

    @Query("SELECT * FROM words WHERE wrongCount > 0 ORDER BY wrongCount DESC")
    suspend fun getWrongWords(): List<Word>

    @Query("SELECT * FROM words WHERE id != :correctId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getWrongOptions(correctId: Int, limit: Int): List<Word>

    // ──────── KÜTÜPHANE ────────
    @Query("SELECT COUNT(*) FROM words WHERE library = :library")
    suspend fun getTotalCount(library: String): Int

    @Query("SELECT COUNT(*) FROM words WHERE library = :library AND isLearned = 1")
    suspend fun getLearnedCount(library: String): Int

    @Query("DELETE FROM words WHERE library = :library")
    suspend fun deleteWordsByLibrary(library: String)

    @Query("UPDATE words SET library = :newName WHERE library = :oldName")
    suspend fun updateLibraryName(oldName: String, newName: String)

    // ──────── QUIZ ────────
    @Query("SELECT * FROM words WHERE library = :library AND isLearned = 0 ORDER BY word ASC")
    suspend fun getUnlearnedWordsAlphabetical(library: String): List<Word>

    @Query("SELECT * FROM words WHERE library = :library AND isLearned = 0 ORDER BY RANDOM()")
    suspend fun getUnlearnedWordsRandom(library: String): List<Word>

    // ──────── GOAL ────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyGoal)

    @Update
    suspend fun updateGoal(goal: DailyGoal)

    @Query("SELECT * FROM daily_goals WHERE date = :date LIMIT 1")
    suspend fun getGoalByDate(date: String): DailyGoal?

    // ──────── STATS ────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: StudyStats)

    @Update
    suspend fun updateStats(stats: StudyStats)

    @Query("SELECT * FROM study_stats ORDER BY date DESC")
    suspend fun getStats(): List<StudyStats>

    @Query("SELECT * FROM study_stats WHERE date = :date LIMIT 1")
    suspend fun getStatsByDate(date: String): StudyStats?

    // ──────── KÜTÜPHANE İSTATİSTİK SATIRI ────────
    @Query("""
        SELECT library AS name,
               COUNT(*) AS totalCount,
               SUM(CASE WHEN isLearned = 1 THEN 1 ELSE 0 END) AS learnedCount,
               SUM(CASE WHEN isLearned = 0 THEN 1 ELSE 0 END) AS notLearnedCount
        FROM words
        GROUP BY library
        ORDER BY library ASC
    """)
    suspend fun getLibraryStatRows(): List<LibraryStatRow>
}
