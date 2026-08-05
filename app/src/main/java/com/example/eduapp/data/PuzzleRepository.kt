package com.example.eduapp.data

import com.example.eduapp.database.AppDao
import com.example.eduapp.database.User
import com.example.eduapp.model.Puzzle
import kotlinx.coroutines.flow.Flow

class PuzzleRepository(
    private val dao: AppDao,
    private val service: PuzzleService
) {
    val allUsers: Flow<List<User>> = dao.getAllUsers()

    suspend fun getUser(id: Int): User? = dao.getUserById(id)

    suspend fun insertUser(user: User) = dao.insert(user)

    suspend fun clearAllUsers() = dao.deleteAll()

    suspend fun fetchNewPuzzle(difficulty: Int): Puzzle {
        // In a real app, this would call the service
        // For now, we'll return a generated one if the service fails
        return try {
            service.getRandomPuzzle(difficulty)
        } catch (e: Exception) {
            generateLocalPuzzle(difficulty)
        }
    }

    private fun generateLocalPuzzle(difficulty: Int): Puzzle {
        // Sample puzzle generation logic
        val a = (1..10).random() * difficulty
        val b = (1..10).random() * difficulty
        val c = (1..10).random() * difficulty
        
        return Puzzle(
            id = System.currentTimeMillis().toString(),
            equations = listOf(
                "Lion + Monkey + Monkey = ${a + 2*b}",
                "Giraffe + Giraffe + 4 = ${2*c + 4}",
                "Giraffe + Elephant = ${c + a}",
                "Elephant + Monkey = ${a + b}"
            ),
            targetVariable = "Lion",
            answer = a,
            difficulty = difficulty
        )
    }
}
