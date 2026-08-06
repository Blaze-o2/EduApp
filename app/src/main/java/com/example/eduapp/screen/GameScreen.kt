package com.example.eduapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
    // Collect the game state from ViewModel using lifecycle-aware collector
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    // Local state for the text input field
    var userAnswer by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puzzle - Level ${gameState.level}", fontWeight = FontWeight.Bold) },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Score: ${gameState.score}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), MaterialTheme.colorScheme.surface)
                            ))
                            .padding(24.dp)
                    ) {
                        puzzle.equations.forEach { equation ->
                            EquationLine(equation, puzzle)
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Solve: ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            CharacterImage(puzzle.targetVariable, puzzle, size = 40)
                            Text(
                                text = " = ?",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedTextField(
                            value = userAnswer,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || (newValue.startsWith("-") && newValue.drop(1).all { it.isDigit() }) || newValue.all { it.isDigit() }) {
                                    userAnswer = newValue
                                }
                            },
                            label = { Text("Enter Answer") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            isError = userAnswer.isEmpty() && gameState.message.isNotEmpty()
                        )

                        if (gameState.message.isNotEmpty()) {
                            Surface(
                                color = if (gameState.message.contains("Correct")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(
                                    text = gameState.message,
                                    color = if (gameState.message.contains("Correct")) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Attempts",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Attempts: ${gameState.maxAttempts - gameState.incorrectAttempts} / ${gameState.maxAttempts}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.submitAnswer(userAnswer)
                                userAnswer = ""
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !gameState.isGameOver && userAnswer.toIntOrNull() != null
                        ) {
                            Text("Submit Answer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.outline)
            ) {
                Text("Quit Game", fontWeight = FontWeight.SemiBold)
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
