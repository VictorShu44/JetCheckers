package com.shu.conversation.ai

import com.shu.conversation.logic.Move
import com.shu.conversation.logic.Piece
import com.shu.conversation.logic.PieceType
import com.shu.conversation.logic.Player
import com.shu.conversation.logic.Position

fun main1() {
    // Инициализация доски
    val board = Array(8) { row ->
        Array<Piece?>(8) { col ->
            when {
                row < 3 && (row + col) % 2 == 1 ->
                    Piece(Player.BLACK, PieceType.MAN, Position(row, col))
                row > 4 && (row + col) % 2 == 1 ->
                    Piece(Player.WHITE, PieceType.MAN, Position(row, col))
                else -> null
            }
        }
    }

    println("Игра против AI разного уровня:")

    // Простой AI (случайные ходы)
    val easyAI = AIFactory.createAI(AIFactory.Difficulty.EASY)
    println("Легкий AI думает...")
    val easyMove = easyAI.findBestMove(board, Player.WHITE)
    println("Легкий AI выбрал: ${easyMove.from} -> ${easyMove.to}")

    // Средний AI
    val mediumAI = AIFactory.createAI(AIFactory.Difficulty.MEDIUM)
    println("\nСредний AI думает...")
    val mediumMove = mediumAI.findBestMove(board, Player.WHITE)
    println("Средний AI выбрал: ${mediumMove.from} -> ${mediumMove.to}")

    // Сложный AI
    val hardAI = AIFactory.createAI(AIFactory.Difficulty.HARD)
    println("\nСложный AI думает...")
    val hardMove = hardAI.findBestMove(board, Player.WHITE)
    println("Сложный AI выбрал: ${hardMove.from} -> ${hardMove.to}")

    // AI с ошибками
    /*val flawedAI = AIFactory.FlawedAI(mistakeProbability = 0.4)
    println("\nAI с ошибками думает...")
    val flawedMove = flawedAI.findBestMove(board, Player.WHITE)
    println("AI с ошибками выбрал: ${flawedMove.from} -> ${flawedMove.to}")*/

    // Сравнение оценки позиции
    val evaluator = PositionEvaluator()
    val positionScore = evaluator.evaluate(board, Player.WHITE)
    println("\nОценка текущей позиции для белых: $positionScore")
}


/*
fun findBestMoveWithTimeLimit(
    board: Array<Array<Piece?>>,
    player: Player,
    timeLimitMs: Long
): Move {
    val startTime = System.currentTimeMillis()
    var depth = 1
    var bestMove: Move? = null

    while (System.currentTimeMillis() - startTime < timeLimitMs && depth <= maxDepth) {
        val move = findBestMoveAtDepth(board, player, depth)
        if (move != null) {
            bestMove = move
        }
        depth++
    }

    return bestMove ?: findBestMoveAtDepth(board, player, 1)!!
}*/
