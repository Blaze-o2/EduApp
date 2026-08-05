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

    // Maps character names to assumed drawable resource names (without extension)
    private val characterImageMap = mapOf(
        "Lion" to "img_lion",
        "Monkey" to "img_monkey",
        "Giraffe" to "img_giraffe",
        "Elephant" to "img_elephant",
        "Zebra" to "img_zebra",
        "Tiger" to "img_tiger",
        "Panda" to "img_panda",
        "Koala" to "img_koala",
        "Kangaroo" to "img_kangaroo",
        "Fox" to "img_fox",
        "Wolf" to "img_wolf"
    )

    private fun generateLocalPuzzle(difficultyLevel: Int, gameLevel: Int): Puzzle {
        val random = Random(System.currentTimeMillis())
        val selectedChars = characters.shuffled(random).take(3 + difficultyLevel)
        
        val maxVal = 5 + (difficultyLevel * 5) + (gameLevel / 2)
        val charValues = selectedChars.associateWith { random.nextInt(2, maxVal) }.toMutableMap() // min 2 for multiplication
        
        val equations = mutableListOf<String>()
        val puzzleType = random.nextInt(3) // 0: Standard, 1: Product, 2: Mixed

        when (puzzleType) {
            1 -> generateProductPuzzle(equations, selectedChars, charValues, random)
            2 -> generateMixedPuzzle(equations, selectedChars, charValues, random)
            else -> generateStandardPuzzle(equations, selectedChars, charValues, difficultyLevel)
        }
        
        // Extract all unique characters that actually appear in the generated equations
        val usedChars = mutableSetOf<String>()
        val nameRegex = characters.joinToString("|").toRegex()
        equations.forEach { eq ->
            nameRegex.findAll(eq).forEach { usedChars.add(it.value) }
        }
        
        // Ensure we only pick a target variable that the user can actually solve for
        val targetChar = if (usedChars.isNotEmpty()) usedChars.random(random) else selectedChars[0]
        
        return Puzzle(
            id = System.currentTimeMillis().toString(),
            equations = equations.shuffled(random),
            targetVariable = targetChar,
            answer = charValues[targetChar] ?: 0,
            difficulty = difficultyLevel,
            characterImages = selectedChars.associateWith { characterImageMap[it] ?: "ic_placeholder" }
        )
    }

    private fun generateStandardPuzzle(
        equations: MutableList<String>,
        selectedChars: List<String>,
        charValues: Map<String, Int>,
        difficultyLevel: Int
    ) {
        val char1 = selectedChars[0]
        val char2 = selectedChars[1]
        val char3 = selectedChars[2]
        
        equations.add("$char1 + $char1 = ${charValues[char1]!! * 2}")
        equations.add("$char1 + $char2 = ${charValues[char1]!! + charValues[char2]!!}")
        equations.add("$char2 + $char3 = ${charValues[char2]!! + charValues[char3]!!}")
        
        if (difficultyLevel >= 2) {
            val char4 = selectedChars[3]
            equations.add("$char4 - $char2 = ${charValues[char4]!! - charValues[char2]!!}")
        }
    }

    private fun generateProductPuzzle(
        equations: MutableList<String>,
        selectedChars: List<String>,
        charValues: MutableMap<String, Int>,
        random: Random
    ) {
        val char1 = selectedChars[0]
        val char2 = selectedChars[1]
        val char3 = selectedChars[2]

        equations.add("$char1 * 2 = ${charValues[char1]!! * 2}")
        equations.add("$char1 * $char2 = ${charValues[char1]!! * charValues[char2]!!}")
        equations.add("$char3 + $char2 = ${charValues[char3]!! + charValues[char2]!!}")
        
        if (selectedChars.size > 3) {
            val char4 = selectedChars[3]
            // Ensure char4 is even so the division is clean
            val originalVal = charValues[char4]!!
            val evenVal = if (originalVal % 2 != 0) originalVal + 1 else originalVal
            equations.add("$char4 / 2 = ${evenVal / 2}")
            charValues[char4] = evenVal
        }
    }

    private fun generateMixedPuzzle(
        equations: MutableList<String>,
        selectedChars: List<String>,
        charValues: MutableMap<String, Int>,
        random: Random
    ) {
        val char1 = selectedChars[0]
        val char2 = selectedChars[1]
        val char3 = selectedChars[2]

        equations.add("$char1 + $char1 + $char1 = ${charValues[char1]!! * 3}")
        equations.add("$char1 + $char2 - $char3 = ${charValues[char1]!! + charValues[char2]!! - charValues[char3]!!}")
        equations.add("$char3 * $char1 = ${charValues[char3]!! * charValues[char1]!!}")

        if (selectedChars.size > 3) {
            val char4 = selectedChars[3]
            equations.add("$char2 + $char4 = ${charValues[char2]!! + charValues[char4]!!}")
        }
    }
}
