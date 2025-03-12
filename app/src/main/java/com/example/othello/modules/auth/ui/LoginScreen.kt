package com.example.othello.modules.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showWelcomeDialog by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }

    val authState by authViewModel.uiState.collectAsState()
    val appContext = LocalContext.current

    // Handle login success
    LaunchedEffect(authState.email) {
        if (authState.email != null && !authState.loading) {
            showWelcomeDialog = true
        }
    }

    // Handle Google Sign-In Dialog
    LaunchedEffect(authState.showGoogleSignInDialog) {
        if (authState.showGoogleSignInDialog) {
            showGoogleDialog = true
        }
    }

    // Welcome Dialog
    if (showWelcomeDialog) {
        AlertDialog(
            onDismissRequest = {
                showWelcomeDialog = false
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            },
            title = { Text("Welcome Back!") },
            text = { Text("Welcome back, ${authState.email ?: "User"}") },
            confirmButton = {
                Button(onClick = {
                    showWelcomeDialog = false
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Google Sign-In Dialog
    if (showGoogleDialog) {
        AlertDialog(
            onDismissRequest = {
                showGoogleDialog = false
                authViewModel.clearGoogleSignInDialog()
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = { Text("Google Sign-In Successful") },
            text = {
                Column {
                    Text("Username: ${authState.googleUsername ?: "N/A"}")
                }
            },
            confirmButton = {
                Button(onClick = {
                    showGoogleDialog = false
                    authViewModel.clearGoogleSignInDialog()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Othello!",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (authState.loading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            authViewModel.setError("Please fill in all fields")
                        } else {
                            authViewModel.signInWithUsername(username, password)
                        }
                    }
                ) {
                    Text(text = "Login", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    authViewModel.signInWithGoogle(appContext)
                }
            ) {
                Text(text = "Sign in with Google")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { navController.navigate("register") }) {
                Text(
                    text = "Don't have an account? Sign Up",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}