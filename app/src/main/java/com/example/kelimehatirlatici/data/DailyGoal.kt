package com.example.kelimehatirlatici.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_goals")
data class DailyGoal(
    @PrimaryKey
    val date: String,
    val targetCount: Int,
    val completedCount: Int
)
