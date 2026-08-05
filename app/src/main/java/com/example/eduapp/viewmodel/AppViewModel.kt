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

    fun selectPlayer(name: String) {
        _gameState.update { it.copy(playerName = name) }
    }

    fun setDifficulty(level: Int) {
        val maxAtt = when(level) {
            1 -> 5
            2 -> 3
            else -> 1
        }
        _gameState.update { it.copy(difficultyLevel = level, maxAttempts = maxAtt) }
        loadNewPuzzle() // Refresh puzzle with new difficulty
    }

    fun loadNewPuzzle() {
        viewModelScope.launch {
            val puzzle = repository.fetchNewPuzzle(_gameState.value.difficultyLevel, _gameState.value.level)
            _gameState.update { it.copy(currentPuzzle = puzzle, message = "") }
        }
    }

    fun submitAnswer(answerStr: String) {
        val currentPuzzle = _gameState.value.currentPuzzle ?: return
        if (_gameState.value.isGameOver) return

        val userAnswer = answerStr.toIntOrNull()
        
        if (userAnswer == currentPuzzle.answer) {
            _gameState.update { current ->
                val addedScore = com.example.eduapp.util.MathUtils.calculateScore(10, current.level, 10)
                val newScore = current.score + addedScore
                
                current.copy(
                    score = newScore,
                    level = current.level + 1,
                    message = "Correct!",
                    showResultDialog = true,
                    lastSolvedAnswer = currentPuzzle.answer
                )
            }
            _gameState.value.playerName?.let { saveProgress(it) }
        } else {
            val nextIncorrect = _gameState.value.incorrectAttempts + 1
            val isLost = nextIncorrect >= _gameState.value.maxAttempts
            
            _gameState.update { 
                it.copy(
                    incorrectAttempts = nextIncorrect,
                    isGameOver = isLost,
                    message = if (isLost) "Game Over! You've run out of attempts." else "Wrong answer, try again! (${_gameState.value.maxAttempts - nextIncorrect} left)"
                )
            }
            
            if (isLost) {
                _gameState.value.playerName?.let { saveProgress(it) }
            }
        }
    }

    fun dismissResultDialog() {
        _gameState.update { it.copy(showResultDialog = false) }
        loadNewPuzzle()
    }

    fun resetGame() {
        _gameState.update { 
            it.copy(
                score = 0,
                level = 1,
                isGameOver = false,
                incorrectAttempts = 0,
                message = ""
            )
        }
        loadNewPuzzle()
    }

    fun saveProgress(username: String) {
        viewModelScope.launch {
            val user = User(
                username = username,
                currentLevel = _gameState.value.level,
                totalScore = _gameState.value.score,
                highScore = _gameState.value.score,
                puzzlesSolved = if (_gameState.value.score > 0) _gameState.value.score / 10 else 0
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
