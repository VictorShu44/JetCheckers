package com.shu.conversation.ai

/*
fun main() {
    // Создаем тестовую позицию с возможностью взятия 2 шашек
    val board = Array(8) { row ->
        Array<Piece?>(8) { col ->
            when {
                // Черная шашка
                row == 2 && col == 2 -> Piece(Player.BLACK, PieceType.REGULAR, Position(row, col))
                // Белые шашки для взятия
                row == 3 && col == 3 -> Piece(Player.WHITE, PieceType.REGULAR, Position(row, col))
                row == 5 && col == 5 -> Piece(Player.WHITE, PieceType.REGULAR, Position(row, col))
                // Пустая доска для второй белой шашки
                row == 3 && col == 1 ->

                    Piece(Player.WHITE, PieceType.REGULAR, Position(row, col))
                row == 1 && col == 3 -> Piece(Player.WHITE, PieceType.REGULAR, Position(row, col))
                else -> null
            }
        }
    }

    println("Исходная позиция:")
    printBoard(board)

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
        printBoard(newBoard)
    }

    // Выбор хода с максимальным количеством взятий
    val bestMove = blackMoves.maxByOrNull { it.captured.size }
    println("\nЛучший ход (максимальное количество взятий):")
    bestMove?.let { move ->
        println("  Срубает ${move.captured.size} шашек")
        println("  Путь: ${move.fullPath.joinToString(" -> ")}")
    }

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
                piece.player == Player.WHITE && piece.type == PieceType.REGULAR -> "○"
                piece.player == Player.BLACK && piece.type == PieceType.REGULAR -> "●"
                piece.player == Player.WHITE && piece.type == PieceType.KING -> "ⓦ"
                piece.player == Player.BLACK && piece.type == PieceType.KING -> "ⓑ"

                else -> "?"
            }
            print("$symbol ")
        }
        println("${8 - row}")
    }
    println("   a b c d e f g h")
}
*/
