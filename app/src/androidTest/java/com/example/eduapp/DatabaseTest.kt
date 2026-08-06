package com.example.eduapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.eduapp.database.AppDao
import com.example.eduapp.database.AppDatabase
import com.example.eduapp.database.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var appDao: AppDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        appDao = db.appDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() = runBlocking {
        val user = User(
            username = "TestUser",
            currentLevel = 5,
            totalScore = 150,
            highScore = 200,
            puzzlesSolved = 4,
            savedLevel = 5,
            savedScore = 150
        )
        appDao.insert(user)
        val allUsers = appDao.getAllUsers().first()
        assertEquals(allUsers[0].username, "TestUser")
        assertEquals(allUsers[0].currentLevel, 5)
    }

    @Test
    @Throws(Exception::class)
    fun updateExistingUser() = runBlocking {
        val user1 = User(id = 1, username = "Player1", totalScore = 50)
        appDao.insert(user1)
        
        val userUpdate = User(id = 1, username = "Player1", totalScore = 100)
        appDao.insert(userUpdate)
        
        val allUsers = appDao.getAllUsers().first()
        assertEquals(1, allUsers.size)
        assertEquals(100, allUsers[0].totalScore)
    }

    @Test
    @Throws(Exception::class)
    fun deleteAllUsers() = runBlocking {
        val user = User(username = "PlayerToDelete")
        appDao.insert(user)
        appDao.deleteAll()
        
        val allUsers = appDao.getAllUsers().first()
        assertEquals(0, allUsers.size)
    }

    @Test
    @Throws(Exception::class)
    fun getUserById() = runBlocking {
        val user = User(id = 99, username = "SpecificUser")
        appDao.insert(user)
        
        val retrieved = appDao.getUserById(99)
        assertEquals("SpecificUser", retrieved?.username)
    }
}
