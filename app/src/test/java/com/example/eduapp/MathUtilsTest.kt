package com.example.eduapp

import com.example.eduapp.util.MathUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class MathUtilsTest {

    @Test
    fun testCalculateScoreWithBonus() {
        val score = MathUtils.calculateScore(10, 2, 20)
        assertEquals(40, score) // (10*2) + 20
    }

    @Test
    fun testCalculateScoreNoBonus() {
        val score = MathUtils.calculateScore(10, 2, 40)
        assertEquals(20, score) // (10*2) + 0
    }
}
