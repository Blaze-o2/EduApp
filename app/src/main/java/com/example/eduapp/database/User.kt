package com.example.eduapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val currentLevel: Int = 1,
    val totalScore: Int = 0,
    val highScore: Int = 0,
    val puzzlesSolved: Int = 0,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    // Saved game state for "Continue" feature
    val savedLevel: Int = 1,
    val savedScore: Int = 0,
    val savedIncorrectAttempts: Int = 0,
    val savedDifficulty: Int = 1
)
