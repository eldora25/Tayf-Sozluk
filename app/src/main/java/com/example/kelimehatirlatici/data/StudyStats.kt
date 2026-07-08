package com.example.kelimehatirlatici.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_stats")
data class StudyStats(
    @PrimaryKey
    val date: String,
    val learnedCount: Int,
    val quizCorrect: Int,
    val quizWrong: Int,
    val studyTimeMinute: Int
)
