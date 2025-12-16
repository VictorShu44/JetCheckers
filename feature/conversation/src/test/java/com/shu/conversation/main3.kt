package com.shu.conversation

import com.shu.conversation.ai.MoveExecutor
import com.shu.conversation.logic.DraughtsMoveGenerator
import com.shu.conversation.logic.Piece
import com.shu.conversation.logic.PieceType
import com.shu.conversation.logic.Player
import com.shu.conversation.logic.Position
import org.junit.Test

class MoveTest {
    @Test
    fun check_two_catch() {
        // Создаем тестовую позицию с возможностью взятия 2 шашек
        val board = Array(8) { row ->
            Array<Piece?>(8) { col ->
                when {
                    // Черная шашка
                    row == 2 && col == 2 -> Piece(Player.BLACK, PieceType.MAN, Position(row, col))
                    // Белые шашки для взятия
                    row == 3 && col == 3 -> Piece(Player.WHITE, PieceType.MAN, Position(row, col))
                    row == 5 && col == 5 -> Piece(Player.WHITE, PieceType.MAN, Position(row, col))
                    // Пустая доска для второй белой шашки
                    row == 3 && col == 1 ->

                        Piece(Player.WHITE, PieceType.MAN, Position(row, col))

                    row == 1 && col == 3 -> Piece(Player.WHITE, PieceType.MAN, Position(row, col))
                    else -> null
                }
            }
        }

        println("Исходная позиция:")
        printBoard2(board)

        val generator = DraughtsMoveGenerator()
        val blackMoves = generator.generateAllMoves(board, Player.BLACK)

        println("\nВозможные ходы для черных:")

        blackMoves.forEachIndexed { index, move ->
            println("\nХод ${index + 1}:")
            println("  Из: ${move.from}")
            println("  В: ${move.to}")
            if (move.captured.isNotEmpty()) {
                println("  Срубает шашки: ${move.captured.joinToString()}")
            }
            if (move.intermediatePositions.isNotEmpty()) {
                println("  Промежуточные позиции: ${move.intermediatePositions.joinToString()}")
            }
            if (move.becomesKing) {
                println("  Превращается в дамку")
            }


            // Визуализация хода
            val executor = MoveExecutor()
            val newBoard = executor.executeMove(board, move)
            println("  Позиция после хода:")
            printBoard2(newBoard)
        }

        // Выбор хода с максимальным количеством взятий
        val bestMove = blackMoves.maxByOrNull { it.captured.size }
        println("\nЛучший ход (максимальное количество взятий):")
        bestMove?.let { move ->
            println("  Срубает ${move.captured.size} шашек")
            println("  Путь: ${move.intermediatePositions.joinToString(" -> ")}")
        }

    }
}

// Функция для отображения доски
fun printBoard2(board: Array<Array<Piece?>>) {
    println("   a b c d e f g h")
    for (row in 0..7) {
        print("${8 - row} ")
        for (col in 0..7) {
            val piece = board[row][col]
            val symbol = when {
                piece == null -> if ((row + col) % 2 == 0) "□" else "■"
                piece.owner == Player.WHITE && piece.type == PieceType.MAN -> "○"
                piece.owner == Player.BLACK && piece.type == PieceType.MAN -> "●"
                piece.owner == Player.WHITE && piece.type == PieceType.KING -> "ⓦ"
                piece.owner == Player.BLACK && piece.type == PieceType.KING -> "ⓑ"

                else -> "?"
            }
            print("$symbol ")
        }
        println("${8 - row}")
    }
    println("   a b c d e f g h")
}
