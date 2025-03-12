package com.example.othello.modules.game.multiplayer.data

data class GameSession(
    val sessionId: String? = null,
    val hostId: String? = null,
    val hostName: String? = null,
    val opponentId: String? = null,
    val opponentName: String? = null,
    val status: String = "waiting", // waiting, playing, finished
    val winnerId: String? = null,
    val board: List<String> = List(64) { " " }, // Flat list for 8x8 board
    val currentTurn: String = "host", // host or opponent
    val hostTile: String = "X", // Host is always 'X'
    val opponentTile: String = "O", // Opponent is always 'O'
    val flippedTiles: List<Pair<Int, Int>> = emptyList()
)

data class GameList(
    val sessionId: String? = null,
    val hostName: String? = null
)