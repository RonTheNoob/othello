package com.example.othello.modules.game.multiplayer.data
//
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import kotlinx.coroutines.tasks.await
//
//
//class GameRepository : GameRepositoryInterface{
//    private val db = Firebase.firestore
//    private val gameSessions = db.collection("sessions")
//
//    override suspend fun createGameSession(hostId: String, hostName: String?): GameSession? {
//        // create game session
//        val gameSession = GameSession(
//            hostId = hostId,
//            hostName = hostName
//        )
//        val docRef = gameSessions.add(gameSession).await()
//
//        val snapshot = docRef.get().await()
//        if (snapshot.exists()) {
//            // convert and serialize to game session
//            var gameSession = snapshot.toObject(GameSession::class.java)
//            gameSession = gameSession?.copy(id = snapshot.id)
//
//            return gameSession
//        }
//
//        return null
//    }
//
//    override fun readGameSession(
//        gameId: String,
//        readOnce: Boolean,
//        onRead: (GameSession?) -> Unit
//    ) {
//        val docRef = gameSessions.document(gameId)
//
//        if (!readOnce) {
//            docRef.addSnapshotListener { snapshot, _ ->
//                if (snapshot != null && snapshot.exists()) {
//                    var gameSession = snapshot.toObject(GameSession::class.java)
//                    gameSession = gameSession?.copy(id = snapshot.id)
//
//                    onRead(gameSession)
//                } else {
//                    onRead(null)
//                }
//            }
//            return
//        }
//
//        docRef.get().addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                var gameSession = task.result.toObject(GameSession::class.java)
//                gameSession = gameSession?.copy(id = task.result.id)
//
//                onRead(gameSession)
//            }
//        }
//    }
//
//    override fun updateGameSession(
//        gameId: String,
//        updated: GameSession
//    ) {
//        gameSessions.document(gameId).set(updated.toMap())
//    }
//
//    override fun readOpenGameSessions(currentUserId: String, onRead: (List<GameSession>) -> Unit) {
//        gameSessions.whereNotEqualTo("creatorId", currentUserId)
//            .addSnapshotListener { snapshot, _ ->
//                if (snapshot != null) {
//                    val sessionDocs = snapshot.documents
//                    var sessions = emptyList<GameSession>()
//
//                    if (sessionDocs.isEmpty()) {
//                        onRead(sessions)
//                        return@addSnapshotListener
//                    }
//
//                    // only display game session that doesn't have any opponent
//                    sessions = sessionDocs.filter { it.getString("opponentId") == null }
//                        .map {
//                            var gameSession = it.toObject(GameSession::class.java)
//                            gameSession = gameSession?.copy(id = it.id)
//                            gameSession as GameSession
//                        }
//
//                    onRead(sessions)
//                }
//            }
//    }
//}