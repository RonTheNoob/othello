package com.example.othello.modules.game.multiplayer.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.othello.modules.auth.ui.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    multiplayerViewModel: MultiplayerViewModel
) {
    LaunchedEffect(Unit) {
        multiplayerViewModel.fetchGameSessions()
    }

    // Get the current user email
    val authState by authViewModel.uiState.collectAsState()
    val currentUserEmail = authState.email

    // Collect the list of available game sessions
    val gameList by multiplayerViewModel.gameList.collectAsState()

    // Reversi/Othello-themed colors
    val othelloColors = OthelloThemeColors()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = othelloColors.primaryContainer,
                    titleContentColor = othelloColors.onPrimaryContainer
                ),
                title = {
                    Text("Available Matches", style = MaterialTheme.typography.titleLarge)
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("home")
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Back to Home",
                            tint = othelloColors.onPrimaryContainer
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Create a new game session
                    if (currentUserEmail != null) {
                        multiplayerViewModel.createGameSession(
                            hostEmail = currentUserEmail,
                            onSuccess = { sessionId ->
                                Log.d("SessionsScreen", "Game session created with id: $sessionId")
                                navController.navigate("game/${sessionId}")
                            },
                            onFailure = { exception ->
                                Log.e("SessionsScreen", "Failed to create game session", exception)
                            }
                        )
                    }
                },
                containerColor = othelloColors.primary,
                contentColor = othelloColors.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Create Match"
                )
            }
        },
        containerColor = othelloColors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn {
                items(gameList) { game ->
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = othelloColors.surface,
                            headlineColor = othelloColors.onSurface,
                            supportingColor = othelloColors.onSurface.copy(alpha = 0.7f)
                        ),
                        headlineContent = {
                            Text(
                                text = game.hostName ?: "Unknown Host",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Session ID: ${game.sessionId}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        trailingContent = {
                            if (currentUserEmail != null) {
                                Button(
                                    onClick = {
                                        // Join the game session and navigate to the game screen
                                        multiplayerViewModel.joinGameSession(
                                            sessionId = game.sessionId ?: "",
                                            opponentEmail = currentUserEmail
                                        )
                                        // Navigate to the game screen with the sessionId as an argument
                                        navController.navigate("game/${game.sessionId}")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = othelloColors.primary,
                                        contentColor = othelloColors.onPrimary
                                    )
                                ) {
                                    Text("Join")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// Reversi/Othello-themed colors
data class OthelloThemeColors(
    val primary: Color = Color(0xFF2E7D32), // Dark green
    val onPrimary: Color = Color.White, // White
    val primaryContainer: Color = Color(0xFF1B5E20), // Darker green
    val onPrimaryContainer: Color = Color.White, // White
    val background: Color = Color(0xFFF5F5F5), // Light gray background
    val surface: Color = Color.White, // White surface
    val onSurface: Color = Color(0xFF212121) // Dark text
)