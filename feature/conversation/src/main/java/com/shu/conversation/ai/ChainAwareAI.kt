package com.shu.conversation.ai

import com.shu.conversation.logic.Move
import com.shu.conversation.logic.Piece
import com.shu.conversation.logic.Player
import com.shu.conversation.logic.Position

class ChainAwareAI(override val maxDepth: Int = 3) : DraughtsAI(maxDepth) {

        // Переопределяем оценку хода для учета цепочек
        fun evaluateMove(board: Array<Array<Piece?>>, move: Move, player: Player): Int {
            var score = evaluateMove(board, move, player)

            // Бонус за длинные цепочки взятий
            if (move.captured.size > 1) {
                // Экспоненциальный бонус за каждую дополнительную шашку в цепочке
                score += (move.captured.size - 1) *

                        150

                // Дополнительный бонус за превращение в дамку в ходе цепочки
                if (move.becomesKing) {
                    score += 100
                }
            }

           /* // Бонус за безопасные цепочки (когда не оставляем шашку под боем)
            if (move.intermediatePositions.isNotEmpty()) {
                val simulatedBoard = applyMove(board, move)
                if (!isPieceUnderAttack(simulatedBoard, move.to, player)) {
                    score += 50
                }

            }*/

            return score
        }

        // Проверка, находится ли шашка под атакой
        private fun isPieceUnderAttack(
            board: Array<Array<Piece?>>,
            position: Position,
            player: Player
        ): Boolean {
            val opponent = when (player) {
                Player.WHITE -> Player.BLACK
                Player.BLACK -> Player.WHITE
            }

            // Проверяем все шашки противника
            for (row in 0..7) {
                for (col in 0..7) {

                    val piece = board[row][col]
                    if (piece?.owner == opponent) {
                       // val moves = generateMovesForPiece(board, piece)
                       /* for (move in moves) {
                            if (move.captured.contains(position)) {
                                return true
                            }
                        }*/
                    }
                }
            }

            return false
        }
    }