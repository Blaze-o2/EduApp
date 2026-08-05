package com.example.eduapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduapp.data.PuzzleRepository
import com.example.eduapp.database.User
import com.example.eduapp.model.GameState
import com.example.eduapp.model.Puzzle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(private val repository: PuzzleRepository) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    val users = repository.allUsers

    init {
        loadNewPuzzle()
    }

    fun loadNewPuzzle() {
        viewModelScope.launch {
            val puzzle = repository.fetchNewPuzzle(_gameState.value.level)
            _gameState.update { it.copy(currentPuzzle = puzzle, message = "") }
        }
    }

    fun submitAnswer(answerStr: String) {
        val currentPuzzle = _gameState.value.currentPuzzle ?: return
        val userAnswer = answerStr.toIntOrNull()
        
        if (userAnswer == currentPuzzle.answer) {
            _gameState.update { current ->
                val addedScore = com.example.eduapp.util.MathUtils.calculateScore(10, current.level, 10)
                val newScore = current.score + addedScore
                
                current.copy(
                    score = newScore,
                    level = current.level + 1, // Level up after each successful answer
                    message = "Correct!",
                    showResultDialog = true,
                    lastSolvedAnswer = currentPuzzle.answer
                )
            }
            saveProgress("Player1") // Automatically save progress for default user
        } else {
            _gameState.update { it.copy(message = "Wrong answer, try again!") }
        }
    }

    fun dismissResultDialog() {
        _gameState.update { it.copy(showResultDialog = false) }
        loadNewPuzzle()
    }

    fun saveProgress(username: String) {
        viewModelScope.launch {
            val user = User(
                username = username,
                currentLevel = _gameState.value.level,
                totalScore = _gameState.value.score,
                highScore = _gameState.value.score, // Simple logic for now
                puzzlesSolved = _gameState.value.score / 10
            )
            repository.insertUser(user)
        }
    }

    fun clearStats() {
        viewModelScope.launch {
            repository.clearAllUsers()
        }
    }
}
