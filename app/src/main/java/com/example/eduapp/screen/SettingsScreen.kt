package com.example.eduapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.eduapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: AppViewModel) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    var soundEnabled by remember { mutableStateOf(true) }
    var difficulty by remember(gameState.difficultyLevel) { mutableStateOf(gameState.difficultyLevel.toFloat()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            ListItem(
                headlineContent = { Text("Sound Effects") },
                trailingContent = {
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Difficulty", modifier = Modifier.padding(horizontal = 16.dp))
            Slider(
                value = difficulty,
                onValueChange = { difficulty = it },
                valueRange = 1f..3f,
                steps = 1,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            val diffLabel = when(difficulty.toInt()) {
                1 -> "Easy (5 attempts)"
                2 -> "Medium (3 attempts)"
                else -> "Hard (1 attempt)"
            }
            Text(
                text = diffLabel,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    viewModel.setDifficulty(difficulty.toInt())
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}
