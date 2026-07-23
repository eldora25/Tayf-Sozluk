package com.example.kelimehatirlatici.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_stats")
data class StudyStats(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val totalWordsStudied: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val studyDate: String = ""
)
