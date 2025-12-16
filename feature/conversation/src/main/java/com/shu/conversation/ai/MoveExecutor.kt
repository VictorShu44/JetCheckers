package com.shu.conversation.ai

import com.shu.conversation.logic.Move
import com.shu.conversation.logic.Piece
import com.shu.conversation.logic.PieceType
import com.shu.conversation.logic.Position

//Исполнитель ходов с поддержкой цепочек
class MoveExecutor {
    // Применение хода к доске с поддержкой цепочек взятий
    fun executeMove(board: Array<Array<Piece?>>, move: Move): Array<Array<Piece?>> {
        val newBoard = deepCopyBoard(board)
        val piece = board[move.from.row][move.from.col]!!

        // Убираем шашку с исходной позиции
        newBoard[move.from.row][move.from.col] = null

        // Если есть промежуточные позиции, это цепочка взятий

        if (move.intermediatePositions.isNotEmpty()) {
            // Для цепочек - шашка проходит через все промежуточные клетки
            // но в итоге оказывается только на конечной позиции
            val finalPiece = if (move.becomesKing) {
                piece.copy(type = PieceType.KING, position = move.to)
            } else {
                piece.copy(position = move.to)
            }
            newBoard[move.to.row][move.to.col] = finalPiece
        } else {
            // Простой ход или одиночное взятие
            val movedPiece = if (move.becomesKing) {

                piece.copy(type = PieceType.KING, position = move.to)
            } else {
                piece.copy(position = move.to)
            }
            newBoard[move.to.row][move.to.col] = movedPiece
        }

        // Убираем все съеденные шашки
        for (captured in move.captured) {
            newBoard[captured.row][captured.col] = null
        }

        return newBoard
    }

    // Пошаговое выполнение цепочки взятий (для анимации)
    fun executeChainStepByStep(board:

                               Array<Array<Piece?>>, move: Move): List<Array<Array<Piece?>>> {
        val steps = mutableListOf<Array<Array<Piece?>>>()
        var currentBoard = deepCopyBoard(board)
        val piece = board[move.from.row][move.from.col]!!

        // Если это цепочка взятий
        if (move.intermediatePositions.isNotEmpty()) {
            val allPositions = listOf(move.from) + move.intermediatePositions + listOf(move.to)
            val capturedByStep = getCapturedByStep(move, allPositions)

            for ((index, position) in allPositions.withIndex()) {

                if (index == 0) continue // Пропускаем начальную позицию

                val prevPosition = allPositions[index - 1]
                val currentPiece = if (index == allPositions.size - 1 && move.becomesKing) {
                    piece.copy(type = PieceType.KING, position = position)
                } else {
                    piece.copy(position = position)
                }

                // Перемещаем шашку
                currentBoard[prevPosition.row][prevPosition.col] = null
                currentBoard[position.row][position.col] = currentPiece

                // Убираем съеденную на этом шаге шашку
                        capturedByStep[index - 1]?.let { capturedPos ->
                            currentBoard[capturedPos.row][capturedPos.col] = null
                        }

                steps.add(deepCopyBoard(currentBoard))
            }
        } else {
            // Простой ход или одиночное взятие
            steps.add(executeMove(board, move))
        }

        return steps
    }

    // Определение, какая шашка съедается на каждом шаге цепочки
    private fun getCapturedByStep(move: Move, allPositions: List<Position>): Map<Int, Position> {
        val capturedByStep = mutableMapOf<Int, Position>()

        if (move.captured.size >= 2 && move.intermediatePositions.isNotEmpty()) {
            // Для каждого прыжка в цепочке определяем съедаемую шашку
            for (i in 0 until move.intermediatePositions.size) {
                if (i < move.captured.size) {
                    capturedByStep[i] = move.captured[i]
                }
            }
        } else if (move.captured.isNotEmpty()) {

            // Одиночное взятие
            capturedByStep[0] = move.captured[0]
        }

        return capturedByStep
    }

    private fun deepCopyBoard(board: Array<Array<Piece?>>): Array<Array<Piece?>> {
        return Array(8) { row ->
            Array(8) { col ->
                board[row][col]?.copy(position = Position(row, col))
            }
        }
    }
}