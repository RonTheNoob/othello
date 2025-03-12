package com.example.othello.modules.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.othello.modules.auth.ui.AuthViewModel

@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel) {
    val authState by authViewModel.uiState.collectAsState()
    val authEmail = authState.email

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1B1B)) // Dark Othello Board Theme
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Othello",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        val buttonModifier = Modifier
            .fillMaxWidth()
            .height(50.dp) // Rectangle buttons
            .padding(vertical = 4.dp)

        val buttonShape = RoundedCornerShape(8.dp) // Slightly rounded for a sleek look

        Button(
            modifier = buttonModifier,
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), // Othello Green
            onClick = { navController.navigate("singleplayer") }
        ) {
            Text(text = "Singleplayer", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = buttonModifier,
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            onClick = { navController.navigate("sessions") }
        ) {
            Text(text = "Multiplayer", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = buttonModifier,
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            onClick = { navController.navigate("leaderboard") }
        ) {
            Text(text = "Leaderboard", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = buttonModifier,
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            onClick = { navController.navigate("tutorial") }
        ) {
            Text(text = "Tutorial", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = buttonModifier,
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            onClick = { navController.navigate("credits") }
        ) {
            Text(text = "Credits", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            modifier = buttonModifier,
            shape = buttonShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(2.dp, Color.White),
            onClick = {
                authViewModel.logout()
                navController.navigate("login")
            }
        ) {
            Text(text = "Logout")
        }
    }
}
