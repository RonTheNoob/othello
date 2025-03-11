package com.example.othello.modules.auth.data

import com.google.firebase.auth.FirebaseUser

sealed class AuthResult {
    data class Success(val user: FirebaseUser, val username: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}