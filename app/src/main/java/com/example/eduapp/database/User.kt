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
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
