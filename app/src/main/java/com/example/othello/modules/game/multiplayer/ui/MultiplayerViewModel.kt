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

//private val _gameList = MutableStateFlow<List<GameList>>(emptyList())
//val gameList: StateFlow<List<GameList>> get() = _gameList


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

    fun createGameSession(hostEmail: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        fetchUserDetails(hostEmail, { userId, username ->
            val sessionId = db.collection("sessions").document().id
            val initialBoard = gameLogic.getInitialBoard().toFlatList() // Convert to flat list
            val gameSession = GameSession(
                sessionId = sessionId,
                hostId = userId,
                hostName = username,
                status = "waiting",
                board = initialBoard // Use the flat list
            )

            db.collection("sessions").document(sessionId)
                .set(gameSession)
                .addOnSuccessListener {
                    _gameSession.update { gameSession }
                    onSuccess(sessionId)
                }
                .addOnFailureListener { onFailure(it) }
        }, onFailure)
    }

    fun updateGameSession(sessionId: String) {
        _gameSession.update { it.copy(sessionId = sessionId) }

        val sessionId = _gameSession.value.sessionId ?: return

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

                    // Convert flat list to nested array
                    val boardArray = board.toNestedArray()

                    _gameSession.update { currentSession ->
                        currentSession.copy(
                            sessionId = sessionId,
                            board = board,
                            currentTurn = currentTurn,
                            hostTile = hostTile,
                            opponentTile = opponentTile
                        )
                    }

                    // Update the local game state
                    _gameState.value = _gameState.value.copy(
                        board = boardArray,
                        currentTurn = currentTurn,
                        playerTile = if (_gameSession.value.hostId == _gameSession.value.hostId) hostTile[0] else opponentTile[0],
                        computerTile = if (_gameSession.value.hostId == _gameSession.value.hostId) opponentTile[0] else hostTile[0],
                        validMoves = gameLogic.getValidMoves(boardArray, hostTile[0])
                    )
                }
            }
    }

//    fun updateGameSession(sessionId: String) {
//
//        val sessionId = _gameSession.value.sessionId ?: return
//
//        sessionListener?.remove()
//
//        sessionListener = db.collection("sessions")
//            .document(sessionId)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    Log.e("Multiplayer", "Error fetching game session", error)
//                    return@addSnapshotListener
//                }
//                snapshot?.let { doc ->
//                    @Suppress("UNCHECKED_CAST")
//                    val board = doc.get("board") as? List<String> ?: List(64) { " " }
//                    val currentTurn = doc.getString("currentTurn") ?: "host"
//                    val hostTile = doc.getString("hostTile") ?: "X"
//                    val opponentTile = doc.getString("opponentTile") ?: "O"
//
//                    // Convert flat list to nested array
//                    val boardArray = board.toNestedArray()
//
//                    _gameSession.update { currentSession ->
//                        currentSession.copy(
//                            board = board,
//                            currentTurn = currentTurn,
//                            hostTile = hostTile,
//                            opponentTile = opponentTile
//                        )
//                    }
//
//                    // Update the local game state
//                    _gameState.value = _gameState.value.copy(
//                        board = boardArray,
//                        currentTurn = currentTurn,
//                        playerTile = if (_gameSession.value.hostId == _gameSession.value.hostId) hostTile[0] else opponentTile[0],
//                        computerTile = if (_gameSession.value.hostId == _gameSession.value.hostId) opponentTile[0] else hostTile[0],
//                        validMoves = gameLogic.getValidMoves(boardArray, hostTile[0])
//                    )
//                }
//            }
//    }

    // Function to fetch all game sessions
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

//    fun joinGameSession(sessionId: String, opponentEmail: String) {
//        _gameSession.update { it.copy(sessionId = sessionId) }
//        val newSessionId = _gameSession.value.sessionId ?: return
//        fetchUserDetails(opponentEmail, { userId, username ->
//            db.collection("GameSession").document(newSessionId)
//                .update("opponentId", userId, "opponentName", username, "status", "pending")
//                .addOnSuccessListener {
//                    updateGameSession(sessionId)
//                }
//                .addOnFailureListener { Log.e("Firestore", "Failed to join game session", it) }
//        }, {Log.e("Firestore", "Failed to join game session", it)})
//    }

    fun makeMove(x: Int, y: Int) {
        val sessionId = _gameSession.value.sessionId ?: return
        val currentSession = _gameSession.value
        val currentTurn = currentSession.currentTurn
        val tile = if (currentTurn == "host") currentSession.hostTile[0] else currentSession.opponentTile[0]

        // Convert flat list to nested array
        val boardArray = currentSession.board.toNestedArray()

        val isValidMove = gameLogic.isValidMove(boardArray, tile, x, y)

        if (isValidMove != false) {
            gameLogic.makeMove(boardArray, tile, x, y)

            // Convert nested array back to flat list
            val updatedBoard = boardArray.toFlatList()
            val nextTurn = if (currentTurn == "host") "opponent" else "host"

            db.collection("sessions").document(sessionId)
                .update(
                    "board", updatedBoard,
                    "currentTurn", nextTurn
                )
                .addOnSuccessListener {
                    Log.d("Multiplayer", "Move made successfully")
                }
                .addOnFailureListener { Log.e("Multiplayer", "Failed to make move", it) }
        }
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
            nestedArray[row][col] = this[i][0] // Convert String to Char
        }
        return nestedArray
    }

    // Convert a nested array (8x8 grid) to a flat list
    private fun Array<MutableList<Char>>.toFlatList(): List<String> {
        val flatList = mutableListOf<String>()
        for (row in this) {
            for (cell in row) {
                flatList.add(cell.toString()) // Convert Char to String
            }
        }
        return flatList
    }

    private fun Array<MutableList<Char>>.toListOfListsString(): List<List<String>> {
        return this.map { row -> row.map { it.toString() } }
    }

    private fun List<List<String>>.toArrayOfMutableListsChar(): Array<MutableList<Char>> {
        return this.map { row -> row.map { it[0] }.toMutableList() }.toTypedArray()
    }
}