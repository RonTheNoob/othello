package com.example.othello

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.othello.modules.auth.data.AuthRepository
import com.example.othello.modules.auth.ui.AuthViewModel
import com.example.othello.modules.auth.ui.LoginScreen
import com.example.othello.modules.auth.ui.RegistrationScreen
import com.example.othello.modules.game.multiplayer.ui.MultiplayerViewModel
import com.example.othello.modules.game.multiplayer.ui.SessionsScreen
import com.example.othello.modules.game.multiplayer.ui.MultiplayerScreen
import com.example.othello.modules.game.ui.OthelloGame
import com.example.othello.modules.home.data.LeaderboardViewModel
import com.example.othello.modules.home.ui.CreditsScreen
import com.example.othello.modules.home.ui.HomeScreen
import com.example.othello.modules.home.ui.LeaderboardScreen
import com.example.othello.modules.home.ui.TutorialScreen
import com.example.othello.ui.theme.OthelloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OthelloTheme{
                val authViewModel = AuthViewModel(authRepository = AuthRepository())
                val multiplayerViewModel: MultiplayerViewModel by viewModels()

                val navController = rememberNavController()

                val authState by authViewModel.uiState.collectAsState()
//                val gameState by multiplayerViewModel.gameState.collectAsState()
//                val authEmail = authState.email

//                var startDestination = "sessions"
//                if (authState.currentUser == null) {
//                    startDestination = "login"
//                } else if (gameState.currentGameSession != null) {
//                    // user has current joined the game
//                    startDestination = "game"
//                }

                var startDestination = "home"
                if (authState.currentUser == null) {
                    startDestination = "login"
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("home") {
                        HomeScreen(
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    }

                    composable("singleplayer") {
                        OthelloGame(navController = navController)
                    }

                    composable("login") {
                        LoginScreen(
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    }

                    composable("register") {
                        RegistrationScreen(
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    }

                    composable("leaderboard") {
                        val leaderboardViewModel: LeaderboardViewModel by viewModels()
                        LeaderboardScreen(
                            navController = navController,
                            viewModel = leaderboardViewModel
                        )
                    }

                    composable("tutorial") {
                        TutorialScreen(navController = navController)
                    }

                    composable("credits") {
                        CreditsScreen(navController = navController)
                    }

                    // MULTIPLAYER (FOR TESTING)
                    composable("sessions") {
                        SessionsScreen(
                            navController = navController,
                            authViewModel = authViewModel,
                            multiplayerViewModel = multiplayerViewModel,
                        )
                    }

                    composable(
                        route = "game/{sessionId}",
                        arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        // Retrieve the sessionId from the route arguments
                        val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                        MultiplayerScreen(
                            navController = navController,
                            multiplayerViewModel = multiplayerViewModel,
                            sessionId = sessionId // Pass the sessionId to the MultiplayerScreen
                        )
                    }
                }
            }
        }
    }
}