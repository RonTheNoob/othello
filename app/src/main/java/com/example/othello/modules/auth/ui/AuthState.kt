package com.example.othello.modules.auth.ui

import com.example.othello.modules.auth.data.User

data class AuthState(
    val email: String? = null,
    val errorText: String? = null,
    val showGoogleSignInDialog: Boolean = false,
    val googleUsername: String? = null,
    val currentUser: User? = null
)
