package com.example.eduapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.eduapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController, viewModel: AppViewModel) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    var userAnswer by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puzzle - Level ${gameState.level}") },
                actions = {
                    Text(
                        "Score: ${gameState.score}",
                        modifier = Modifier.padding(end = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            gameState.currentPuzzle?.let { puzzle ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        puzzle.equations.forEach { equation ->
                            Text(
                                text = equation,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Find the value of: ${puzzle.targetVariable} = ?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { userAnswer = it },
                    label = { Text("Your Answer") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (gameState.message.isNotEmpty()) {
                    Text(
                        text = gameState.message,
                        color = if (gameState.message.contains("Correct")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Attempts: ${gameState.maxAttempts - gameState.incorrectAttempts} / ${gameState.maxAttempts}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.submitAnswer(userAnswer)
                        userAnswer = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !gameState.isGameOver
                ) {
                    Text("Submit")
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Quit Game")
            }
        }
    }

    if (gameState.showResultDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResultDialog() },
            title = { Text("Puzzle Solved!") },
            text = {
                Column {
                    Text("Correct! The answer was ${gameState.lastSolvedAnswer}.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Moving to Level ${gameState.level}...", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissResultDialog() }) {
                    Text("Next Level")
                }
            }
        )
    }

    if (gameState.isGameOver) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Game Over") },
            text = { Text("You've run out of attempts! Better luck next time.") },
            confirmButton = {
                Button(onClick = { 
                    viewModel.resetGame()
                    navController.popBackStack()
                }) {
                    Text("Back to Home")
                }
            }
        )
    }
}
