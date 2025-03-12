package com.example.othello.modules.game.multiplayer.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.othello.modules.game.data.OthelloGameLogic
import com.example.othello.modules.game.multiplayer.data.GameList
import com.example.othello.modules.game.multiplayer.data.GameSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.othello.modules.game.ui.GameState
import androidx.compose.runtime.State
import com.google.firebase.firestore.FieldValue

class MultiplayerViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var sessionListener: ListenerRegistration? = null

    private val _gameList = MutableStateFlow<List<GameList>>(emptyList())
    val gameList: StateFlow<List<GameList>> get() = _gameList

    private val _gameSession = MutableStateFlow(GameSession())
    val gameSession: StateFlow<GameSession> = _gameSession.asStateFlow()

    private val _gameState = mutableStateOf(GameState())
    val gameState: State<GameState> = _gameState

    private val gameLogic = OthelloGameLogic()

    // Track the local player's role (host or opponent)
    var isHost = false
        private set
    private var localPlayerId: String? = null

    fun createGameSession(hostEmail: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        fetchUserDetails(hostEmail, { userId, username ->
            val sessionId = db.collection("sessions").document().id
            val initialBoard = gameLogic.getInitialBoard().toFlatList() // Convert to flat list (from helper function made below)
            val gameSession = GameSession(
                sessionId = sessionId,
                hostId = userId,
                hostName = username,
                status = "waiting",
                board = initialBoard
            )

            db.collection("sessions").document(sessionId)
                .set(gameSession)
                .addOnSuccessListener {
                    _gameSession.update { gameSession }
                    isHost = true // Mark as host
                    localPlayerId = userId // Stores the local player ID
                    onSuccess(sessionId)
                }
                .addOnFailureListener { onFailure(it) }
        }, onFailure)
    }

    fun updateGameSession(sessionId: String) {
        _gameSession.update { it.copy(sessionId = sessionId) }

        sessionListener?.remove()

        sessionListener = db.collection("sessions")
            .document(sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Multiplayer", "Error fetching game session", error)
                    return@addSnapshotListener
                }

                snapshot?.let { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val board = doc.get("board") as? List<String> ?: List(64) { " " }
                    val currentTurn = doc.getString("currentTurn") ?: "host"
                    val hostTile = doc.getString("hostTile") ?: "X"
                    val opponentTile = doc.getString("opponentTile") ?: "O"
                    val hostId = doc.getString("hostId") ?: ""
                    val opponentId = doc.getString("opponentId")
                    val status = doc.getString("status") ?: "waiting"
                    val hostName = doc.getString("hostName") ?: ""
                    val opponentName = doc.getString("opponentName")
                    val winnerId = doc.getString("winnerId")
                    val firestoreFlippedTiles = doc.get("flippedTiles") as? List<Map<String, Int>> ?: emptyList()

                    // Convert Firestore-compatible flippedTiles to List<Pair<Int, Int>>
                    val flippedTiles = firestoreFlippedTiles.map {
                        val x = (it["x"] as? Long)?.toInt() ?: 0
                        val y = (it["y"] as? Long)?.toInt() ?: 0
                        Pair(x, y)
                    }

                    // Convert flat list to nested array
                    val boardArray = board.toNestedArray()

                    // Update session
                    _gameSession.update { currentSession ->
                        currentSession.copy(
                            sessionId = sessionId,
                            hostId = hostId,
                            hostName = hostName,
                            opponentId = opponentId,
                            opponentName = opponentName,
                            board = board,
                            currentTurn = currentTurn,
                            hostTile = hostTile,
                            opponentTile = opponentTile,
                            status = status,
                            flippedTiles = flippedTiles,
                            winnerId = winnerId
                        )
                    }

                    // Determine if current player is host
                    isHost = hostId == localPlayerId

                    // Determine local player's tile and opponent's tile
                    val localPlayerTile = if (isHost) hostTile[0] else opponentTile[0]
                    val opponentPlayerTile = if (isHost) opponentTile[0] else hostTile[0]

                    // Determine if it's local player's turn
                    val isLocalPlayerTurn = (isHost && currentTurn == "host") || (!isHost && currentTurn == "opponent")

                    // Set display turn text
                    val turnMessage = when {
                        status == "waiting" -> "Waiting for opponent to join..."
                        status == "finished" -> "Game Over"
                        isLocalPlayerTurn -> "Your turn!"
                        else -> "Opponent's turn..."
                    }

                    // Define game over message based on status
                    val gameOverMessage = if (status == "finished") {
                        val scores = gameLogic.getScoreOfBoard(boardArray)
                        val hostScore = scores[hostTile[0]] ?: 0
                        val opponentScore = scores[opponentTile[0]] ?: 0

                        // Determine if the game ended normally or due to a player quitting
                        if (winnerId != null && (winnerId == hostId || winnerId == opponentId)) {
                            // Check if the winner won due to the other player quitting
                            val quitterExists = hostScore + opponentScore < 60 // Not a full board means likely a quit

                            if (quitterExists) {
                                // Someone quit
                                val winnerName = if (winnerId == hostId) hostName else opponentName ?: "Opponent"
                                val loserName = if (winnerId != hostId) hostName else opponentName ?: "Opponent"
                                "$winnerName wins! $loserName quit the game."
                            } else {
                                // Normal game end
                                when {
                                    hostScore > opponentScore -> "Host ($hostName) wins! $hostScore to $opponentScore"
                                    hostScore < opponentScore -> "Opponent ($opponentName) wins! $opponentScore to $hostScore"
                                    else -> "It's a tie! $hostScore to $opponentScore"
                                }
                            }
                        } else {
                            // Normal game end without explicit winner
                            when {
                                hostScore > opponentScore -> "$hostName wins! $hostScore to $opponentScore"
                                hostScore < opponentScore -> "$opponentName wins! $opponentScore to $hostScore"
                                else -> "It's a tie! $hostScore to $opponentScore"
                            }
                        }
                    } else {
                        ""
                    }

                    // Check if the game ended due to the opponent quitting
                    val opponentQuitMessage = if (status == "finished" && winnerId != null && winnerId == localPlayerId) {
                        // Calculate if the board is not fully populated (indicating a quit rather than normal game end)
                        val totalPieces = boardArray.sumOf { row -> row.count { it != ' ' } }
                        if (totalPieces < 60) { // Not a full or almost full board
                            "Opponent quit. You win!"
                        } else {
                            "" // Normal game end
                        }
                    } else {
                        ""
                    }

                    // Update the local game state
                    _gameState.value = _gameState.value.copy(
                        board = boardArray,
                        currentTurn = if (isLocalPlayerTurn) "player" else "computer",
                        playerTile = localPlayerTile,
                        computerTile = opponentPlayerTile,
                        validMoves = if (isLocalPlayerTurn && status != "finished")
                            gameLogic.getValidMoves(boardArray, localPlayerTile)
                        else
                            emptyList(),
                        turnMessage = turnMessage,
                        gameOverMessage = gameOverMessage,
                        opponentQuitMessage = opponentQuitMessage,
                        gameOver = status == "finished"
                    )
                }
            }
    }

    fun fetchGameSessions() {
        db.collection("sessions")
            .whereEqualTo("status", "waiting") // Only fetch waiting sessions
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Multiplayer", "Error fetching game sessions", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    viewModelScope.launch {
                        _gameList.value = snapshot.documents.mapNotNull { doc ->
                            GameList(
                                hostName = doc.getString("hostName") ?: "Unknown",
                                sessionId = doc.id
                            )
                        }
                    }
                }
            }
    }

    fun joinGameSession(sessionId: String, opponentEmail: String) {
        fetchUserDetails(opponentEmail, { userId, username ->
            // Store local player ID
            localPlayerId = userId

            // Update the session in Firestore with the opponent's details
            db.collection("sessions").document(sessionId)
                .update(
                    "opponentId", userId,
                    "opponentName", username,
                    "status", "playing" // Update status to "playing"
                )
                .addOnSuccessListener {
                    // Update the local game session state
                    _gameSession.update { currentSession ->
                        currentSession.copy(
                            sessionId = sessionId,
                            opponentId = userId,
                            opponentName = username,
                            status = "playing"
                        )
                    }

                    // Mark as opponent (not host)
                    isHost = false

                    // Fetch the updated game session details
                    updateGameSession(sessionId)

                    Log.d("Multiplayer", "Successfully joined game session: $sessionId")
                }
                .addOnFailureListener { exception ->
                    Log.e("Multiplayer", "Failed to join game session", exception)
                }
        }, { exception ->
            Log.e("Multiplayer", "Failed to fetch opponent details", exception)
        })
    }

    override fun onCleared() {
        sessionListener?.remove()
        super.onCleared()
    }

    private fun fetchUserDetails(email: String, onSuccess: (String, String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("Users").whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.firstOrNull()?.let { document ->
                    onSuccess(document.getString("userId") ?: "", document.getString("username") ?: "")
                } ?: onFailure(Exception("User not found"))
            }
            .addOnFailureListener(onFailure)
    }

    private fun List<String>.toNestedArray(): Array<MutableList<Char>> {
        val nestedArray = Array(8) { MutableList(8) { ' ' } }
        for (i in 0 until 64) {
            val row = i / 8
            val col = i % 8
            nestedArray[row][col] = if (this[i].isNotEmpty()) this[i][0] else ' '
        }
        return nestedArray
    }

    private fun Array<MutableList<Char>>.toFlatList(): List<String> {
        val flatList = mutableListOf<String>()
        for (row in this) {
            for (cell in row) {
                flatList.add(cell.toString())
            }
        }
        return flatList
    }

    // MULTIPLAYER GAME ITSELF
    fun initializeGame(sessionId: String) {
        updateGameSession(sessionId)
    }


    fun makeMove(x: Int, y: Int) {
        val sessionId = _gameSession.value.sessionId ?: return
        val currentSession = _gameSession.value

        // Determine if it's the local player's turn
        val isLocalPlayerTurn = (isHost && currentSession.currentTurn == "host") ||
                (!isHost && currentSession.currentTurn == "opponent")

        if (!isLocalPlayerTurn) {
            Log.d("Multiplayer", "Not player's turn")
            return
        }

        // Get the correct tile based on player role
        val tile = if (isHost) currentSession.hostTile[0] else currentSession.opponentTile[0]

        // this converts the flat list to nested array (from helper function made below)
        val boardArray = currentSession.board.toNestedArray()

        // Check if the move is valid
        val (isValid, tilesToFlip) = gameLogic.isValidMoveMulti(boardArray, tile, x, y)

        if (isValid && tilesToFlip != null) {
            // Make the move
            gameLogic.makeMoveMulti(boardArray, tile, x, y)

            // Converts the nested array back to flat list
            val updatedBoard = boardArray.toFlatList()

            // Switch turns
            val nextTurn = if (currentSession.currentTurn == "host") "opponent" else "host"

            // Checks if the next player has valid moves
            val nextPlayerTile = if (nextTurn == "host") currentSession.hostTile[0] else currentSession.opponentTile[0]
            val nextPlayerHasValidMoves = gameLogic.hasValidMovesMulti(boardArray, nextPlayerTile)

            // If next player has no valid moves, it stays the current player's turn or skips their turn
            val finalNextTurn = if (nextPlayerHasValidMoves) {
                nextTurn
            } else {
                // Check if current player still has moves
                val currentPlayerHasMoreMoves = gameLogic.hasValidMovesMulti(boardArray, tile)
                if (currentPlayerHasMoreMoves) {
                    // Keep current player's turn
                    currentSession.currentTurn
                } else {
                    // Game will end after this move
                    nextTurn
                }
            }

            Log.d("Multiplayer", "Move made at ($x,$y), switching turn from ${currentSession.currentTurn} to $finalNextTurn")

            // Convert flippedTiles to a Firestore-compatible format (List<Map<String, Int>>)
            val firestoreFlippedTiles = tilesToFlip.map {
                mapOf("x" to it.first.toInt(), "y" to it.second.toInt())
            }

            // Update Firestore with the new board, currentTurn, and flippedTiles
            db.collection("sessions").document(sessionId)
                .update(
                    "board", updatedBoard,
                    "currentTurn", finalNextTurn,
                    "flippedTiles", firestoreFlippedTiles
                )
                .addOnSuccessListener {
                    Log.d("Multiplayer", "Move made successfully")

                    // Check if the game is over
                    val hostTile = currentSession.hostTile[0]
                    val opponentTile = currentSession.opponentTile[0]
                    checkForGameOver(sessionId, boardArray, hostTile, opponentTile)
                }
                .addOnFailureListener {
                    Log.e("Multiplayer", "Failed to make move", it)
                }
        } else {
            Log.d("Multiplayer", "Invalid move attempted at $x,$y")
        }
    }

    private fun checkForGameOver(sessionId: String, board: Array<MutableList<Char>>, hostTile: Char, opponentTile: Char) {
        val hostValidMoves = gameLogic.getValidMoves(board, hostTile)
        val opponentValidMoves = gameLogic.getValidMoves(board, opponentTile)

        if (hostValidMoves.isEmpty() && opponentValidMoves.isEmpty()) {
            // Game over
            val scores = gameLogic.getScoreOfBoard(board)
            val hostScore = scores[hostTile] ?: 0
            val opponentScore = scores[opponentTile] ?: 0

            val winnerId = when {
                hostScore > opponentScore -> _gameSession.value.hostId
                hostScore < opponentScore -> _gameSession.value.opponentId
                else -> null // Tie
            }

            db.collection("sessions").document(sessionId)
                .update(
                    "status", "finished",
                    "winnerId", winnerId
                )
                .addOnSuccessListener {
                    Log.d("Multiplayer", "Game over: winner=$winnerId")
                    updateUserStats(winnerId, _gameSession.value.hostId, _gameSession.value.opponentId)
                }
                .addOnFailureListener { Log.e("Multiplayer", "Failed to update game session", it) }
        }
    }

    fun clearFlippedTiles() {
        val sessionId = _gameSession.value.sessionId ?: return

        // Clear flippedTiles in Firestore
        db.collection("sessions").document(sessionId)
            .update("flippedTiles", emptyList<Pair<Int, Int>>())
            .addOnSuccessListener {
                Log.d("Multiplayer", "Flipped tiles cleared")
            }
            .addOnFailureListener {
                Log.e("Multiplayer", "Failed to clear flipped tiles", it)
            }
    }

    fun handlePlayerQuit(sessionId: String) {
        val currentSession = _gameSession.value

        // Determine the player who quit (local player)
        val quittingPlayerId = localPlayerId
        val remainingPlayerId = if (quittingPlayerId == currentSession.hostId) {
            currentSession.opponentId
        } else {
            currentSession.hostId
        }

        // Update the session status and winner
        db.collection("sessions").document(sessionId)
            .update(
                "status", "finished",
                "winnerId", remainingPlayerId
            )
            .addOnSuccessListener {
                Log.d("Multiplayer", "Game marked as finished due to player quitting")

                // Update user stats: remaining player gets a win, quitting player gets a loss
                updateUserStats(remainingPlayerId, "wins")
                updateUserStats(quittingPlayerId, "losses")
            }
            .addOnFailureListener { e ->
                Log.e("Multiplayer", "Failed to update game session", e)
            }
    }

    private fun updateUserStats(userId: String?, field: String) {
        if (userId == null) return

        db.collection("Users").document(userId)
            .update(field, FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("Multiplayer", "User $userId $field updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Multiplayer", "Failed to update user $userId $field", e)
            }
    }

    private fun updateUserStats(winnerId: String?, hostId: String?, opponentId: String?) {
        if (winnerId == null) {
            // It's a tie
            updateUserDocument(hostId, "draws")
            updateUserDocument(opponentId, "draws")
        } else {
            // Update the winner's wins
            updateUserDocument(winnerId, "wins")
            // Update the loser's losses
            val loserId = if (winnerId == hostId) opponentId else hostId
            updateUserDocument(loserId, "losses")
        }
    }

    private fun updateUserDocument(userId: String?, field: String) {
        if (userId == null) return

        db.collection("Users").document(userId)
            .update(field, FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("Multiplayer", "User $userId $field updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Multiplayer", "Failed to update user $userId $field", e)
            }
    }
}