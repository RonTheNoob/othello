package com.example.othello.modules.game.multiplayer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.othello.R
import com.example.othello.modules.game.data.OthelloGameLogic
import com.example.othello.modules.game.multiplayer.data.GameSession
import com.example.othello.modules.game.ui.GameState
import kotlinx.coroutines.delay

@Composable
fun MultiplayerScreen(
    navController: NavController,
    multiplayerViewModel: MultiplayerViewModel,
    sessionId: String
) {
    LaunchedEffect(sessionId) {
        multiplayerViewModel.initializeGame(sessionId)
    }

    val gameState by multiplayerViewModel.gameState
    val gameSession by multiplayerViewModel.gameSession.collectAsState()

    // states to control the visibility of the dialog
    var showOpponentQuitDialog by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    // shows the dialog if the opponent quits
    LaunchedEffect(gameState.opponentQuitMessage) {
        if (gameState.opponentQuitMessage.isNotEmpty()) {
            showOpponentQuitDialog = true
        }
    }

    // shows game over dialog when the game is finished
    LaunchedEffect(gameState.gameOver, gameState.gameOverMessage) {
        if (gameState.gameOver && gameState.gameOverMessage.isNotEmpty() && !showOpponentQuitDialog) {
            showGameOverDialog = true
        }
    }

    // shows dialog when the opponent quits
    if (showOpponentQuitDialog) {
        AlertDialog(
            onDismissRequest = {
                showOpponentQuitDialog = false
                navController.navigate("sessions") {
                    popUpTo("sessions") { inclusive = true }
                }
            },
            title = {
                Text("Game Over")
            },
            text = {
                Text(gameState.opponentQuitMessage)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOpponentQuitDialog = false
                        navController.navigate("sessions") {
                            popUpTo("sessions") { inclusive = true }
                        }
                    }
                ) {
                    Text("Return to Lobby")
                }
            }
        )
    }

    // shows dialog when the game is over
    if (showGameOverDialog) {
        AlertDialog(
            onDismissRequest = {
                showGameOverDialog = false
                navController.navigate("sessions") {
                    popUpTo("sessions") { inclusive = true }
                }
            },
            title = {
                Text("Game Over")
            },
            text = {
                Text(gameState.gameOverMessage)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGameOverDialog = false
                        navController.navigate("sessions") {
                            popUpTo("sessions") { inclusive = true }
                        }
                    }
                ) {
                    Text("Return to Lobby")
                }
            }
        )
    }

    BackHandler {
        multiplayerViewModel.handlePlayerQuit(sessionId)
        navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Game status
        if (gameSession.status == "waiting") {
            WaitingForOpponent()
        } else {
            // Game Info
            MultiplayerGameInfo(gameState, gameSession, isHost = multiplayerViewModel.isHost)

            // Board
            BoardView(
                board = gameState.board,
                validMoves = gameState.validMoves,
                onCellClick = { x, y ->
                    multiplayerViewModel.makeMove(x, y)
                },
                flippedTiles = gameSession.flippedTiles,
                multiplayerViewModel = multiplayerViewModel
            )

            // Game End or Quit
            GameEnd(
                navController = navController,
                gameOver = gameState.gameOver,
                multiplayerViewModel = multiplayerViewModel,
                sessionId = sessionId
            )
        }
    }
}

@Composable
fun WaitingForOpponent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Text(
            text = "Waiting for opponent to join...",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        CircularProgressIndicator(
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = "Good luck!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun BoardView(
    board: Array<MutableList<Char>>,
    validMoves: List<Pair<Int, Int>>,
    onCellClick: (Int, Int) -> Unit,
    flippedTiles: List<Pair<Int, Int>>,
    multiplayerViewModel: MultiplayerViewModel
) {
    LaunchedEffect(flippedTiles) {
        if (flippedTiles.isNotEmpty()) {
            val totalDelay = flippedTiles.size * 100L + 1000L // Total delay for all tiles to flip
            delay(totalDelay)
            multiplayerViewModel.clearFlippedTiles()
        }
    }

    Column(
        modifier = Modifier
            .background(Color(0xFF008000)) // Green board color
            .border(BorderStroke(2.dp, Color.Black)) // Grid color for the board.
    ) {
        // creates the 8x8 grid
        for (y in 0 until 8) {
            Row {
                for (x in 0 until 8) {
                    val isValidMove = validMoves.contains(Pair(x, y))

                    CellView(
                        cell = board[x][y],
                        isValidMove = isValidMove,
                        onClick = { onCellClick(x, y) },
                        gridColor = Color.Black,
                        validMoveColor = Color.Green.copy(alpha = 0.5f),
                        flippedTiles = flippedTiles,
                        x = x,
                        y = y
                    )
                }
            }
        }
    }
}

@Composable
fun CellView(
    cell: Char,
    isValidMove: Boolean,
    onClick: () -> Unit,
    gridColor: Color,
    validMoveColor: Color,
    flippedTiles: List<Pair<Int, Int>>,
    x: Int,
    y: Int
) {
    val isFlipped = flippedTiles.contains(Pair(x, y))
    val index = flippedTiles.indexOf(Pair(x, y)) // gets the index of the tile in the flippedTiles list
    val delay = index * 100L // adds a delay based on the tile's position (100ms per tile)

    // Animate the rotation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = delay.toInt(), // delay for the wavy effect
            easing = FastOutSlowInEasing
        )
    )

    // determines the target color based on the cell value
    val targetColor = if (cell == 'X') Color.Black else Color.White
    val oppositeColor = if (cell == 'X') Color.White else Color.Black

    // animate the color change for flipped tiles
    val animatedColor by animateColorAsState(
        targetValue = if (isFlipped && rotation > 90f) oppositeColor else targetColor, // changes color at the midpoint for flipped tiles
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = if (isFlipped) delay.toInt() + 500 else 0, // Start color change at midpoint for flipped tiles
            easing = FastOutSlowInEasing
        )
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .border(BorderStroke(1.dp, gridColor))
            .background(if (isValidMove) validMoveColor else Color.Transparent)
            .clickable(onClick = onClick) // Makes the cell clickable
            .graphicsLayer {
            rotationY = rotation
            cameraDistance = 8 * density
        },
        contentAlignment = Alignment.Center
    ) {
        // Displays a circle if the cell is occupied
        if (cell != ' ') {
            Surface(
                shape = CircleShape,
                color = if (isFlipped) animatedColor else targetColor,
                modifier = Modifier
                    .size(30.dp)
                    .border(2.dp, if (cell == 'X') Color.White else Color.Black, CircleShape)
            ) {}
        }
    }
}

@Composable
fun MultiplayerGameInfo(gameState: GameState, gameSession: GameSession, isHost: Boolean) {
    val gameLogic = OthelloGameLogic()
    val scores = gameLogic.getScoreOfBoard(gameState.board)
    val playerScore = scores[gameState.playerTile] ?: 0
    val opponentScore = scores[gameState.computerTile] ?: 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Host Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val playerName = if (isHost) {
                    "${gameSession.hostName} (${if (gameState.playerTile == 'X') "Black" else "White"})"
                } else {
                    "${gameSession.opponentName ?: "You"} (${if (gameState.playerTile == 'X') "Black" else "White"})"
                }

                Text(playerName)
                Text(
                    text = "$playerScore",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            // Opponent Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val opponentName = if (isHost) {
                    "${gameSession.opponentName ?: "Opponent"} (${if (gameState.computerTile == 'X') "Black" else "White"})"
                } else {
                    "${gameSession.hostName} (${if (gameState.computerTile == 'X') "Black" else "White"})"
                }

                Text(opponentName)
                Text(
                    text = "$opponentScore",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        // Turn message with enhanced color/styling
        val turnMessageColor = when {
            gameSession.status == "finished" -> MaterialTheme.colorScheme.error
            gameState.turnMessage == "Your turn!" -> Color.Green
            else -> MaterialTheme.colorScheme.onBackground
        }

        Text(
            text = gameState.turnMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .background(
                    color = if (gameState.turnMessage == "Your turn!")
                        Color.Green.copy(alpha = 0.1f)
                    else
                        Color.Transparent,
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            color = turnMessageColor
        )
    }
}

@Composable
fun GameEnd(
    navController: NavController,
    gameOver: Boolean,
    multiplayerViewModel: MultiplayerViewModel,
    sessionId: String
) {
    if (gameOver) {
        Button(
            onClick = {
                navController.navigate("sessions") {
                    popUpTo("sessions") { inclusive = true }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Return to Lobby")
        }
    } else {
        Button(
            onClick = {
                multiplayerViewModel.handlePlayerQuit(sessionId)
                navController.popBackStack()
            },
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Forfeit?")
        }
    }
}