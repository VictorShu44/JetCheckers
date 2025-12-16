package com.shu.conversation.ai

import android.util.Log
import com.shu.conversation.logic.DraughtsMoveGenerator
import com.shu.conversation.logic.Move
import com.shu.conversation.logic.Piece
import com.shu.conversation.logic.PieceType
import com.shu.conversation.logic.Player
import com.shu.conversation.logic.Position


// Дополняем класс DraughtsAI
open class DraughtsAI(
    val maxDepth: Int = 3,
    private val randomness: Double = 0.0,
    private val weights: PositionEvaluator.Weights = PositionEvaluator.Weights()
) {
    private val moveGenerator = DraughtsMoveGenerator()
    private val evaluator = PositionEvaluator()

    // Основной метод поиска лучшего хода с заданной глубиной
    fun findBestMoveAtDepth(board: Array<Array<Piece?>>, player: Player, depth: Int): Move {
        val moves = moveGenerator.generateAllMoves(board, player)


        if (moves.isEmpty()) {
            throw IllegalStateException("Нет возможных ходов для игрока $player")
        }

        if (moves.size == 1) return moves[0]

        // Для очень простого AI (depth=1) можно использовать быструю оценку
        if (depth == 1) {
            return findBestImmediateMove(board, player, moves)
        }

        var bestMove = moves[0]
        var bestValue = Int.MIN_VALUE

        // Перебираем все ходы и выбираем лучший

        for (move in moves) {

            val newBoard = applyMove(board, move)
            val value = minimax(
                board = newBoard,
                depth = depth - 1,
                alpha = Int.MIN_VALUE,
                beta = Int.MAX_VALUE,
                maximizingPlayer = false, // Следующий ход - противник
                currentPlayer = player,
                originalPlayer = player,
                weights = weights
            )

            if (value > bestValue) {
                bestValue = value
                bestMove = move
                Log.d("mov", "find ${move.from.row},${move.from.col}, ${move.to.row}, ${move.to.col}")
            }
        }


        return bestMove
    }

    // Быстрая оценка для самого поверхностного поиска
    private fun findBestImmediateMove(
        board: Array<Array<Piece?>>,
        player: Player,
        moves: List<Move>
    ): Move {
        // Простая эвристика: предпочитаем взятия, затем превращения, затем центральные позиции
        return moves.maxByOrNull { move ->
            var score = 0

            // Бонус за взятия
            score += move.captured.size * 100

            // Бонус за превращение в дамку

            if (move.becomesKing) {
                score += 50
            }

            // Бонус за ход в центр
            val centerBonus = when (move.to.row) {
                in 3..4 -> if (move.to.col in 3..4) 20 else 10
                else -> 0
            }
            score += centerBonus

            // Штраф за ход к краю (если не превращение)
            if (!move.becomesKing) {
                val edgePenalty = when {
                    move.to.row == 0 || move.to.row == 7 -> -15
                    move.to.col == 0 || move.to.col == 7 -> -10

                    else -> 0
                }
                score += edgePenalty
            }

            score
        } ?: moves.random()
    }

    // Полный метод минимакс с альфа-бета отсечением
    private fun minimax(
        board: Array<Array<Piece?>>,
        depth: Int,
        alpha: Int,
        beta: Int,
        maximizingPlayer: Boolean,
        currentPlayer: Player,
        originalPlayer: Player,
        weights: PositionEvaluator.Weights
    ): Int {

        // Базовый случай: достигнута максимальная глубина или игра окончена
        if (depth == 0 || isGameOver(board, currentPlayer)) {
            return evaluator.evaluate(board, originalPlayer, weights)
        }

        val moves = moveGenerator.generateAllMoves(board, currentPlayer)

        // Если нет возможных ходов
        if (moves.isEmpty()) {
            return if (currentPlayer == originalPlayer) {
                // Текущий игрок (которого мы оцениваем) не может ходить - плохо
                Int.MIN_VALUE + depth
            } else {

                // Противник не может ходить - хорошо
                Int.MAX_VALUE - depth
            }
        }

        return if (maximizingPlayer) {
            var maxEval = Int.MIN_VALUE
            var currentAlpha = alpha

            for (move in moves) {
                val newBoard = applyMove(board, move)
                val eval = minimax(
                    board = newBoard,
                    depth = depth - 1,
                    alpha = currentAlpha,
                    beta = beta,
                    maximizingPlayer = false,
                    currentPlayer = currentPlayer.opponent(),

                    originalPlayer = originalPlayer,
                    weights = weights
                )

                maxEval = maxOf(maxEval, eval)
                currentAlpha = maxOf(currentAlpha, eval)

                // Альфа-бета отсечение
                if (beta <= currentAlpha) {
                    break
                }
            }
            maxEval
        } else {
            var minEval = Int.MAX_VALUE
            var currentBeta = beta

            for (move in moves) {
                val newBoard = applyMove(board, move)

                val eval = minimax(
                    board = newBoard,
                    depth = depth - 1,
                    alpha = alpha,
                    beta = currentBeta,
                    maximizingPlayer = true,
                    currentPlayer = currentPlayer.opponent(),
                    originalPlayer = originalPlayer,
                    weights = weights
                )

                minEval = minOf(minEval, eval)
                currentBeta = minOf(currentBeta, eval)

                // Альфа-бета отсечение
                if (currentBeta <= alpha) {
                    break
                }
            }

            minEval
        }
    }

    // Метод для поиска лучшего хода с ограничением по времени
    fun findBestMoveWithTimeLimit(
        board: Array<Array<Piece?>>,
        player: Player,
        timeLimitMs: Long
    ): Move {
        val startTime = System.currentTimeMillis()
        var depth = 1
        var bestMove: Move? = null
        var bestMoveAtDepth: Move

        // Итеративное углубление: начинаем с глубины 1 и увеличиваем, пока не закончится время
        while (System.currentTimeMillis() -

            startTime < timeLimitMs && depth <= maxDepth) {
            try {
                // Пытаемся найти лучший ход на текущей глубине
                bestMoveAtDepth = findBestMoveAtDepth(board, player, depth)
                bestMove = bestMoveAtDepth

                // Если нашли выигрышный ход, возвращаем его сразу
                if (isWinningMove(board, player, bestMoveAtDepth)) {
                    return bestMoveAtDepth
                }

                depth++

                // Проверяем, осталось ли время для следующей итерации
                val timeSpent =

                    System.currentTimeMillis() - startTime
                val averageTimePerDepth = timeSpent / depth
                if (timeSpent + averageTimePerDepth * 3 > timeLimitMs) {
                    break // Вероятно, не успеем закончить следующую глубину
                }
            } catch (e: Exception) {
                // Если произошла ошибка (например, не хватило памяти), возвращаем лучший найденный ход
                break
            }
        }

        return bestMove ?: findBestMoveAtDepth(board, player, 1)
    }

    // Проверка, является ли ход выигрышным
    private fun isWinningMove(board: Array<Array<Piece?>>, player: Player, move: Move): Boolean {
        val newBoard = applyMove(board, move)

        // Проверяем, остались ли у противника шашки
        val opponent = player.opponent()
        var opponentHasPieces = false
        for (row in 0..7) {
            for (col in 0..7) {
                if (newBoard[row][col]?.owner == opponent) {
                    opponentHasPieces = true
                    break
                }
            }
            if (opponentHasPieces) break
        }


        if (!opponentHasPieces) return true

        // Проверяем, есть ли у противника ходы
        return moveGenerator.generateAllMoves(newBoard, opponent).isEmpty()
    }

    // Оригинальный метод findBestMove (использует максимальную глубину)
    open fun findBestMove(board: Array<Array<Piece?>>, player: Player): Move {
        val moves = moveGenerator.generateAllMoves(board, player)

        if (moves.isEmpty()) {
            throw IllegalStateException("Нет возможных ходов")
        }

        if (moves.size == 1) return moves[0]

        // Применяем случайность для "тупого" AI
      /*  if (randomness > 0.0 && Math.random() < randomness) {
            return if (Math.random() < 0.3) {
                // 30% шанс выбрать худший ход
                findWorstMove(board, player, moves)
            } else {
                moves.random()
            }
        }
*/
        return findBestMoveAtDepth(board, player, maxDepth)

    }

    // Поиск худшего хода (для создания ошибок)
    private fun findWorstMove(
        board: Array<Array<Piece?>>,
        player: Player,
        moves: List<Move>
    ): Move {
        if (moves.size == 1) return moves[0]

        // Ищем ходы без взятий
        val nonCaptureMoves = moves.filter { it.captured.isEmpty() }
        if (nonCaptureMoves.isNotEmpty()) {
            // Среди ходов без взятий ищем самый плохой
            return nonCaptureMoves.minByOrNull { move ->
                var score = 0


                // Штраф за превращение (парадоксально, но для худшего хода)
                if (move.becomesKing) {
                    score -= 30
                }

                // Штраф за ход в центр
                val centerPenalty = when (move.to.row) {
                    in 3..4 -> if (move.to.col in 3..4) -20 else -10
                    else -> 0
                }
                score += centerPenalty

                // Бонус за ход к краю
                val edgeBonus = when {
                    move.to.row == 0 || move.to.row == 7 -> 15
                    move.to.col == 0 || move.to.col == 7 -> 10

                    else -> 0
                }
                score += edgeBonus

                score
            } ?: nonCaptureMoves.random()
        }

        // Если все ходы со взятиями, выбираем с минимальным количеством взятий
        val minCaptures = moves.minOf { it.captured.size }
        val worstCaptureMoves = moves.filter { it.captured.size == minCaptures }
        return worstCaptureMoves.random()
    }

    // Вспомогательные методы
    private fun applyMove(board: Array<Array<Piece?>>, move: Move):

            Array<Array<Piece?>> {
        val newBoard = deepCopyBoard(board)
        val piece = board[move.from.row][move.from.col]

        // Удаляем шашку с исходной позиции
        newBoard[move.from.row][move.from.col] = null

        // Удаляем все съеденные шашки
        for (captured in move.captured) {
            newBoard[captured.row][captured.col] = null
        }

        // Ставим шашку на новую позицию
        val movedPiece = if (move.becomesKing) {
            piece?.copy(type = PieceType.KING,

                position = move.to)
        } else {
            piece?.copy(position = move.to)
        }
        newBoard[move.to.row][move.to.col] = movedPiece

        return newBoard
    }

    private fun deepCopyBoard(board: Array<Array<Piece?>>): Array<Array<Piece?>> {
        return Array(8) { row ->
            Array(8) { col ->
                board[row][col]?.copy(position = Position(row, col))
            }
        }
    }


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

        return !hasPieces || moveGenerator.generateAllMoves(board, player).isEmpty()
    }


    private fun Player.opponent(): Player = when (this) {
        Player.WHITE -> Player.BLACK
        Player.BLACK -> Player.WHITE
    }
}

// Обновленная фабрика AI
object AIFactory {

    enum class Difficulty {
        VERY_EASY, EASY, MEDIUM, HARD, VERY_HARD, EXPERT
    }

    fun createAI(difficulty: Difficulty): DraughtsAI {
        return when (difficulty) {
            Difficulty.VERY_EASY -> DraughtsAI(
                maxDepth = 1,

                randomness = 0.8,
                weights = PositionEvaluator.Weights(
                    pieceValue = 80,
                    kingValue = 200,
                    centralPosition = 0,
                    mobility = 0,
                    safePosition = 0,
                    promotionChance = 5
                )
            )

            Difficulty.EASY -> DraughtsAI(
                maxDepth = 2,
                randomness = 0.4,
                weights = PositionEvaluator.Weights(
                    pieceValue = 100,
                    kingValue = 250,
                    centralPosition = 5,
                    mobility = 2,

                    safePosition = 5,
                    promotionChance = 10
                )
            )

            Difficulty.MEDIUM -> DraughtsAI(
                maxDepth = 3,
                randomness = 0.1,
                weights = PositionEvaluator.Weights(
                    pieceValue = 100,
                    kingValue = 300,
                    centralPosition = 10,
                    mobility = 5,
                    safePosition = 15,
                    promotionChance = 20
                )
            )

            Difficulty.HARD -> DraughtsAI(
                maxDepth = 4,

                randomness = 0.0,
                weights = PositionEvaluator.Weights(
                    pieceValue = 100,
                    kingValue = 350,
                    centralPosition = 15,
                    mobility = 8,
                    safePosition = 20,
                    promotionChance = 25
                )
            )

            Difficulty.VERY_HARD -> DraughtsAI(
                maxDepth = 5,
                randomness = 0.0,
                weights = PositionEvaluator.Weights(
                    pieceValue = 100,
                    kingValue = 400,
                    centralPosition = 20,
                    mobility = 10,

                    safePosition = 25,
                    promotionChance = 30
                )
            )

            Difficulty.EXPERT -> DraughtsAI(
                maxDepth = 6,
                randomness = 0.0,
                weights = PositionEvaluator.Weights(
                    pieceValue = 100,
                    kingValue = 450,
                    centralPosition = 25,
                    mobility = 15,
                    safePosition = 30,
                    promotionChance = 35
                )
            )
        }
    }
}