package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random


/**
 * Simple single-file Tic-Tac-Toe using Jetpack Compose.
 *
 * Player is 'X' (human). AI is 'O' (simple random move).
 *
 * This file demonstrates:
 *  - Composable functions and state with remember / mutableStateOf / mutableStateListOf
 *  - Basic layout primitives (Column / Row / Box)
 *  - Handling clicks and updating reactive UI
 *  - Simple game logic (win/draw detection)
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent attaches a Compose UI hierarchy to the Activity.
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                TicTacToeScreen()
                }
            }
        }
    }
}

@Composable
fun TicTacToeScreen() {
    // The board is a list of 9 strings: "", "X", or "O".
    // Using a SnapshotStateList so Compose re-composes when elements change.
    val board = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }

    // Track current status text (turns, winner announcement)
    var statusText by remember { mutableStateOf("Your turn — play X") }
    var gameOver by remember { mutableStateOf(false) }

    // When the player makes a move, we:
    //  1) set the square to "X"
    //  2) check for player win/draw
    //  3) if game not over, let AI choose a move and then check again
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Tic Tac Toe",
            style = MaterialTheme.typography.headlineMedium
        )


        Spacer(modifier = Modifier.height(16.dp))

        // 3x3 board
        for (row in 0 until 3) {
            Row {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    Square(
                        value = board[index],
                        enabled = board[index].isEmpty() && !gameOver,
                        onClick = {
                            handlePlayerMove(
                                index = index,
                                board = board,
                                currentGameOver = gameOver,                // <- new arg
                                statusTextSetter = { statusText = it },
                                gameOverSetter = { gameOver = it }
                            )

                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = statusText, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Button(onClick = {
                // Reset game
                for (i in 0 until 9) board[i] = ""
                statusText = "Your turn — play X"
                gameOver = false
            }) {
                Text("Reset")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(onClick = {
                // Quick demo: let AI go first as 'O'
                for (i in 0 until 9) board[i] = ""
                gameOver = false
                val aiMove = pickRandomMove(board)
                if (aiMove != -1) board[aiMove] = "O"
                statusText = "Your turn — play X"
            }) {
                Text("AI starts")
            }
        }
    }
}

fun handlePlayerMove(
    index: Int,
    board: SnapshotStateList<String>,
    currentGameOver: Boolean,
    statusTextSetter: (String) -> Unit,
    gameOverSetter: (Boolean) -> Unit
) {
    // Use the passed-in currentGameOver value instead of referencing a non-existent top-level var.
    if (board[index].isEmpty() && !currentGameOver) {
        board[index] = "X"

        // Check player win or draw
        when {
            checkWinner(board, "X") -> {
                statusTextSetter("You win! 🎉")
                gameOverSetter(true)
            }
            board.none { it.isEmpty() } -> {
                statusTextSetter("It's a draw.")
                gameOverSetter(true)
            }
            else -> {
                // AI move
                statusTextSetter("AI thinking...")
                val aiMove = pickRandomMove(board)
                if (aiMove != -1) {
                    board[aiMove] = "O"
                    when {
                        checkWinner(board, "O") -> {
                            statusTextSetter("AI wins — better luck next time.")
                            gameOverSetter(true)
                        }
                        board.none { it.isEmpty() } -> {
                            statusTextSetter("It's a draw.")
                            gameOverSetter(true)
                        }
                        else -> {
                            statusTextSetter("Your turn — play X")
                            // game still ongoing; don't change gameOver
                        }
                    }
                } else {
                    // No move for AI (shouldn't happen because we checked draw earlier)
                    statusTextSetter("It's a draw.")
                    gameOverSetter(true)
                }
            }
        }
    }
}


@Composable
fun Square(value: String, enabled: Boolean, onClick: () -> Unit) {
    // A clickable square with border and centered text.
    Surface(
        modifier = Modifier
            .size(100.dp)
            .padding(4.dp)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Black),
        tonalElevation = 2.dp     // <-- Material 3 version
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = value,
                fontSize = 36.sp,
                color = when (value) {
                    "X" -> Color(0xFF1B5E20) // greenish for X
                    "O" -> Color(0xFF0D47A1) // bluish for O
                    else -> Color.Black.copy(alpha = 0.6f)
                }
            )
        }
    }
}

/** Returns -1 if no move possible; otherwise index 0..8 for chosen move. */
fun pickRandomMove(board: List<String>): Int {
    val empties = board.mapIndexedNotNull { idx, v -> if (v.isEmpty()) idx else null }
    return if (empties.isEmpty()) -1 else empties[Random.nextInt(empties.size)]
}

/** Simple winner check for the 3x3 board. */
fun checkWinner(board: List<String>, mark: String): Boolean {
    val b = board
    val lines = listOf(
        // rows
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        // cols
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        // diagonals
        listOf(0, 4, 8),
        listOf(2, 4, 6)
    )
    return lines.any { line -> line.all { idx -> b[idx] == mark } }
}
