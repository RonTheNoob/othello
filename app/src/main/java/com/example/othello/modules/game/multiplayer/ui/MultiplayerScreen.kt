package com.example.othello.modules.game.multiplayer.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.othello.R
import com.example.othello.modules.game.data.OthelloGameLogic
import com.example.othello.modules.game.multiplayer.data.GameSession
import com.example.othello.modules.game.ui.GameState

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
        } else if (gameState.message.contains("Opponent quit")) {
            // Show a message if the opponent quit
            Text(
                text = "Opponent quit. You win!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Green,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            // Game Info
            MultiplayerGameInfo(gameState, gameSession)

            // Board
            BoardView(
                board = gameState.board,
                validMoves = gameState.validMoves,
                onCellClick = { x, y ->
                    multiplayerViewModel.makeMove(x, y)
                }
            )

            // Game End or Quit
            GameEnd(
                navController = navController,
                gameOver = gameState.gameOver || gameSession.status == "finished",
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
    onCellClick: (Int, Int) -> Unit
) {
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
                        validMoveColor = Color.Green.copy(alpha = 0.5f)
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
    validMoveColor: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(BorderStroke(1.dp, gridColor))
            .background(if (isValidMove) validMoveColor else Color.Transparent)
            .clickable(onClick = onClick), // Make the cell clickable
        contentAlignment = Alignment.Center
    ) {
        // Displays a circle if the cell is occupied
        if (cell != ' ') {
            Surface(
                shape = CircleShape,
                color = if (cell == 'X') Color.Black else Color.White,
                modifier = Modifier
                    .size(30.dp)
                    .border(2.dp, if (cell == 'X') Color.White else Color.Black, CircleShape)
            ) {}
        }
    }
}

@Composable
fun MultiplayerGameInfo(gameState: GameState, gameSession: GameSession) {
    val gameLogic = OthelloGameLogic()
    val scores = gameLogic.getScoreOfBoard(gameState.board)
    val playerScore = scores[gameState.playerTile] ?: 0
    val opponentScore = scores[gameState.computerTile] ?: 0

    if (gameSession.status == "finished") {
        val winner = if (playerScore > opponentScore) "You" else "Opponent"
    }

    val turnIndicator = when {
        gameSession.status == "waiting" -> "Waiting for opponent..."
        gameSession.status == "finished" -> "Game over! ${if (playerScore > opponentScore) "You win!" else "Opponent wins!"}"
        gameState.message.contains("Your turn") -> "Your turn"
        else -> "Opponent's turn"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Text(
            text = "Multiplayer Othello",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("You (${if (gameState.playerTile == 'X') "Black" else "White"})")
                Text(
                    text = "$playerScore",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Opponent (${if (gameState.computerTile == 'X') "Black" else "White"})")
                Text(
                    text = "$opponentScore",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Text(
            text = turnIndicator,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
            color = if (turnIndicator == "Your turn") Color.Green else MaterialTheme.colorScheme.onBackground
        )

//        Text(
//            text = gameState.message,
//            style = MaterialTheme.typography.headlineMedium,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.padding(top = 4.dp),
//            color = if (turnIndicator == "Your turn") Color.Green else MaterialTheme.colorScheme.onBackground
//        )
    }
}

@Composable
fun GameEnd(navController: NavController, gameOver: Boolean, multiplayerViewModel: MultiplayerViewModel, sessionId: String) {
    Row {
        Button(
            onClick = {
                multiplayerViewModel.handlePlayerQuit(sessionId)
                navController.popBackStack()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Quit the Game?")
        }
    }
}