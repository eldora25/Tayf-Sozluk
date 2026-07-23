package com.example.kelimehatirlatici.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val meaning: String,              // İlk anlam (eski uyumluluk)
    val meanings: String = "[]",       // Çoklu anlam: JSON array ["anlam1","anlam2"]
    val example: String = "",          // İlk örnek cümle (eski uyumluluk)
    val examples: String = "[]",       // Çoklu örnek: JSON array ["örnek1","örnek2"]
    val library: String = "Genel",
    val level: String = "Genel",
    val wrongCount: Int = 0,
    val quizCorrectCount: Int = 0,
    val quizWrongCount: Int = 0
)
