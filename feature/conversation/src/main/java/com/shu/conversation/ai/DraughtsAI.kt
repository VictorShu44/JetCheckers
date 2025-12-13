package com.shu.conversation.ai

import com.shu.conversation.logic.DraughtsMoveGenerator
import com.shu.conversation.logic.Move
import com.shu.conversation.logic.Piece
import com.shu.conversation.logic.PieceType
import com.shu.conversation.logic.Player
import com.shu.conversation.logic.Position

// AI с разными уровнями сложности
open class DraughtsAI2(
    private val maxDepth: Int = 3,  // глубина поиска (1-7)
    private val randomness: Double = 0.0  // уровень случайности (0.0-1.0)
) {
    val moveGenerator = DraughtsMoveGenerator()
    private val evaluator = PositionEvaluator()

    // Веса для разных уровней сложности
    private val easyWeights = PositionEvaluator.Weights(
        pieceValue = 100,
        kingValue = 250,
        centralPosition = 5,
        mobility = 2,
        safePosition = 10,
        promotionChance = 15
    )

    private val mediumWeights = PositionEvaluator.Weights(
        pieceValue = 100,
        kingValue = 300,
        centralPosition = 10,
        mobility = 5,
        safePosition = 15,
        promotionChance = 20
    )

    private val hardWeights = PositionEvaluator.Weights(
        pieceValue = 100,
        kingValue = 350,
        centralPosition = 15,
        mobility = 8,
        safePosition = 20,
        promotionChance = 25
    )

    // Выбор лучшего хода
    open fun findBestMove(board: Array<Array<Piece?>>, player: Player): Move {
        val moves = moveGenerator.generateAllMoves(board, player)

        // Если есть только один ход, возвращаем его
        if (moves.size == 1) return moves[0]

        // Применяем случайность для "тупого" AI
        if (randomness > 0.0 && Math.random() < randomness) {
            return moves.random()
        }

        val weights = when (maxDepth) {
            1 -> easyWeights
            2 -> mediumWeights
            else -> hardWeights
        }

        var bestMove = moves[0]
        var bestValue = Int.MIN_VALUE

        // Параллельная оценка ходов для скорости
        for (move in moves) {
            val newBoard = applyMove(board, move)
            val value = minimax(
                newBoard,
                maxDepth - 1,
                Int.MIN_VALUE,
                Int.MAX_VALUE,
                false,
                player,
                weights
            )

            if (value > bestValue) {
                bestValue = value
                bestMove = move
            }
        }

        return bestMove
    }

    // Алгоритм минимакс с альфа-бета отсечением
    private fun minimax(
        board: Array<Array<Piece?>>,
        depth: Int,
        alpha: Int,
        beta: Int,
        maximizingPlayer: Boolean,
        originalPlayer: Player,
        weights: PositionEvaluator.Weights
    ): Int {
        val currentPlayer = if (maximizingPlayer) originalPlayer else originalPlayer.opponent()

        // Терминальные условия
        if (depth == 0 || isGameOver(board, currentPlayer)) {
            return evaluator.evaluate(board, originalPlayer, weights)
        }

        val moves = moveGenerator.generateAllMoves(board, currentPlayer)

        // Если нет ходов - это терминальное состояние
        if (moves.isEmpty()) {
            return if (currentPlayer == originalPlayer) {
                Int.MIN_VALUE + depth  // проигрыш (чем быстрее, тем лучше)
            } else {
                Int.MAX_VALUE - depth  // выигрыш
            }
        }

        if (maximizingPlayer) {
            var maxEval = Int.MIN_VALUE
            var currentAlpha = alpha

            for (move in moves) {
                val newBoard = applyMove(board, move)
                val eval =
                    minimax(newBoard, depth - 1, currentAlpha, beta, false, originalPlayer, weights)
                maxEval = maxOf(maxEval, eval)
                currentAlpha = maxOf(currentAlpha, eval)

                if (beta <= currentAlpha) break  // отсечение
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            var currentBeta = beta

            for (move in moves) {
                val newBoard = applyMove(board, move)
                val eval =
                    minimax(newBoard, depth - 1, alpha, currentBeta, true, originalPlayer, weights)
                minEval = minOf(minEval, eval)
                currentBeta = minOf(currentBeta, eval)

                if (currentBeta <= alpha) break  // отсечение
            }
            return minEval
        }
    }

    // Проверка окончания игры
    private fun isGameOver(board: Array<Array<Piece?>>, player: Player): Boolean {
        // Проверяем, есть ли у игрока шашки
        var hasPieces = false
        for (row in 0..7) {
            for (col in 0..7) {
                if (board[row][col]?.owner == player) {
                    hasPieces = true
                    break
                }
            }
            if (hasPieces) break
        }

        if (!hasPieces) return true

        // Проверяем, есть ли у игрока возможные ходы
        return moveGenerator.generateAllMoves(board, player).isEmpty()
    }

    // Применение хода к доске
    private fun applyMove(board: Array<Array<Piece?>>, move: Move): Array<Array<Piece?>> {
        val newBoard = deepCopyBoard(board)
        val piece = board[move.from.row][move.from.col]!!

        // Перемещаем шашку
        newBoard[move.from.row][move.from.col] = null

        // Если шашка превращается в дамку
        val movedPiece = if (move.becomesKing) {
            piece.copy(type = PieceType.KING, position = move.to)
        } else {
            piece.copy(position = move.to)
        }
        newBoard[move.to.row][move.to.col] = movedPiece

        // Удаляем съеденные шашки
        for (captured in move.captured) {
            newBoard[captured.row][captured.col] = null
        }

        return newBoard
    }

    private fun deepCopyBoard(board: Array<Array<Piece?>>): Array<Array<Piece?>> {
        return Array(8) { row ->
            Array(8) { col ->
                board[row][col]?.copy(position = Position(row, col))
            }
        }
    }

    // Утилита для получения противника
    private fun Player.opponent(): Player = when (this) {
        Player.WHITE -> Player.BLACK
        Player.BLACK -> Player.WHITE
    }
}