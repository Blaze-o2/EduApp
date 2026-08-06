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
    // Repository and SoundManager are kept at the activity level to ensure stability
    private lateinit var repository: PuzzleRepository
    private lateinit var soundManager: com.example.eduapp.util.SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Database, API, and Repository once per activity lifecycle
        // This avoids costly re-initialization during recompositions
        val db = AppDatabase.getDatabase(applicationContext)

        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/") // Placeholder URL for the puzzle API
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        
        val service = retrofit.create(PuzzleService::class.java)
        repository = PuzzleRepository(db.appDao(), service)
        soundManager = com.example.eduapp.util.SoundManager(applicationContext)

        enableEdgeToEdge()
        setContent {
            EduAppTheme {
                AppNav(repository, soundManager)
            }
        }
    }
}

@Composable
fun AppNav(repository: PuzzleRepository, soundManager: com.example.eduapp.util.SoundManager) {
    val navController = rememberNavController()
    val factory = remember(repository, soundManager) { AppViewModelFactory(repository, soundManager) }
    val appViewModel: AppViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "landing") {
        composable("landing") {
            LandingScreen(navController)
        }
        composable("game") {
            GameScreen(navController, appViewModel)
        }
        composable("settings") {
            SettingsScreen(navController, appViewModel)
        }
        composable("stats") {
            StatisticsScreen(navController, appViewModel)
        }
        composable("selectPlayer") {
            SelectPlayerScreen(navController, appViewModel)
        }
    }
}
