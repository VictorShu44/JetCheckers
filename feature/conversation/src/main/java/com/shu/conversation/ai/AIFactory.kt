package com.shu.conversation.ai

import com.shu.conversation.logic.Move
import com.shu.conversation.logic.Piece
import com.shu.conversation.logic.Player

// Фабрика для создания AI разных уровней
object AIFactory2 {

    enum class Difficulty {
        VERY_EASY, EASY, MEDIUM, HARD, VERY_HARD
    }

    fun createAI(difficulty: Difficulty): DraughtsAI {
        return when (difficulty) {
            Difficulty.VERY_EASY -> DraughtsAI(
                maxDepth = 1,
                randomness = 0.7  // 70% случайных ходов
            )

            Difficulty.EASY -> DraughtsAI(
                maxDepth = 2,
                randomness = 0.4  // 40% случайных ходов
            )

            Difficulty.MEDIUM -> DraughtsAI(
                maxDepth = 3,
                randomness = 0.1  // 10% случайных ходов
            )

            Difficulty.HARD -> DraughtsAI(
                maxDepth = 4,
                randomness = 0.0  // всегда лучший ход
            )

            Difficulty.VERY_HARD -> DraughtsAI(
                maxDepth = 5,     // очень глубокая оценка
                randomness = 0.0
            )
        }
    }

    // AI, который делает ошибки с определенной вероятностью
    class FlawedAI(
        private val mistakeProbability: Double = 0.3,  // вероятность ошибки
        private val baseAI: DraughtsAI = DraughtsAI(maxDepth = 3)
    ) {
      /*  fun findBestMove(board: Array<Array<Piece?>>, player: Player): Move {
            val moves = baseAI.moveGenerator.generateAllMoves(board, player)

            // С вероятностью mistakeProbability выбираем плохой ход
            if (Math.random() < mistakeProbability && moves.size > 1) {
                // Ищем ходы без взятия или с меньшим количеством взятий
                val captureMoves = moves.filter { it.captured.isNotEmpty() }
                val nonCaptureMoves = moves.filter { it.captured.isEmpty() }

                // Если есть ходы без взятия, выбираем случайный из них (это ошибка)
                if (nonCaptureMoves.isNotEmpty()) {
                    return nonCaptureMoves.random()
                }
                // Иначе выбираем ход с минимальным количеством взятий
                else if (captureMoves.size > 1) {
                    val minCaptures = captureMoves.minOf { it.captured.size }
                    val badMoves = captureMoves.filter { it.captured.size == minCaptures }
                    return badMoves.random()
                }
            }

            // Иначе нормальный AI
            return baseAI.findBestMove(board, player)
        }*/
    }
}