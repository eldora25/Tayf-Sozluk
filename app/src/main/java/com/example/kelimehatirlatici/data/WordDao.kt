package com.example.kelimehatirlatici.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    @Update
    suspend fun updateWord(word: Word)

    @Query("""
        SELECT * FROM words
        WHERE library = :library AND level = :level
        ORDER BY id DESC
    """)
    suspend fun getWordsByLibraryAndLevel(
        library: String,
        level: String
    ): List<Word>

    @Query("""
        SELECT * FROM words
        WHERE library = :library AND level = :level
        ORDER BY isLearned ASC, repeatCount ASC, lastReviewedAt ASC
        LIMIT 1
    """)
    suspend fun getNextWord(
        library: String,
        level: String
    ): Word?

    @Query("""
        SELECT DISTINCT library FROM words
        ORDER BY library ASC
    """)
    suspend fun getLibraries(): List<String>

    @Query("""
        SELECT * FROM words
        WHERE wrongCount > 0
        ORDER BY wrongCount DESC
    """)
    suspend fun getWrongWords(): List<Word>

    @Query("""
        SELECT * FROM words
        WHERE id != :correctId
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getWrongOptions(
        correctId: Int,
        limit: Int
    ): List<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyGoal)

    @Update
    suspend fun updateGoal(goal: DailyGoal)

    @Query("""
        SELECT * FROM daily_goals
        WHERE date = :date
        LIMIT 1
    """)
    suspend fun getGoalByDate(date: String): DailyGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: StudyStats)

    @Update
    suspend fun updateStats(stats: StudyStats)

    @Query("""
        SELECT * FROM study_stats
        ORDER BY date DESC
    """)
    suspend fun getStats(): List<StudyStats>

    @Query("""
        SELECT * FROM study_stats
        WHERE date = :date
        LIMIT 1
    """)
    suspend fun getStatsByDate(date: String): StudyStats?
}
