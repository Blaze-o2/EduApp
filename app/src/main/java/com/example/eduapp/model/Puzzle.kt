package com.example.eduapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Puzzle(
    val id: String,
    val equations: List<String>,
    val targetVariable: String,
    val answer: Int,
    val difficulty: Int,
    val characterImages: Map<String, String> = emptyMap() // character name to drawable resource name
)

data class GameState(
    val currentPuzzle: Puzzle? = null,
    val score: Int = 0,
    val level: Int = 1,
    val isGameOver: Boolean = false,
    val message: String = "",
    val showResultDialog: Boolean = false,
    val lastSolvedAnswer: Int? = null,
    val playerName: String? = null,
    val incorrectAttempts: Int = 0,
    val maxAttempts: Int = 5,
    val difficultyLevel: Int = 1, // 1: Easy, 2: Medium, 3: Hard
    val showStartDialog: Boolean = false,
    val soundEnabled: Boolean = true
)
