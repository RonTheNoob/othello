package com.example.othello

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.othello.ui.theme.OthelloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OthelloTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OthelloBoard()
                }
            }
        }
    }
}

@Composable
fun OthelloBoard() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Othello",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / 8 // Divides the board to 8

                // This draws the vertical liness
                for (i in 0..8) {
                    drawLine(
                        color = Color.Black,
                        start = Offset(i * cellSize, 0f),
                        end = Offset(i * cellSize, size.height),
                        strokeWidth = 2f
                    )
                }

                // This draws the horizontal liness yes
                for (i in 0..8) {
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, i * cellSize),
                        end = Offset(size.width, i * cellSize),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OthelloBoardPreview() {
    OthelloTheme {
            OthelloBoard()
    }
}