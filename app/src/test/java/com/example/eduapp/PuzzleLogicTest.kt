package com.example.eduapp

import com.example.eduapp.model.Puzzle
import org.junit.Assert.assertEquals
import org.junit.Test

class PuzzleLogicTest {

    @Test
    fun testPuzzleAnswerValidation() {
        val puzzle = Puzzle(
            id = "1",
            equations = listOf("A + B = 10", "A - B = 2"),
            targetVariable = "A",
            answer = 6,
            difficulty = 1
        )
        
        // Simulating the logic from ViewModel
        val userAnswer = "6"
        val isCorrect = userAnswer.toIntOrNull() == puzzle.answer
        
        assertEquals(true, isCorrect)
    }

    @Test
    fun testWrongAnswerValidation() {
        val puzzle = Puzzle(
            id = "1",
            equations = listOf("A + B = 10", "A - B = 2"),
            targetVariable = "A",
            answer = 6,
            difficulty = 1
        )
        
        val userAnswer = "5"
        val isCorrect = userAnswer.toIntOrNull() == puzzle.answer
        
        assertEquals(false, isCorrect)
    }
}
