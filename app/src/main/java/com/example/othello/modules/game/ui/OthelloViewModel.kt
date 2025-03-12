package com.example.othello.modules.game.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.othello.modules.game.data.OthelloGameLogic
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OthelloViewModel : ViewModel() {
    private val _gameState = mutableStateOf(GameState())
    val gameState: State<GameState> = _gameState

    private val gameLogic = OthelloGameLogic()

    init {
        resetGame()
    }

    fun resetGame() {
        val newBoard = Array(8) { MutableList(8) { ' ' } }
        gameLogic.resetBoard(newBoard)

        val playerTile = 'X' // default but can be changed if you want
        val computerTile = 'O'

        _gameState.value = GameState(
            board = newBoard,
            currentTurn = gameLogic.whoGoesFirst(),
            playerTile = playerTile,
            computerTile = computerTile,
            validMoves = gameLogic.getValidMoves(newBoard, playerTile)
        )

        if (_gameState.value.currentTurn == "computer") {
            makeComputerMove()
        }
    }

    fun choosePlayerTile(chosenTile: Char) {
        val playerTile = if (chosenTile == 'X') 'X' else 'O'
        val computerTile = if (playerTile == 'X') 'O' else 'X'

        _gameState.value = _gameState.value.copy(
            playerTile = playerTile,
            computerTile = computerTile,
            validMoves = gameLogic.getValidMoves(_gameState.value.board, playerTile)
        )
    }

//    fun makePlayerMove(x: Int, y: Int) {
//        val currentState = _gameState.value
//
//        if (currentState.gameOver || currentState.currentTurn != "player") {
//            return
//        }
//
//        val move = gameLogic.makeMove(
//            currentState.board,
//            currentState.playerTile,
//            x,
//            y
//        )
//
//        if (move) {
//            val flippedTiles = gameLogic.getFlippedTiles(currentState.board, currentState.playerTile, x, y)
//
//            // this checks if computer can move
//            val computerValidMoves = gameLogic.getValidMoves(currentState.board, currentState.computerTile)
//
//            if(computerValidMoves.isNotEmpty()) {
//                _gameState.value = currentState.copy(
//                    currentTurn = "computer",
//                    message = "Computer's turn",
//                    flippedTiles = flippedTiles
//                )
//                makeComputerMove()
//            } else {
//                // this is for the player
//                val playerValidMoves = gameLogic.getValidMoves(currentState.board, currentState.playerTile)
//
//                if (playerValidMoves.isEmpty()) {
//                    // if no more moves, then it's game over of course
//                    endGame()
//                } else {
//                    _gameState.value = currentState.copy(
//                        validMoves = playerValidMoves,
//                        message = "Computer has no moves. Your turn!",
//                        flippedTiles = flippedTiles
//                    )
//                }
//            }
//        }
//    }

    fun makePlayerMove(x: Int, y: Int) {
        val currentState = _gameState.value

        if (currentState.gameOver || currentState.currentTurn != "player") {
            return
        }

        // Check if the move is valid and get the tiles to flip
        val tilesToFlipAny = gameLogic.isValidMove(currentState.board, currentState.playerTile, x, y)
        if (tilesToFlipAny == false) {
            return
        }

        @Suppress("UNCHECKED_CAST")
        val tilesToFlip = tilesToFlipAny as List<Pair<Int, Int>>

        // Make the move and update the board
        gameLogic.makeMove(currentState.board, currentState.playerTile, x, y)

        // Update the state with the tiles to flip
        _gameState.value = currentState.copy(
            currentTurn = "computer",
            message = "Computer's turn",
            flippedTiles = tilesToFlip
        )

        // Check if the computer can move
        val computerValidMoves = gameLogic.getValidMoves(currentState.board, currentState.computerTile)
        if (computerValidMoves.isNotEmpty()) {
            makeComputerMove()
        } else {
            // Check if the player can move
            val playerValidMoves = gameLogic.getValidMoves(currentState.board, currentState.playerTile)
            if (playerValidMoves.isEmpty()) {
                endGame()
            } else {
                _gameState.value = currentState.copy(
                    validMoves = playerValidMoves,
                    message = "Computer has no moves. Your turn!"
                )
            }
        }
    }


    private fun makeComputerMove() {
        viewModelScope.launch {
            delay(2000)

            val currentState = _gameState.value
            val move = gameLogic.getComputerMove(currentState.board, currentState.computerTile)

            // Check if the move is valid and get the tiles to flip
            val tilesToFlipAny = gameLogic.isValidMove(currentState.board, currentState.computerTile, move.first, move.second)
            if (tilesToFlipAny == false) {
                return@launch
            }

            // Cast the result to a list of tiles to flip
            @Suppress("UNCHECKED_CAST")
            val tilesToFlip = tilesToFlipAny as List<Pair<Int, Int>>

            // Make the move and update the board
            gameLogic.makeMove(currentState.board, currentState.computerTile, move.first, move.second)

            // Check if the player can move
            val playerValidMoves = gameLogic.getValidMoves(currentState.board, currentState.playerTile)

            if (playerValidMoves.isNotEmpty()) {
                _gameState.value = currentState.copy(
                    currentTurn = "player",
                    validMoves = playerValidMoves,
                    message = "Your turn!",
                    flippedTiles = tilesToFlip // Update flippedTiles
                )
            } else {
                // Check if the computer can move
                val computerValidMoves = gameLogic.getValidMoves(currentState.board, currentState.computerTile)

                if (computerValidMoves.isEmpty()) {
                    endGame()
                } else {
                    _gameState.value = currentState.copy(
                        message = "You have no moves. Computer's turn!",
                        flippedTiles = tilesToFlip // Update flippedTiles
                    )
                    makeComputerMove()
                }
            }
        }
    }

//    private fun makeComputerMove() {
//        viewModelScope.launch {
//            delay(1000)
//
//            val currentState = _gameState.value
//            val move = gameLogic.getComputerMove(currentState.board, currentState.computerTile)
//
//            gameLogic.makeMove(currentState.board, currentState.computerTile, move.first, move.second)
//
//            val flippedTiles = gameLogic.getFlippedTiles(currentState.board, currentState.computerTile, move.first, move.second)
//
//            // check if the player can do a move
//            val playerValidMoves = gameLogic.getValidMoves(currentState.board, currentState.playerTile)
//
//            if(playerValidMoves.isNotEmpty()) {
//                _gameState.value = currentState.copy(
//                    currentTurn = "player",
//                    validMoves = playerValidMoves,
//                    message = "Your turn!",
//                    flippedTiles = flippedTiles
//                )
//            } else {
//                // check if the computer can make a move
//                val computerValidMoves = gameLogic.getValidMoves(currentState.board, currentState.computerTile)
//
//                if (computerValidMoves.isEmpty()) {
//                    // if no more moves, then it's game over of course
//                    endGame()
//                } else {
//                    _gameState.value = currentState.copy(
//                        message = "You have no moves. Computer's turn!",
//                        flippedTiles = flippedTiles
//                    )
//                    makeComputerMove()
//                }
//            }
//        }
//    }

    fun clearFlippedTiles() {
        _gameState.value = _gameState.value.copy(flippedTiles = emptyList())
    }

    private fun endGame() {
        val currentState = _gameState.value
        val scores = gameLogic.getScoreOfBoard(currentState.board)
        val playerScore = scores[currentState.playerTile] ?: 0
        val computerScore = scores[currentState.computerTile] ?: 0

        val message = when {
            playerScore > computerScore -> "You win! $playerScore to $computerScore"
            playerScore < computerScore -> "You lose! $playerScore to $computerScore"
            else -> "It's a tie! $playerScore to $computerScore"
        }

        _gameState.value = currentState.copy(
            gameOver = true,
            message = message
        )
    }
}