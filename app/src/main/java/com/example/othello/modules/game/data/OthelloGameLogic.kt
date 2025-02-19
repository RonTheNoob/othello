package com.example.othello.modules.game.data

import kotlin.random.Random

class OthelloGameLogic {

    fun resetBoard(board: Array<MutableList<Char>>) {
        // Blanks out/resets the board it is passed, except for the original starting position/starting pieces.
        for (x in 0..7) {
            for (y in 0..7) {
                board[x][y] = ' '
            }
        }
        // Starting pieces:
        board[3][3] = 'X'
        board[3][4] = 'O'
        board[4][3] = 'O'
        board[4][4] = 'X'
    }

    fun whoGoesFirst(): String {
        // Randomly choose the player who will go first.
        return if (Random.nextInt(2) == 0) "computer" else "player"
    }

    fun isValidMove(board: Array<MutableList<Char>>, tile: Char, xStart: Int, yStart: Int): Any {
        // Returns False if the player's move on space xStart, yStart is invalid.
        // If it is a valid move, returns a list of spaces that would become the player's if they made a move here.
        if (board[xStart][yStart] != ' ' || !isOnBoard(xStart, yStart)) {
            return false
        }

        board[xStart][yStart] = tile // temporarily set the tile on the board.

        val otherTile = if (tile == 'X') 'O' else 'X'

        val tilesToFlip = mutableListOf<Pair<Int, Int>>()
        val directions = listOf(
            Pair(0, 1), Pair(1, 1), Pair(1, 0), Pair(1, -1),
            Pair(0, -1), Pair(-1, -1), Pair(-1, 0), Pair(-1, 1)
        )
        for ((xDirection, yDirection) in directions) {
            var x = xStart
            var y = yStart
            x += xDirection // first step in the direction
            y += yDirection // first step in the direction
            if (isOnBoard(x, y) && board[x][y] == otherTile) {
                x += xDirection
                y += yDirection
                if (!isOnBoard(x, y)) {
                    // continue in for loop
                    continue
                }
                while (board[x][y] == otherTile) {
                    x += xDirection
                    y += yDirection
                    if (!isOnBoard(x, y)) { // break out of while loop, then continue in for loop
                        break
                    }
                }
                if (!isOnBoard(x, y)) {
                    continue
                }
                if (board[x][y] == tile) {
                    // There are pieces to flip over. Go in the reverse direction until we reach the original space, noting all the tiles along the way.
                    while (true) {
                        x -= xDirection
                        y -= yDirection
                        if (x == xStart && y == yStart) {
                            break
                        }
                        tilesToFlip.add(Pair(x, y))
                    }
                }
            }
        }

        board[xStart][yStart] = ' ' //restore the empty space
        if (tilesToFlip.isEmpty()) { // If no tiles were flipped, this is not a valid move.
            return false
        }
        return tilesToFlip
    }

    fun isOnBoard(x: Int, y: Int): Boolean {
        return x in 0..7 && y in 0..7
    }

    fun getValidMoves(board: Array<MutableList<Char>>, tile: Char): List<Pair<Int, Int>> {
        val validMoves = mutableListOf<Pair<Int, Int>>()
        for (x in 0..7) {
            for (y in 0..7) {
                if (isValidMove(board, tile, x, y) != false) {
                    validMoves.add(Pair(x, y))
                }
            }
        }
        return validMoves
    }

    fun makeMove(board: Array<MutableList<Char>>, tile: Char, xStart: Int, yStart: Int): Boolean {
        val tilesToFlipAny = isValidMove(board, tile, xStart, yStart)
        if (tilesToFlipAny == false) {
            return false
        }

        @Suppress("UNCHECKED_CAST")
        val tilesToFlip = tilesToFlipAny as List<Pair<Int, Int>>
        board[xStart][yStart] = tile
        for ((x, y) in tilesToFlip) {
            board[x][y] = tile
        }
        return true
    }

    fun getBoardCopy(board: Array<MutableList<Char>>): Array<MutableList<Char>> {
        val dupeBoard = Array(8) { MutableList(8) { ' ' } }
        for (x in 0..7) {
            for (y in 0..7) {
                dupeBoard[x][y] = board[x][y]
            }
        }
        return dupeBoard
    }

    fun isOnCorner(x: Int, y: Int): Boolean {
        return (x == 0 && y == 0) ||
                (x == 7 && y == 0) ||
                (x == 0 && y == 7) ||
                (x == 7 && y == 7)
    }

    fun getComputerMove(board: Array<MutableList<Char>>, computerTile: Char): Pair<Int, Int> {
        val possibleMoves = getValidMoves(board, computerTile).toMutableList()
        possibleMoves.shuffle()

        // Always go for a corner if available
        for ((x, y) in possibleMoves) {
            if (isOnCorner(x, y)) {
                return Pair(x, y)
            }
        }

        // Find best scoring move
        var bestScore = -1
        var bestMove: Pair<Int, Int> = possibleMoves[0]
        for ((x, y) in possibleMoves) {
            val dupeBoard = getBoardCopy(board)
            makeMove(dupeBoard, computerTile, x, y)
            val score = getScoreOfBoard(dupeBoard)[computerTile] ?: 0
            if (score > bestScore) {
                bestMove = Pair(x, y)
                bestScore = score
            }
        }

        return bestMove
    }

    fun getScoreOfBoard(board: Array<MutableList<Char>>): Map<Char, Int> {
        var xScore = 0
        var oScore = 0
        for (x in 0..7) {
            for (y in 0..7) {
                if (board[x][y] == 'X') {
                    xScore += 1
                }
                if (board[x][y] == 'O') {
                    oScore += 1
                }
            }
        }
        return mapOf('X' to xScore, 'O' to oScore)
    }
}