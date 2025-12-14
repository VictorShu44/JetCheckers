package com.shu.conversation.logic

// Файл: feture/checkers/ui/CheckersScreen.kt

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shu.jetnews.core.ui.components.AppBar

@Composable
fun CheckersScreen() {
    var game = remember { CheckersGame() }
    val gameState = game.gameState.value

    LaunchedEffect(gameState.currentPlayer) {
        if (gameState.currentPlayer == Player.BLACK) {
            Log.d("mov", "player ${gameState.currentPlayer} one")
            game.moveBlack()
        }
    }
    LaunchedEffect(gameState.selectedPiece) {
        gameState.selectedPiece?.let {

            Log.d("mov", "player ${gameState.currentPlayer} two")
            game.moveBlackTo()
        }
    }
    Scaffold(
        topBar = {
                AppBar(title = "Шашки", actions = {
                    IconButton(
                        onClick = {
                            game = CheckersGame()
                            game.restart() }
                    ) {
                        Icon(Icons.Filled.Refresh, "Trigger Refresh")
                    }
                    IconButton(
                        onClick = {  }
                    ) {
                        Icon(Icons.Filled.Menu, "Trigger Refresh")
                    }
                })

        },
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(it). padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Шашки",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            BoardView(
                board = gameState.board,
                selectedPiece = gameState.selectedPiece,
                onCellClick = { row, col -> game.handleCellClick(row, col) }
            )

            Text(
                text = gameState.statusMessage,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun BoardView(
    board: Board,
    selectedPiece: Pair<Int, Int>?,
    onCellClick: (row: Int, col: Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Делаем доску квадратной
            .border(2.dp, Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until board.size) {
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    for (col in 0 until board.size) {
                        val isLight = (row + col) % 2 == 0
                        val isSelected =
                            selectedPiece?.let { it.first == row && it.second == col } ?: false

                        val cellColor = when {
                            isSelected -> Color(0xFF66A3FF) // Цвет выделенной клетки
                            isLight -> Color(0xFFF0D9B5)  // Светлая клетка
                            else -> Color(0xFFB58863)     // Темная клетка
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(cellColor)
                                .clickable { onCellClick(row, col) },
                            contentAlignment = Alignment.Center
                        ) {
                            board.getPiece(Position(row, col))?.let { piece ->
                                PieceView(piece)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieceView(piece: Piece) {
    val pieceColor = if (piece.owner == Player.WHITE) Color.White else Color.Black
    val borderColor = if (piece.owner == Player.WHITE) Color.Black else Color.Gray

    Box(
        modifier = Modifier
            .fillMaxSize(0.8f)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(pieceColor)
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (piece.type == PieceType.KING) {
            // Рисуем корону для дамки
            Text(
                text = "👑",
                color = if (piece.owner == Player.WHITE) Color.Black else Color.White,
                fontSize = 18.sp
            )
        }
    }
}
