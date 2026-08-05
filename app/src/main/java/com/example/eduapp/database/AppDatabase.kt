package com.example.eduapp.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
