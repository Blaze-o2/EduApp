package com.example.eduapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.eduapp.data.PuzzleRepository
import com.example.eduapp.data.PuzzleService
import com.example.eduapp.database.AppDatabase
import com.example.eduapp.screen.*
import com.example.eduapp.ui.theme.EduAppTheme
import com.example.eduapp.viewmodel.AppViewModel
import com.example.eduapp.viewmodel.AppViewModelFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduAppTheme {
                AppNav()
            }
        }
    }
}

@Composable
fun AppNav() {
    val context = LocalContext.current
    val navController = rememberNavController()
    
    // Initialize Database, API, and Repository
    val repository = remember {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_db"
        ).build()

        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/") // Placeholder URL
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        
        val service = retrofit.create(PuzzleService::class.java)
        PuzzleRepository(db.appDao(), service)
    }

    val factory = AppViewModelFactory(repository)
    val appViewModel: AppViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "landing") {
        composable("landing") {
            LandingScreen(navController)
        }
        composable("game") {
            GameScreen(navController, appViewModel)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("stats") {
            StatisticsScreen(navController, appViewModel)
        }
    }
}
