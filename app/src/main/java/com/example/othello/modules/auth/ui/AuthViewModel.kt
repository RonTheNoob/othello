package com.example.othello.modules.auth.ui

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.othello.modules.auth.data.AuthRepositoryInterface
import com.example.othello.modules.auth.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepositoryInterface) : ViewModel() {
    // Expose the screen UI state
    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    init {
        val currentUser = getAuthenticatedUser()
        if (currentUser != null) {
            _uiState.update { currentState
                ->
                currentState.copy(
                    email = currentUser.email,
                )
            }
        }
    }

    fun logout() {
        // Sign out the user
        authRepository.logout()

        _uiState.update { currentState ->
            currentState.copy(
                email = null,
            )
        }
    }

    fun registerUser(username: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        // Register the user with the provided email and password
        if (!isValidEmail(email)) {
            _uiState.update { it.copy(errorText = "Invalid email format") }
            onResult(false)
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorText = "Password must be at least 6 characters") }
            onResult(false)
            return
        }

        viewModelScope.launch {
            authRepository.registerUser(username, email, password) { errorMessage ->
                if (errorMessage == null) {
                    // Success case
                    _uiState.update { it.copy(errorText = null) }
                    onResult(true)
                } else {
                    // Error case
                    _uiState.update { it.copy(errorText = errorMessage) }
                    onResult(false)
                }
            }
        }
    }

//    fun signInUserWithEmailAndPassword(email: String, password: String) {
//        // Sign in the user with the provided email and password
//        Firebase.auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val currentUser = task.result.user
//
//                    _uiState.update { currentState
//                        ->
//                        currentState.copy(
//                            email = currentUser?.email,
//                        )
//                    }
//                } else {
//                    Log.w("FIREBASE_REGISTER", "signInWithEmailAndPassword:failure")
//                }
//            }
//    }

    fun signInWithUsername(username: String, password: String) {
        _uiState.update { it.copy(loading = true, errorText = null) } // Start loading
        viewModelScope.launch {
            val errorMessage = authRepository.signInWithUsername(username, password)
            if (errorMessage == null) {
                // Login successful
                val user = authRepository.getCurrentUser()
                _uiState.update {
                    it.copy(
                        email = user?.email ?: username,
                        errorText = null,
                        loading = false // Stop loading
                    )
                }
            } else {
                // Login failed
                _uiState.update {
                    it.copy(
                        errorText = errorMessage,
                        loading = false // Stop loading
                    )
                }
            }
        }
    }

    fun signInWithGoogle(appContext: Context) {
        // Sign in the user with Google
        viewModelScope.launch {
            authRepository.signInWithGoogle(appContext) { currentUser ->
                if (currentUser != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            email = currentUser.email,
                            showGoogleSignInDialog = true,
                            googleUsername = currentUser.displayName
                                ?: currentUser.email?.substringBefore("@")
                        )
                    }
                }
            }
        }
    }

    fun getCurrentUser(): User? {
        return getAuthenticatedUser()
    }

    fun getAuthenticatedUser(): User? {
        val firebaseUser = authRepository.getCurrentUser()

        if (firebaseUser == null) {
            return null
        }

        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: ""
        )
    }


    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun clearGoogleSignInDialog() {
        _uiState.update { it.copy(
            showGoogleSignInDialog = false,
            googleUsername = null
        ) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorText = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(errorText = message) }
    }

}