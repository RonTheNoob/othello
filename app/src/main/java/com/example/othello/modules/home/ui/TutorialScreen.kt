package com.example.othello.modules.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.othello.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20)) // Othello Green
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("How to Play Othello", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .background(Color(0xFF1B5E20)), // Dark green background
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Image
                Image(
                    painter = painterResource(id = R.drawable.othello_board),
                    contentDescription = "Othello Board",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Crop
                )

                // Title
                Text(
                    text = "Welcome to Othello!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Subtitle
                Text(
                    text = "Learn the rules and master the game.",
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Game Rules Sections
                TutorialSection(
                    title = "Objective",
                    description = "The goal of Othello is to have the majority of your colored discs (black or white) on the board at the end of the game."
                )

                TutorialSection(
                    title = "Setup",
                    description = "The game starts with 4 discs placed in the center of the board: 2 white and 2 black, arranged in an alternating pattern."
                )

                TutorialSection(
                    title = "How to Play",
                    description = "Players take turns placing one disc of their color on the board. Each new disc must be placed adjacent to an opponent's disc, forming a straight line (horizontal, vertical, or diagonal) of your discs on either side of the opponent's disc(s)."
                )

                TutorialSection(
                    title = "Flipping Discs",
                    description = "When you place a disc, all of the opponent's discs that are in a straight line between your new disc and another disc of your color are flipped to your color."
                )

                TutorialSection(
                    title = "Winning the Game",
                    description = "The game ends when the board is full or neither player can make a valid move. The player with the most discs of their color on the board wins!"
                )

                // Spacer to push content up
                Spacer(modifier = Modifier.height(24.dp))

                // Back Button
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}

@Composable
fun TutorialSection(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Justify
            )
        }
    }
}
