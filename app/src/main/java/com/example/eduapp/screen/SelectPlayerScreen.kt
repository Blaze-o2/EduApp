package com.example.eduapp.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
fun SelectPlayerScreen(navController: NavController, viewModel: AppViewModel) {
    val users by viewModel.users.collectAsStateWithLifecycle(initialValue = emptyList())
    var newPlayerName by rememberSaveable { mutableStateOf("") }
    
    // Get unique player names
    val playerNames = remember(users) {
        users.map { it.username }.distinct()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Player", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(text = "Add New Player", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newPlayerName,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isLetterOrDigit() || it.isWhitespace() }) {
                                        newPlayerName = newValue
                                    }
                                },
                                label = { Text("Player Name") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    if (newPlayerName.isNotBlank()) {
                                        viewModel.selectPlayer(newPlayerName.trim())
                                        navController.navigate("game")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(56.dp),
                                enabled = newPlayerName.isNotBlank()
                            ) {
                                Text("Start")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            if (playerNames.isNotEmpty()) {
                item {
                    Text(text = "Previous Players", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(playerNames) { name ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        ListItem(
                            headlineContent = { Text(name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                            trailingContent = { Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                            modifier = Modifier.clickable {
                                viewModel.selectPlayer(name)
                                navController.navigate("game")
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    }

    val state by viewModel.gameState.collectAsStateWithLifecycle()
    if (state.showStartDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Game in Progress") },
            text = { Text("Would you like to continue your saved game or start a new one?") },
            confirmButton = {
                Button(onClick = { 
                    viewModel.startGame(isNew = false)
                    navController.navigate("game") 
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    viewModel.startGame(isNew = true)
                    navController.navigate("game") 
                }) {
                    Text("New Game")
                }
            }
        )
    }
}
