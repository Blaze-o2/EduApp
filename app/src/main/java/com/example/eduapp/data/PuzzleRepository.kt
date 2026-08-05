package com.example.eduapp.data

import com.example.eduapp.database.AppDao
import com.example.eduapp.database.User
import com.example.eduapp.model.Puzzle
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class PuzzleRepository(
    val dao: AppDao,
    private val service: PuzzleService
) {
    val allUsers: Flow<List<User>> = dao.getAllUsers()

    suspend fun getUser(id: Int): User? = dao.getUserById(id)

    suspend fun insertUser(user: User) = dao.insert(user)

    suspend fun clearAllUsers() = dao.deleteAll()

    suspend fun fetchNewPuzzle(difficultyLevel: Int, gameLevel: Int): Puzzle {
        return try {
            service.getRandomPuzzle(difficultyLevel)
        } catch (e: Exception) {
            generateLocalPuzzle(difficultyLevel, gameLevel)
        }
    }

    private val characters = listOf(
        "Lion", "Monkey", "Giraffe", "Elephant", "Zebra", 
        "Tiger", "Panda", "Koala", "Kangaroo", "Fox", "Wolf"
    )

    private fun generateLocalPuzzle(difficultyLevel: Int, gameLevel: Int): Puzzle {
        val random = Random(System.currentTimeMillis())
        val selectedChars = characters.shuffled(random).take(3 + difficultyLevel)
        
        // Assign random values to selected characters
        // Values scale with both difficulty and game level
        val maxVal = 5 + (difficultyLevel * 5) + (gameLevel / 2)
        val charValues = selectedChars.associateWith { random.nextInt(1, maxVal) }
        
        val equations = mutableListOf<String>()
        
        when (difficultyLevel) {
            1 -> { // Easy: Simple sums and repetitions
                val char1 = selectedChars[0]
                val char2 = selectedChars[1]
                val char3 = selectedChars[2]
                
                equations.add("$char1 + $char1 = ${charValues[char1]!! * 2}")
                equations.add("$char1 + $char2 = ${charValues[char1]!! + charValues[char2]!!}")
                equations.add("$char2 + $char3 = ${charValues[char2]!! + charValues[char3]!!}")
            }
            2 -> { // Medium: More variables and some subtraction/multiplication by small constants
                val char1 = selectedChars[0]
                val char2 = selectedChars[1]
                val char3 = selectedChars[2]
                val char4 = selectedChars[3]
                
                equations.add("$char1 + $char1 + $char1 = ${charValues[char1]!! * 3}")
                equations.add("$char1 + $char2 = ${charValues[char1]!! + charValues[char2]!!}")
                equations.add("$char2 + $char3 + $char3 = ${charValues[char2]!! + 2 * charValues[char3]!!}")
                equations.add("$char4 - $char3 = ${charValues[char4]!! - charValues[char3]!!}")
            }
            else -> { // Hard: More variables, mixed operations, larger numbers
                val char1 = selectedChars[0]
                val char2 = selectedChars[1]
                val char3 = selectedChars[2]
                val char4 = selectedChars[3]
                val char5 = selectedChars[4]
                
                equations.add("$char1 + $char1 + 4 = ${2 * charValues[char1]!! + 4}")
                equations.add("$char1 * 2 + $char2 = ${2 * charValues[char1]!! + charValues[char2]!!}")
                equations.add("$char2 + $char3 + $char4 = ${charValues[char2]!! + charValues[char3]!! + charValues[char4]!!}")
                equations.add("$char5 - $char4 = ${charValues[char5]!! - charValues[char4]!!}")
                equations.add("$char5 + $char1 + $char2 = ${charValues[char5]!! + charValues[char1]!! + charValues[char2]!!}")
            }
        }
        
        // Randomize target variable from the characters used in equations
        val targetChar = selectedChars.random(random)
        
        return Puzzle(
            id = System.currentTimeMillis().toString(),
            equations = equations.shuffled(random), // Randomize layout
            targetVariable = targetChar,
            answer = charValues[targetChar]!!,
            difficulty = difficultyLevel
        )
    }
}
