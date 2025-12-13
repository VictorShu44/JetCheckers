package com.shu.conversation.ai

import com.shu.conversation.logic.Piece
import com.shu.conversation.logic.PieceType
import com.shu.conversation.logic.Player

// Класс для оценки позиции
class PositionEvaluator {

    // Веса для разных факторов
    data class Weights(
        val pieceValue: Int = 100,
        val kingValue: Int = 300,
        val centralPosition: Int = 10,
        val mobility: Int = 5,
        val safePosition: Int = 15,
        val promotionChance: Int = 20
    )

    // Оценка позиции для игрока
    fun evaluate(board: Array<Array<Piece?>>, player: Player, weights: Weights = Weights()): Int {
        var score = 0

        // Подсчет материала
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = board[row][col] ?: continue

                val value = when {
                    piece.owner == player && piece.type == PieceType.MAN -> weights.pieceValue
                    piece.owner == player && piece.type == PieceType.KING -> weights.kingValue
                    piece.owner != player && piece.type == PieceType.MAN -> -weights.pieceValue
                    piece.owner != player && piece.type == PieceType.KING -> -weights.kingValue
                    else -> 0
                }
                score += value

                // Бонусы за позицию
                if (piece.owner == player) {
                    // Центральные позиции
                    if (row in 2..5 && col in 2..5) {
                        score += weights.centralPosition
                    }

                    // Безопасные позиции (у края доски)
                    if (col == 0 || col == 7 || row == 0 || row == 7) {
                        score += weights.safePosition
                    }

                    // Близость к превращению
                    if (piece.type == PieceType.MAN) {
                        val distanceToPromotion = when (piece.owner) {
                            Player.WHITE -> row  // белым нужно дойти до 0 ряда
                            Player.BLACK -> 7 - row  // черным до 7 ряда
                        }
                        score += (4 - distanceToPromotion) * weights.promotionChance
                    }
                }
            }
        }

        return score
    }
}