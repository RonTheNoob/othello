package com.example.othello.modules.home.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LeaderboardViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<Leaderboard>>(emptyList())
    val users: StateFlow<List<Leaderboard>> get() = _users

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    init {
        fetchLeaderboard()
    }

    private fun fetchLeaderboard() {
        viewModelScope.launch {
            _loading.value = true // Set loading to true
            _users.value = getLeaderboardData()
            _loading.value = false // Set loading to false after data is fetched
        }
    }

    private suspend fun getLeaderboardData(): List<Leaderboard> {
        val db = FirebaseFirestore.getInstance()
        return try {
            val snapshot = db.collection("Users")
                .orderBy("wins", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.map { doc ->
                Leaderboard(
                    username = doc.getString("username") ?: "Unknown User",
                    wins = doc.getLong("wins")?.toInt() ?: 0,
                    losses = doc.getLong("losses")?.toInt() ?: 0,
                    draws = doc.getLong("draws")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}