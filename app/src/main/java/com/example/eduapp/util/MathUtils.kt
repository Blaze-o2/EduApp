package com.example.eduapp.util

object MathUtils {
    fun calculateScore(baseScore: Int, multiplier: Int, timeTaken: Int): Int {
        val timeBonus = if (timeTaken < 30) 20 else 0
        return (baseScore * multiplier) + timeBonus
    }
    
    fun isLevelUp(currentScore: Int, threshold: Int): Boolean {
        return currentScore >= threshold
    }
}
