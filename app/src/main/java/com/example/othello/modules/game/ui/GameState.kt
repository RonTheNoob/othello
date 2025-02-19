package com.example.othello.modules.game.ui

data class GameState(
    val board: Array<MutableList<Char>> = Array(8) { MutableList(8) { ' ' } },
    val currentTurn: String = "player",
    val playerTile: Char = 'X',
    val computerTile: Char = 'O',
    val gameOver: Boolean = false,
    val validMoves: List<Pair<Int, Int>> = emptyList(),
    val message: String = ""

) {

    // Following code is because there is a property with an 'Array' type in a 'data' class
    // Android Studio recommended/suggested to override 'equals()' and 'hashCode()'

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!board.contentEquals(other.board)) return false
        if (currentTurn != other.currentTurn) return false
        if (playerTile != other.playerTile) return false
        if (computerTile != other.computerTile) return false
        if (gameOver != other.gameOver) return false
        if (validMoves != other.validMoves) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentHashCode()
        result = 31 * result + currentTurn.hashCode()
        result = 31 * result + playerTile.hashCode()
        result = 31 * result + computerTile.hashCode()
        result = 31 * result + gameOver.hashCode()
        result = 31 * result + validMoves.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }
}
