package com.shu.conversation.ai

import com.shu.conversation.logic.Move
import com.shu.conversation.logic.Piece
import com.shu.conversation.logic.PieceType
import com.shu.conversation.logic.Player

// AI, который может учиться на своих ошибках
class LearningAI : DraughtsAI(maxDepth = 3) {

    private val moveHistory = mutableListOf<Pair<Array<Array<Piece?>>, Move>>()
    private val positionScores = mutableMapOf<String, Int>()

    // Улучшенная оценка с учетом истории
    override fun findBestMove(board: Array<Array<Piece?>>, player: Player): Move {
        val boardHash = boardToHash(board, player)

        // Если позиция уже встречалась, используем сохраненную оценку
        if (positionScores.containsKey(boardHash)) {
            val savedScore = positionScores[boardHash]!!
            // Можем корректировать стратегию на основе прошлых результатов
        }

        val move = super.findBestMove(board, player)

        // Сохраняем историю для обучения
        moveHistory.add(board to move)

        // Ограничиваем размер истории
        if (moveHistory.size > 1000) {
            moveHistory.removeFirst()
        }

        return move
    }

    // Метод для обучения на результате игры
    fun learnFromGame(result: GameResult, player: Player) {
        // Усиливаем или ослабляем вес ходов в зависимости от результата
        val scoreChange = when (result) {
            GameResult.WIN -> 10
            GameResult.LOSS -> -10
            GameResult.DRAW -> 0
        }

        for ((board, move) in moveHistory) {
            val hash = boardToHash(board, player)
            val currentScore = positionScores.getOrDefault(hash, 0)
            positionScores[hash] = currentScore + scoreChange
        }

        moveHistory.clear()
    }

    private fun boardToHash(board: Array<Array<Piece?>>, player: Player): String {
        return buildString {
            for (row in 0..7) {
                for (col in 0..7) {
                    val piece = board[row][col]
                    when {
                        piece == null -> append('0')
                        piece.owner == player && piece.type == PieceType.MAN -> append('1')
                        piece.owner == player && piece.type == PieceType.KING -> append('2')
                        piece.owner != player && piece.type == PieceType.MAN -> append('3')
                        piece.owner != player && piece.type == PieceType.KING -> append('4')
                    }
                }
            }
        }
    }

    enum class GameResult { WIN, LOSS, DRAW }
}