package com.shu.conversation.ai

import com.shu.conversation.logic.Piece
import com.shu.conversation.logic.PieceType
import com.shu.conversation.logic.Player
import com.shu.conversation.logic.Position

fun main() {
    // Создаем тестовую позицию
   /* val board = Array(8) { row ->
        Array<Piece?>(8) { col ->
            when {
                row == 2 && col == 3 -> Piece(Player.BLACK, PieceType.MAN, Position(row, col))
                row == 5 && col == 4 -> Piece(
                    Player.WHITE, PieceType.MAN,

                    Position(row, col)
                )

                row == 5 && col == 2 -> Piece(Player.WHITE, PieceType.MAN, Position(row, col))
                else -> null
            }
        }
    }

    println("Текущая позиция:")
    printBoard(board)

    val player = Player.WHITE

    // Тестируем разные уровни AI
    val difficulties = AIFactory.Difficulty.entries.toTypedArray()

    for (difficulty in difficulties) {
        val ai = AIFactory.createAI(difficulty)


        println("\n${"-".repeat(40)}")
        println("Уровень: $difficulty")
        println("Глубина поиска: ${ai.maxDepth}")

        // Поиск с фиксированной глубиной
        val startTime = System.currentTimeMillis()
        val move = ai.findBestMove(board, player)
        val timeTaken = System.currentTimeMillis() - startTime

        println("Лучший ход: ${move.from} -> ${move.to}")
        if (move.captured.isNotEmpty()) {
            println("Взятие шашек: ${move.captured.joinToString()}")
        }
        if (move.becomesKing) {
            println("Превращение в дамку!")

        }
        println("Время расчета: ${timeTaken}мс")

        // Поиск с ограничением по времени
        if (difficulty.ordinal >= AIFactory.Difficulty.MEDIUM.ordinal) {
            val timeLimitMove = ai.findBestMoveWithTimeLimit(board, player, 1000)
            println("Ход с ограничением времени (1 сек): ${timeLimitMove.from} -> ${timeLimitMove.to}")
        }
    }

    // Тестируем поиск на разной глубине
    println("\n${"=".repeat(40)}")
    println("Тестирование поиска на разной глубине:")


    val hardAI = AIFactory.createAI(AIFactory.Difficulty.HARD)

    for (depth in 1..hardAI.maxDepth) {
        val startTime = System.currentTimeMillis()
        val move = hardAI.findBestMoveAtDepth(board, player, depth)
        val timeTaken = System.currentTimeMillis() - startTime

        println("Глубина $depth: ${move.from} -> ${move.to} (${timeTaken}мс)")
    }*/
}

// Функция для отображения доски

fun printBoard(board: Array<Array<Piece?>>) {
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
