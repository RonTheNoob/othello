package com.example.othello.modules.game.ui

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.othello.R
import com.example.othello.modules.game.data.OthelloGameLogic
import kotlinx.coroutines.delay

@Composable
fun OthelloGame(navController: NavController, viewModel: OthelloViewModel = viewModel()) {
    val gameState by viewModel.gameState

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Game
        GameInfo(gameState)

        // Board
        BoardView(
            board = gameState.board,
            validMoves = if (gameState.currentTurn == "player") gameState.validMoves else emptyList(),
            onCellClick = { x, y ->
                if (gameState.currentTurn == "player" && gameState.validMoves.contains(Pair(x, y))) {
                    viewModel.makePlayerMove(x, y)
                }
            },
            flippedTiles = gameState.flippedTiles,
            viewModel = viewModel
        )

        GameEnd(
            navController = navController,
            gameOver = gameState.gameOver,
            onRestart = { viewModel.resetGame() }
        )
    }
}

@Composable
fun BoardView(
    board: Array<MutableList<Char>>,
    validMoves: List<Pair<Int, Int>>,
    onCellClick: (Int, Int) -> Unit,
    flippedTiles: List<Pair<Int, Int>>,
    viewModel: OthelloViewModel
) {

    LaunchedEffect(flippedTiles) {
        if (flippedTiles.isNotEmpty()) {
            val totalDelay = flippedTiles.size * 100L + 1000L // Total delay for all tiles to flip
            delay(totalDelay)
            viewModel.clearFlippedTiles()
        }
    }

    Column(
        modifier = Modifier
            .background(colorResource(id = R.color.board_color))
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
    val index = flippedTiles.indexOf(Pair(x, y)) // Get the index of the tile in the flippedTiles list
    val delay = index * 100L // Add a delay based on the tile's position (100ms per tile)

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = delay.toInt(),
            easing = FastOutSlowInEasing
        )
    )

    // Determine the target color based on the cell value
    val targetColor = if (cell == 'X') Color.White else Color.Black
    val oppositeColor = if (cell == 'X') Color.Black else Color.White

    // Animate the color change only for flipped tiles
    val animatedColor by animateColorAsState(
        targetValue = if (isFlipped && rotation > 90f) oppositeColor else targetColor, // Change color at midpoint for flipped tiles
        animationSpec = tween(
            durationMillis = 500, // Half the flip duration
            delayMillis = if (isFlipped) delay.toInt() + 500 else 0, // Start color change at midpoint for flipped tiles
            easing = FastOutSlowInEasing
        )
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .border(BorderStroke(1.dp, gridColor))
            .background(if (isValidMove) validMoveColor else Color.Transparent)
            .clickable(onClick = onClick) // Make the cell clickable
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
                color = if (isFlipped) animatedColor else targetColor, //if (cell == 'X') Color.Black else Color.White,
                modifier = Modifier
                    .size(30.dp)
                    .border(2.dp, if (cell == 'X') Color.White else Color.Black, CircleShape)
            ) {}
        }
    }
}

@Composable
fun GameInfo(gameState: GameState) {
    val gameLogic = OthelloGameLogic()
    val scores = gameLogic.getScoreOfBoard(gameState.board)
    val playerScore = scores[gameState.playerTile] ?: 0
    val computerScore = scores[gameState.computerTile] ?: 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Text(
            text = "Othello",
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
                Text("Computer (${if (gameState.computerTile == 'X') "Black" else "White"})")
                Text(
                    text = "$computerScore",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Text(
            text = gameState.message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun GameEnd(navController: NavController, gameOver: Boolean, onRestart: () -> Unit) {
    Row {
        if (gameOver) {
            Button(
                onClick = onRestart,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Play Again?")
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = { navController.popBackStack()},
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Quit the Game")
        }
    }
}