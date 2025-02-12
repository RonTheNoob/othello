package com.example.othello.modules.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.othello.ui.theme.OthelloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OthelloTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    OthelloGrid()
                }
            }
        }
    }
}

@Composable
fun OthelloGrid() {
    // State to track the board (8x8 grid)
    val board = remember { Array(8) { Array(8) { ' ' } } }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Draw the 8x8 grid with row numbers and column letters
        BoardView(board) { x, y ->
            // Handle cell click
            println("Cell clicked: (${'A' + x}, ${y + 1})")
        }
    }
}

@Composable
fun BoardView(
    board: Array<Array<Char>>,
    onCellClick: (Int, Int) -> Unit
) {
    // Dark green background for the board
    val boardColor = Color(0xFF006400) // Dark green color
    val gridColor = Color.Black // Black grid lines

    Column(
        modifier = Modifier
            .background(boardColor)
            .border(BorderStroke(2.dp, gridColor)) // Outer border for the board
    ) {
        // Draw column letters (A-H) at the top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp), // Add padding to align with row numbers
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (x in 0 until 8) {
                Text(
                    text = ('A' + x).toString(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        // Draw the grid with row numbers
        for (y in 0 until 8) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Draw row number (1-8) on the left
                Text(
                    text = (y + 1).toString(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(4.dp)
                        .width(24.dp) // Fixed width for alignment
                )

                // Draw the 8x8 grid
                for (x in 0 until 8) {
                    CellView(
                        cell = board[x][y],
                        onClick = { onCellClick(x, y) },
                        gridColor = gridColor
                    )
                }
            }
        }
    }
}

@Composable
fun CellView(
    cell: Char,
    onClick: () -> Unit,
    gridColor: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp) // Size of each cell
            .border(BorderStroke(1.dp, gridColor)) // Black grid lines
            .clickable(onClick = onClick), // Make the cell clickable
        contentAlignment = Alignment.Center
    ) {
        // Display a circle if the cell is occupied
        if (cell != ' ') {
            Surface(
                shape = CircleShape,
                color = if (cell == 'X') Color.Black else Color.White,
                modifier = Modifier.size(30.dp)
            ) {}
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OthelloGridPreview() {
    OthelloTheme {
        OthelloGrid()
        // add previews of the other functions to check
    }
}