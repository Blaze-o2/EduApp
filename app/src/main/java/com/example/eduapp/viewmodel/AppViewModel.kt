package com.example.eduapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduapp.data.PuzzleRepository
import com.example.eduapp.database.User
import com.example.eduapp.model.GameState
import com.example.eduapp.util.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: PuzzleRepository,
    private val soundManager: SoundManager? = null,
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    val users = repository.allUsers

    init {
        loadNewPuzzle()
    }

    fun selectPlayer(name: String) {
        viewModelScope.launch {
            val allUsers = repository.dao.getAllUsersList()
            val existingUser = allUsers.find { it.username == name }
            
            _gameState.update { it.copy(
                playerName = name,
                showStartDialog = (existingUser != null && (existingUser.savedLevel > 1 || existingUser.savedScore > 0))
            ) }
            
            if (!_gameState.value.showStartDialog) {
                startGame(isNew = true)
            }
        }
    }

    fun startGame(isNew: Boolean) {
        viewModelScope.launch {
            if (isNew) {
                _gameState.update { it.copy(
                    score = 0,
                    level = 1,
                    incorrectAttempts = 0,
                    isGameOver = false,
                    showStartDialog = false,
                    message = ""
                ) }
            } else {
                val name = _gameState.value.playerName ?: return@launch
                val allUsers = repository.dao.getAllUsersList()
                val user = allUsers.find { it.username == name }
                user?.let { u ->
                    _gameState.update { it.copy(
                        score = u.savedScore,
                        level = u.savedLevel,
                        incorrectAttempts = u.savedIncorrectAttempts,
                        difficultyLevel = u.savedDifficulty,
                        maxAttempts = when(u.savedDifficulty) {
                            1 -> 5
                            2 -> 3
                            else -> 1
                        },
                        isGameOver = false,
                        showStartDialog = false,
                        message = ""
                    ) }
                }
            }
            loadNewPuzzle()
        }
    }

    fun setDifficulty(level: Int) {
        val maxAtt = when(level) {
            1 -> 5
            2 -> 3
            else -> 1
        }
        _gameState.update { it.copy(difficultyLevel = level, maxAttempts = maxAtt) }
        loadNewPuzzle()
        _gameState.value.playerName?.let { saveProgress(it) }
    }

    fun toggleSound(enabled: Boolean) {
        _gameState.update { it.copy(soundEnabled = enabled) }
        soundManager?.setEnabled(enabled)
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
            soundManager?.playSuccess()
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
            soundManager?.playError()
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
            val current = _gameState.value
            val user = User(
                username = username,
                currentLevel = current.level,
                totalScore = current.score,
                highScore = current.score,
                puzzlesSolved = if (current.score > 0) current.score / 10 else 0,
                savedLevel = current.level,
                savedScore = current.score,
                savedIncorrectAttempts = current.incorrectAttempts,
                savedDifficulty = current.difficultyLevel
            )
            repository.insertUser(user)
        }
    }

    fun clearStats() {
        viewModelScope.launch {
            repository.clearAllUsers()
        }
    }

    override fun onCleared() {
        soundManager?.release()
    }
}
