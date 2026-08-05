package com.example.eduapp.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var newPlayerName by remember { mutableStateOf("") }
    
    // Get unique player names
    val playerNames = remember(users) {
        users.map { it.username }.distinct()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Player") },
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
            Text("Add New Player", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Player Name") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newPlayerName.isNotBlank()) {
                            viewModel.selectPlayer(newPlayerName)
                            navController.navigate("game")
                        }
                    }
                ) {
                    Text("Start")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (playerNames.isNotEmpty()) {
                Text("Previous Players", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(playerNames) { name ->
                        ListItem(
                            headlineContent = { Text(name) },
                            modifier = Modifier.clickable {
                                viewModel.selectPlayer(name)
                                navController.navigate("game")
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
