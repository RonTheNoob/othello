package com.example.othello.modules.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val appContext = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()
    var showGoogleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState.email != null) {
            if (authState.showGoogleSignInDialog) {
                showGoogleDialog = true
            } else {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }

    if (showGoogleDialog) {
        AlertDialog(
            onDismissRequest = {
                showGoogleDialog = false
                authViewModel.clearGoogleSignInDialog()
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            },
            title = {
                Text("Google Sign-In Successful")
            },
            text = {
                Column {
                    Text("Username: ${authState.googleUsername ?: "N/A"}")
                }
            },
            confirmButton = {
//                HomeScreenButton("OK") {
//                    showGoogleDialog = false
//                    authViewModel.clearGoogleSignInDialog()
//                    navController.navigate("home") {
//                        popUpTo("login") { inclusive = true }
//                    }
//                }
                Button(
                    onClick = {
                        showGoogleDialog = false
                        authViewModel.clearGoogleSignInDialog()
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                ) {
                    Text(text = "OK")
                }
            }
        )
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome to Othello!", style = MaterialTheme.typography.headlineMedium, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Sign in to continue", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                authViewModel.signInUserWithEmailAndPassword(username, password)
            }) {
                Text(text = "Login")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                authViewModel.signInWithGoogle(appContext)
            }) {
                Text(text = "Sign in with Google")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = {
                navController.navigate("register")
            }) {
                Text(text = "Don't have an account? Sign Up")
            }
        }
    }
}