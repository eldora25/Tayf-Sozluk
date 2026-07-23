package com.example.kelimehatirlatici.data

import androidx.room.*

@Dao
interface WordDao {

    @Query("SELECT * FROM words")
    fun getAllWords(): List<Word>

    @Query("SELECT * FROM words WHERE library = :library AND level = :level")
    fun getWordsByLibraryAndLevel(library: String, level: String): List<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addWord(word: Word)

    @Query("SELECT DISTINCT library FROM words")
    fun getAllLibraries(): List<String>

    @Query("SELECT DISTINCT level FROM words")
    fun getAllLevels(): List<String>

    @Query("SELECT * FROM words WHERE id = :wordId LIMIT 1")
    fun getWordById(wordId: Int): Word?

    @Query("SELECT COUNT(*) FROM words WHERE library = :library AND level = :level AND wrongCount = 0")
    fun getLearnedCount(library: String, level: String): Int

    @Query("SELECT COUNT(*) FROM words WHERE library = :library AND level = :level AND wrongCount > 0")
    fun getLearningCount(library: String, level: String): Int

    @Query("UPDATE words SET wrongCount = :wrongCount WHERE id = :wordId")
    fun updateIncorrectCount(wordId: Int, wrongCount: Int)

    @Query("UPDATE words SET quizCorrectCount = :quizCorrectCount WHERE id = :wordId")
    fun updateQuizCorrectCount(wordId: Int, quizCorrectCount: Int)

    @Query("UPDATE words SET quizWrongCount = :quizWrongCount WHERE id = :wordId")
    fun updateQuizWrongCount(wordId: Int, quizWrongCount: Int)

    @Query("""
        UPDATE words SET 
            word = :word, 
            meaning = :meaning, 
            meanings = :meanings, 
            example = :example, 
            examples = :examples, 
            level = :level, 
            library = :library 
        WHERE id = :id
    """)
    fun updateWord(
        id: Int,
        word: String,
        meaning: String,
        meanings: String,
        example: String,
        examples: String,
        level: String,
        library: String
    )

    @Query("UPDATE words SET library = :newLibrary WHERE id = :wordId")
    fun moveWord(wordId: Int, newLibrary: String)

    @Query("DELETE FROM words WHERE id = :id")
    fun deleteWord(id: Int)
}
