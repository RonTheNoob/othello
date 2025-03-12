package com.example.othello.modules.auth.data

import com.google.firebase.auth.FirebaseUser

interface AuthRepositoryInterface {
    suspend fun signInWithUsername(username: String, password: String, ): String?
    suspend fun signInWithGoogle(context: android.content.Context, onSignIn: (user: FirebaseUser?) -> Unit)
    suspend fun registerUser(username: String, email: String, password: String, onResult: (String?) -> Unit) // Fixed parameter type
//    suspend fun registerUser(username: String, email: String, password: String): AuthResult
    fun logout()
    fun getCurrentUser(): FirebaseUser?
}