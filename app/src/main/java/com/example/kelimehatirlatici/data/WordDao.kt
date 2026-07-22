package com.example.kelimehatirlatici.data

import androidx.room.*

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

    @Query("SELECT COUNT(*) FROM words WHERE library = :library")
    suspend fun getTotalCount(library: String): Int

    @Query("SELECT COUNT(*) FROM words WHERE library = :library AND isLearned = 1")
    suspend fun getLearnedCount(library: String): Int

    @Query("DELETE FROM words WHERE library = :library")
    suspend fun deleteWordsByLibrary(library: String)

    @Query("UPDATE words SET library = :newName WHERE library = :oldName")
    suspend fun updateLibraryName(oldName: String, newName: String)

    @Query("SELECT * FROM words WHERE library = :library AND isLearned = 0 ORDER BY word ASC")
    suspend fun getUnlearnedWordsAlphabetical(library: String): List<Word>

    @Query("SELECT * FROM words WHERE library = :library AND isLearned = 0 ORDER BY RANDOM()")
    suspend fun getUnlearnedWordsRandom(library: String): List<Word>

    // ══════════ YARDIMCI SORGULAR ══════════
    @Query("SELECT COUNT(*) FROM words WHERE library = :library AND word = :word")
    suspend fun countByLibraryAndWord(library: String, word: String): Int

    @Query("SELECT * FROM words WHERE library = :library ORDER BY word ASC")
    suspend fun getWordsByLibrary(library: String): List<Word>

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun getWordById(id: Int): Word?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyGoal)

    @Update
    suspend fun updateGoal(goal: DailyGoal)

    @Query("SELECT * FROM daily_goals WHERE date = :date LIMIT 1")
    suspend fun getGoalByDate(date: String): DailyGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: StudyStats)

    @Update
    suspend fun updateStats(stats: StudyStats)

    @Query("SELECT * FROM study_stats ORDER BY date DESC")
    suspend fun getStats(): List<StudyStats>

    @Query("SELECT * FROM study_stats WHERE date = :date LIMIT 1")
    suspend fun getStatsByDate(date: String): StudyStats?

    // ═══════════════════════════════════════════════════════════
    // YENİ: KELİME DÜZENLEME METODLARI
    // ═══════════════════════════════════════════════════════════
    @Query("""
        UPDATE words 
        SET word = :newWord, 
            meaning = :newMeaning, 
            example = :newExample, 
            level = :newLevel, 
            library = :newLibrary 
        WHERE id = :id
    """)
    suspend fun updateWordFull(
        id: Int,
        newWord: String,
        newMeaning: String,
        newExample: String,
        newLevel: String,
        newLibrary: String
    )

    @Query("UPDATE words SET library = :newLibrary WHERE id = :id")
    suspend fun updateWordLibrary(id: Int, newLibrary: String)
}
