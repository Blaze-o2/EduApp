package com.example.eduapp.data

import com.example.eduapp.model.Puzzle
import retrofit2.http.GET
import retrofit2.http.Query

interface PuzzleService {
    @GET("puzzles/random")
    suspend fun getRandomPuzzle(@Query("difficulty") difficulty: Int): Puzzle
}
