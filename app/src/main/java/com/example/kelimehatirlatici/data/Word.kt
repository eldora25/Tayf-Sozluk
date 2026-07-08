package com.example.kelimehatirlatici.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val word: String,
    val meaning: String,
    val example: String = "",
    val library: String = "Genel",
    val level: String = "Genel",
    val isLearned: Boolean = false,
    val repeatCount: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewedAt: Long = 0L
)
