package com.example.eduapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.eduapp.R
import com.example.eduapp.model.Puzzle
import com.example.eduapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController, viewModel: AppViewModel) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    var userAnswer by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puzzle - Level ${gameState.level}") },
                actions = {
                    Text(
                        text = "Score: ${gameState.score}",
                        modifier = Modifier.padding(end = 16.dp),
                        fontWeight = FontWeight.Bold,
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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
                            EquationLine(equation, puzzle)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Find the value of: ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            CharacterImage(puzzle.targetVariable, puzzle, size = 32)
                            Text(
                                text = " = ?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Attempts: ${gameState.maxAttempts - gameState.incorrectAttempts} / ${gameState.maxAttempts}",
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
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

@Composable
fun CharacterImage(name: String, puzzle: Puzzle, size: Int = 24) {
    val context = LocalContext.current
    val imageName = puzzle.characterImages[name]
    val resId = if (imageName != null) {
        context.resources.getIdentifier(imageName, "drawable", context.packageName)
    } else 0
    
    if (resId != 0) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = resId),
            contentDescription = name,
            modifier = Modifier.size(size.dp)
        )
    } else {
        // Fallback to name if image not found
        Text(
            text = name, 
            fontSize = (size * 0.7).sp, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
fun EquationLine(equation: String, puzzle: Puzzle) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        val tokens = equation.split(" ")
        tokens.forEach { token ->
            if (puzzle.characterImages.containsKey(token)) {
                CharacterImage(token, puzzle, size = 28)
            } else {
                Text(
                    text = " $token ", 
                    fontSize = 20.sp,
                    fontWeight = if (token == "=") FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
