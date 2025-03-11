package com.example.othello.modules.game.multiplayer.data

data class GameSession(
    val sessionId: String? = null,
    val hostId: String? = null,
    val hostName: String? = null,
    val opponentId: String? = null,
    val opponentName: String? = null,
    val status: String = "waiting", // waiting, playing, finished
    val winnerId: String? = null,
    val ready: Int = 0,
    val score: Int = 0,
    val board: List<String> = List(64) { " " }, // Flat list for 8x8 board
    val currentTurn: String = "host", // host or opponent
    val hostTile: String = "X", // Host is always 'X'
    val opponentTile: String = "O" // Opponent is always 'O'
//    val currentTurn: String = "", // hostId or guestId
//    val flippedTiles: List<Pair<Int, Int>> = emptyList(),
//    val validMoves: List<Pair<Int, Int>> = emptyList(),
//    val gameOver: Boolean = false,
//    val hostTile: Char = 'X',
//    val guestTile: Char = 'O',
//    val winnerId: String? = null,
//    val board: List<List<String>> = List(8) { List(8) { " " } },
) {
//    // Convert to map for Firestore updates
//    fun toMap(): Map<String, Any?> {
//        return mapOf(
//            "sessionId" to sessionId,
//            "hostId" to hostId,
//            "hostName" to hostName,
//            "opponentId" to opponentId,
//            "opponentName" to opponentName,
//            "status" to status,
//            "currentTurn" to currentTurn,
//            "flippedTiles" to flippedTiles,
//            "validMoves" to validMoves,
//            "gameOver" to gameOver,
//            "hostTile" to hostTile,
//            "guestTile" to guestTile,
//            "winnerId" to winnerId,
//            "board" to board
//        )
//    }
}

data class GameList(
    val sessionId: String? = null,
    val hostName: String? = null
)