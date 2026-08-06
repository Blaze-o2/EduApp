package com.example.eduapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduapp.data.PuzzleRepository
import com.example.eduapp.database.User
import com.example.eduapp.model.GameState
import com.example.eduapp.model.Puzzle
import com.example.eduapp.util.SoundManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * AppViewModel manages the game state and logic for the EduApp.
 * It coordinates between the repository, sound manager, and the UI.
 */
class AppViewModel(
    private val repository: PuzzleRepository,
    private val soundManager: SoundManager? = null,
) : ViewModel() {

    // Internal state flow to handle game updates
    private val _gameState = MutableStateFlow(GameState())
    // Public exposure of state for the UI
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Json instance for serializing/deserializing puzzles
    private val json = Json { ignoreUnknownKeys = true }

    // Flow of users from the database for statistics and selection
    val users = repository.allUsers

    /**
     * Handles player selection. Checks if the player exists to show a "Continue" dialog
     * or starts a fresh game.
     */
    fun selectPlayer(name: String) {
        viewModelScope.launch {
            val allUsers = repository.dao.getAllUsersList()
            val existingUser = allUsers.find { it.username == name }
            // Only show continue dialog if the user exists, has made progress, and hasn't lost the game
            val shouldShowDialog = (existingUser != null && 
                                  (existingUser.savedLevel > 1 || existingUser.savedScore > 0) &&
                                  existingUser.savedIncorrectAttempts < (when(existingUser.savedDifficulty) {
                                      1 -> 5
                                      2 -> 3
                                      else -> 1
                                  }))
            
            _gameState.update { it.copy(
                playerName = name,
                showStartDialog = shouldShowDialog,
                navigateToGame = false
            ) }
            
            if (!shouldShowDialog) {
                startGame(isNew = true)
            }
        }
    }

    /**
     * Resets the flag after the navigation to the game screen has been handled.
     */
    fun onNavigatedToGame() {
        _gameState.update { it.copy(navigateToGame = false) }
    }

    /**
     * Starts a game session. 
     * @param isNew if true, resets progress. If false, attempts to load saved state.
     */
    fun startGame(isNew: Boolean) {
        viewModelScope.launch {
            var shouldLoadNew = isNew
            if (isNew) {
                _gameState.update { it.copy(
                    score = 0,
                    level = 1,
                    incorrectAttempts = 0,
                    isGameOver = false,
                    showStartDialog = false,
                    message = "",
                    currentPuzzle = null,
                    navigateToGame = true
                ) }
            } else {
                val name = _gameState.value.playerName ?: return@launch
                val allUsers = repository.dao.getAllUsersList()
                val user = allUsers.find { it.username == name }
                user?.let { u ->
                    val savedPuzzle: Puzzle? = try {
                        u.savedPuzzleJson?.let { json.decodeFromString<Puzzle>(it) }
                    } catch (_: Exception) {
                        null
                    }
                    
                    if (savedPuzzle == null) shouldLoadNew = true

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
                        message = "",
                        currentPuzzle = savedPuzzle,
                        navigateToGame = true
                    ) }
                } ?: run { shouldLoadNew = true }
            }
            if (shouldLoadNew) {
                loadNewPuzzle()
            }
        }
    }

    fun setDifficulty(level: Int) {
        if (_gameState.value.difficultyLevel == level) return
        val maxAtt = when(level) {
            1 -> 5
            2 -> 3
            else -> 1
        }
        _gameState.update { it.copy(difficultyLevel = level, maxAttempts = maxAtt, currentPuzzle = null) }
        loadNewPuzzle()
        _gameState.value.playerName?.let { saveProgress(it) }
    }

    fun toggleSound(enabled: Boolean) {
        _gameState.update { it.copy(soundEnabled = enabled) }
        soundManager?.setEnabled(enabled)
    }

    /**
     * Fetches a new puzzle from the repository and updates the state.
     */
    fun loadNewPuzzle() {
        viewModelScope.launch {
            val puzzle = repository.fetchNewPuzzle(_gameState.value.difficultyLevel, _gameState.value.level)
            _gameState.update { it.copy(currentPuzzle = puzzle, message = "") }
        }
    }

    /**
     * Validates the user's answer against the current puzzle.
     * Updates score, level, or incorrect attempts accordingly.
     */
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
        _gameState.update { it.copy(showResultDialog = false, currentPuzzle = null) }
        loadNewPuzzle()
    }

    fun resetGame() {
        _gameState.update { 
            it.copy(
                score = 0,
                level = 1,
                isGameOver = false,
                incorrectAttempts = 0,
                message = "",
                currentPuzzle = null
            )
        }
        loadNewPuzzle()
    }

    /**
     * Persists the current game state to the database for the given user.
     * Updates existing record if it exists, otherwise creates a new one.
     */
    fun saveProgress(username: String) {
        viewModelScope.launch {
            val current = _gameState.value
            val allUsers = repository.dao.getAllUsersList()
            val existingUser = allUsers.find { it.username == username }
            
            val user = User(
                id = existingUser?.id ?: 0,
                username = username,
                currentLevel = current.level,
                totalScore = current.score,
                // Only update high score if current score is better
                highScore = maxOf(existingUser?.highScore ?: 0, current.score),
                // Solved puzzles is level - 1
                puzzlesSolved = current.level - 1,
                savedLevel = current.level,
                savedScore = current.score,
                savedIncorrectAttempts = current.incorrectAttempts,
                savedDifficulty = current.difficultyLevel,
                savedPuzzleJson = current.currentPuzzle?.let { json.encodeToString(it) },
                savedTargetAnswer = current.currentPuzzle?.answer
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
