package com.shu.conversation.logic

import android.util.Log
import kotlin.math.abs

// Типы шашек
//enum class PieceType { REGULAR, KING }

// Цвет шашки
//enum class Player { WHITE, BLACK }

// Клетка доски
data class Position(val row: Int, val col: Int) {
    fun isValid(): Boolean = row in 0..7 && col in 0..7

    override fun toString(): String = "(${row + 1}, ${col + 1})"
}

// Шашка
/*data class Piece(
    val player: Player,
    val type: PieceType = PieceType.MAN,
    val position: Position
)*/

// Ход
data class Move(
    val from: Position,
    val to: Position,
    val captured: List<Position> = emptyList(),
    val becomesKing: Boolean = false
)

// Генератор ходов
class DraughtsMoveGenerator {

    // Направления для простых шашек
    private val regularDirections = mapOf(
        Player.WHITE to listOf(-1 to -1, -1 to 1),  // Белые ходят вверх
        Player.BLACK to listOf(1 to -1, 1 to 1)     // Черные ходят вниз
    )

    // Направления для дамок (все диагонали)
    private val kingDirections = listOf(
        -1 to -1, -1 to 1, 1 to -1, 1 to 1
    )

    // Основная функция генерации всех возможных ходов
    fun generateAllMoves(
        board: Array<Array<Piece?>>,
        player: Player
    ): Pair<List<Move>, List<Move>> {
        val allMoves = mutableListOf<Move>()
        val captureMoves = mutableListOf<Move>()

        // Находим все шашки игрока
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col]
                if (piece?.owner == player) {
                    val moves = generateMovesForPiece(board, piece)
                    // Разделяем взятия и простые ходы
                    moves.forEach { move ->
                        var t = ""
                        move.captured.forEach { p ->
                            t = t + " ${p.row}- ${p.col}"

                        }
                        if (t.isNotEmpty()) {
                            Log.d("mov", "size Capted ${move.captured.size}")
                            Log.d("mov", " list capter $t")
                        }
                        if (move.captured.isNotEmpty()) {
                            captureMoves.add(move)
                        } else {
                            allMoves.add(move)
                        }
                    }
                }
            }
        }

        // По правилам: если есть взятия, обязаны бить
        val capter = if (captureMoves.isNotEmpty()) {
            // Находим максимальное количество взятий
            val maxCaptures = captureMoves.maxOfOrNull { it.captured.size } ?: 0
            captureMoves.filter { it.captured.size == maxCaptures }
        } else {
            emptyList()
        }

        return Pair(capter, allMoves)
    }

    // Генерация ходов для конкретной шашки
    private fun generateMovesForPiece(board: Array<Array<Piece?>>, piece: Piece): List<Move> {
        return when (piece.type) {
            PieceType.MAN -> generateRegularMoves(board, piece)
            PieceType.KING -> generateKingMoves(board, piece)
        }
    }

    // Ходы для простой шашки
    private fun generateRegularMoves(board: Array<Array<Piece?>>, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        val directions = regularDirections[piece.owner]!!

        // Проверяем простые ходы
        for ((dr, dc) in directions) {

            val newRow = piece.position.row + dr
            val newCol = piece.position.col + dc
            // Log.d("mov", " direction $dr , $dc [${piece.position.row} ${piece.position.col}]  [$newRow $newCol] ")
            // Log.d("mov", " before isValidPosition ${isValidPosition(newRow, newCol)} ,  ")
            if (isValidPosition(newRow, newCol) && board[newRow][newCol] == null) {
                val becomesKing = (piece.owner == Player.WHITE && newRow == 0) ||
                        (piece.owner == Player.BLACK && newRow == 7)
                //   Log.d("mov", " direction $dr , $dc [${piece.position.row} ${piece.position.col}]  [$newRow $newCol] ")
                //  Log.d("mov", " isValidPosition ${isValidPosition(newRow, newCol)} , board ${board[newRow][newCol]} ")
                moves.add(Move(piece.position, Position(newRow, newCol), becomesKing = becomesKing))
            }
        }

        // Проверяем взятия (рекурсивно)
        val captureMoves = findCaptureMoves(board, piece, piece.position, mutableSetOf())
        var t = ""
        captureMoves.forEach { mov ->
            t = t + " ${mov.from.row}-${mov.from.col} ${mov.to.row} ${mov.to.col}"

        }
        if (t.isNotEmpty()) {
            Log.d("mov", "capt generato129 $t")
        }

        moves.addAll(captureMoves)


        return moves
    }

    // Ходы для дамки
    private fun generateKingMoves(board: Array<Array<Piece?>>, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        // Простые ходы дамки
        for ((dr, dc) in kingDirections) {
            var row = piece.position.row + dr
            var col = piece.position.col + dc

            while (isValidPosition(row, col) && board[row][col] == null) {
                moves.add(Move(piece.position, Position(row, col)))
                row += dr
                col += dc
            }
        }

        // Взятия дамкой (рекурсивно)
        val captureMoves = findKingCaptureMoves(board, piece, piece.position, mutableSetOf())
        moves.addAll(captureMoves)

        return moves
    }

    // Поиск взятий для простой шашки (рекурсивный)
    private fun findCaptureMoves(
        board: Array<Array<Piece?>>,
        piece: Piece,
        currentPos: Position,
        captured: MutableSet<Position>
    ): List<Move> {
        val moves = mutableListOf<Move>()
        val directions = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
        var textPosition = ""
        for ((dr, dc) in directions) {
            val jumpRow = currentPos.row + dr
            val jumpCol = currentPos.col + dc
            val landRow = currentPos.row + 2 * dr
            val landCol = currentPos.col + 2 * dc

            if (isValidPosition(jumpRow, jumpCol) &&
                isValidPosition(landRow, landCol)
            ) {

                val jumpedPiece = board[jumpRow][jumpCol]
                val landingCell = board[landRow][landCol]


                // Проверяем можно ли взять
                if (jumpedPiece != null &&
                    jumpedPiece.owner != piece.owner &&
                    landingCell == null &&
                    !captured.contains(Position(jumpRow, jumpCol))
                ) {
                    textPosition =
                        textPosition + "[${currentPos.row} - ${currentPos.col} = jump $jumpRow-$jumpCol ${jumpedPiece.position.row} ${jumpedPiece.position.col} cell ${landRow} - ${landCol} ],"
                    /* Log.d("mov", "jumpedPiece ${jumpedPiece.position.row} ${jumpedPiece.position.col} ")
                     Log.d("mov", "landingCell ${landRow} - ${landCol} ")*/


                    val newCaptured = captured.toMutableSet()
                    newCaptured.add(Position(jumpRow, jumpCol))

                    val becomesKing = (piece.owner == Player.WHITE && landRow == 0) ||
                            (piece.owner == Player.BLACK && landRow == 7)

                    // Создаем временную доску для проверки дальнейших взятий
                    val newBoard = deepCopyBoard(board)
                    newBoard[currentPos.row][currentPos.col] = null
                    newBoard[jumpRow][jumpCol] = null
                    newBoard[landRow][landCol] = piece.copy(position = Position(landRow, landCol))

                    // Рекурсивно ищем продолжение взятий
                    val furtherMoves = findCaptureMoves(
                        newBoard,
                        piece.copy(position = Position(landRow, landCol)),
                        Position(landRow, landCol),
                        newCaptured
                    )

                    if (furtherMoves.isEmpty()) {
                        Log.d("mov", "moves.add  ${piece.copy(position = Position(landRow, landCol)).position.row}${piece.copy(position = Position(landRow, landCol)).position.col} ")
                        moves.add(
                            Move(
                                piece.position,
                                piece.copy(position = Position(landRow, landCol)).position,
                                newCaptured.toList(),
                                becomesKing
                            )
                        )
                    } else {
                        Log.d("mov", "moves.addAll ${furtherMoves.size}")
                        moves.addAll(furtherMoves)
                    }
                }
            }
        }
        if (textPosition.isNotEmpty()) {
            Log.d("mov", "textPosition ${textPosition} size = ${moves.size}")
        }
        var ti = ""
        moves.forEach { mov ->
            ti = ti + " ${mov.from.row}-${mov.from.col} ${mov.to.row} ${mov.to.col}"

        }
        if (ti.isNotEmpty()) {
            Log.d("mov", "capt ves recurs $ti")
        }

        return moves
    }

    // Поиск взятий для дамки (рекурсивный)
    private fun findKingCaptureMoves(
        board: Array<Array<Piece?>>,
        piece: Piece,
        currentPos: Position,
        captured: MutableSet<Position>
    ): List<Move> {
        val moves = mutableListOf<Move>()

        for ((dr, dc) in kingDirections) {
            var row = currentPos.row + dr
            var col = currentPos.col + dc
            var foundEnemy: Position? = null

// Ищем вражескую шашку

            while (isValidPosition(row, col)) {
                val cellPiece = board[row][col]

                if (cellPiece != null) {
                    if (cellPiece.owner != piece.owner && !captured.contains(Position(row, col))) {
                        foundEnemy = Position(row, col)
                        break
                    } else {
                        break // Наткнулись на свою шашку или уже битую
                    }
                }
                row += dr
                col += dc
            }

            // Если нашли врага, ищем куда можно встать за ним
            if (foundEnemy != null) {
                var landRow = foundEnemy.row + dr
                var landCol = foundEnemy.col + dc

                while (isValidPosition(landRow, landCol) && board[landRow][landCol] == null) {
                    val newCaptured = captured.toMutableSet()
                    newCaptured.add(foundEnemy)

                    // Создаем временную доску
                    val newBoard = deepCopyBoard(board)
                    newBoard[currentPos.row][currentPos.col] = null
                    newBoard[foundEnemy.row][foundEnemy.col] = null
                    newBoard[landRow][landCol] = piece.copy(position = Position(landRow, landCol))

                    // Рекурсивно ищем продолжение
                    val furtherMoves = findKingCaptureMoves(
                        newBoard,
                        piece.copy(position = Position(landRow, landCol)),
                        Position(landRow, landCol),
                        newCaptured
                    )

                    if (furtherMoves.isEmpty()) {
                        moves.add(
                            Move(
                                piece.position,
                                Position(landRow, landCol),
                                newCaptured.toList()
                            )
                        )
                    } else {
                        moves.addAll(furtherMoves)
                    }

                    landRow += dr
                    landCol += dc
                }
            }
        }

        return moves
    }

    // Вспомогательные функции
    private fun isValidPosition(row: Int, col: Int): Boolean {
        return row in 0..7 && col in 0..7
    }

    private fun deepCopyBoard(board: Array<Array<Piece?>>): Array<Array<Piece?>> {
        return Array(8) { row ->
            Array(8) { col ->
                board[row][col]?.copy(position = Position(row, col))
            }
        }
    }
}

// Пример использования
fun main() {
    // Создаем начальную доску
    /* val board = Array(8) { row ->
         Array<Piece?>(8) { col ->
             when {
                 row < 3 && (row + col) % 2 == 1 -> Piece(
                     Player.BLACK,
                     PieceType.MAN,
                     Position(row, col)
                 )

                 row > 4 && (row + col) % 2 == 1 -> Piece(
                     Player.WHITE,
                     PieceType.MAN,
                     Position(row, col)
                 )

                 else -> null
             }
         }
     }

     val generator = DraughtsMoveGenerator()

     // Генерируем ходы для белых
     println("Ходы белых:")
     val whiteMoves = generator.generateAllMoves(board, Player.WHITE)
     whiteMoves.forEach { move ->
         val captureInfo = if (move.captured.isNotEmpty())
             " (взятие: ${move.captured.joinToString()})" else ""
         println("${move.from} -> ${move.to}$captureInfo")
     }

     println("\nХоды черных:")
     val blackMoves = generator.generateAllMoves(board, Player.BLACK)
     blackMoves.forEach { move ->
         val captureInfo = if (move.captured.isNotEmpty())
             " (взятие: ${move.captured.joinToString()})" else ""
         println("${move.from} -> ${move.to}$captureInfo")
     }*/
}


// Дополнительные утилиты для отображения доски
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