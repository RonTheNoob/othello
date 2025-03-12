package com.example.othello.modules.home.data

data class Leaderboard(
    val username: String = "",
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0
)
