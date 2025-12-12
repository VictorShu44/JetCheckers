// Файл: feture/checkers/logic/CheckersGame.kt
package com.shu.design.logic


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlin.math.abs

/**
 * Перечисление для игроков (цветов шашек)
 */
enum class Player {
    WHITE, BLACK
}

/**
 * Перечисление для типов шашек
 */
enum class PieceType {
    MAN, // Обычная шашка
    KING // Дамка
}

/**
 * Класс, представляющий одну шашку на доске
 * @param owner Игрок, которому принадлежит шашка
 * @param type Тип шашки (обычная или дамка)
 */
data class Piece(val owner: Player, var type: PieceType = PieceType.MAN)


// ----------- КОНЕЦ НЕДОСТАЮЩИХ КЛАССОВ -----------


class Board {
    val size = 8
    val cells: Array<Array<Piece?>> = Array(size) { arrayOfNulls<Piece?>(size) }

    init {
        initPieces()
    }

    // ----------- НЕДОСТАЮЩИЕ ФУНКЦИИ -----------

    /**
     * Получает шашку по координатам (строка, столбец)
     */
    fun getPiece(row: Int, col: Int): Piece? {
        return if (inside(row, col)) cells[row][col] else null
    }

    /**
     * Устанавливает шашку в заданную клетку
     */
    fun setPiece(row: Int, col: Int, piece: Piece?) {
        if (inside(row, col)) {
            cells[row][col] = piece
        }
    }

    /**
     * Проверяет, находятся ли координаты в пределах доски
     */
    fun inside(row: Int, col: Int): Boolean {
        return row >= 0 && row < size && col >= 0 && col < size
    }

    /**
     * Расставляет шашки в начальную позицию
     */
    fun initPieces() {
        // Очищаем доску на случай повторной инициализации
        for (r in 0 until size) {
            for (c in 0 until size) {
                cells[r][c] = null
            }
        }

        // Расставляем черные и белые шашки
        for (r in 0 until 3) {
            for (c in 0 until size) {
                if ((r + c) % 2 != 0) {
                    setPiece(r, c, Piece(Player.BLACK))
                }
            }
        }

        for (r in 5 until size) {
            for (c in 0 until size) {
                if ((r + c) % 2 != 0) {
                    setPiece(r, c, Piece(Player.WHITE))
                }
            }
        }
    }

    // ----------- КОНЕЦ НЕДОСТАЮЩИХ ФУНКЦИЙ -----------
}

/** Состояние игры для UI */
data class GameState(
    val board: Board,
    val currentPlayer: Player,
    val selectedPiece: Pair<Int, Int>? = null, // Координаты выбранной шашки
    val statusMessage: String = "Ход белых"
)

/** Основная логика игры, адаптированная для Compose */
class CheckersGame {
    private var board = Board()
    private var selectedPiece: Pair<Int, Int>? = null
    private var mustCapture = false // Флаг, указывающий на обязательный захват

    // Live-данные для UI
    val gameState: MutableState<GameState> = mutableStateOf(createInitialGameState())

    private fun createInitialGameState(): GameState {
        return GameState(
            board = board,
            currentPlayer = Player.WHITE,
            statusMessage = "Ход белых"
        )
    }

    /** Обрабатывает нажатие на клетку (row, col) */
    fun handleCellClick(row: Int, col: Int) {
        val currentSelected = selectedPiece
        val clickedPiece = board.getPiece(row, col)

        if (currentSelected == null) {
            // 1. Попытка выбрать шашку
            if (clickedPiece != null && clickedPiece.owner == gameState.value.currentPlayer) {
                selectedPiece = Pair(row, col)
                updateState()
            }
        } else {
            // 2. Попытка сделать ход выбранной шашкой
            val (srcRow, srcCol) = currentSelected

            if (tryMove(srcRow, srcCol, row, col)) {
                // Ход успешен
                val isCapture = abs(row - srcRow) == 2
                maybeKing(row, col)

                // Проверяем, возможен ли следующий захват той же шашкой
                if (isCapture && canCaptureFrom(row, col)) {
                    mustCapture = true
                    selectedPiece = Pair(row, col) // Оставляем шашку выбранной для следующего хода
                    updateState(newStatus = "Необходимо продолжить захват!")
                } else {
                    // Цепочка захватов окончена или это был обычный ход
                    mustCapture = false
                    selectedPiece = null
                    switchPlayer()
                }
            } else {
                // 3. Ход не удался, возможно, игрок хочет выбрать другую шашку
                if (clickedPiece != null && clickedPiece.owner == gameState.value.currentPlayer) {
                    selectedPiece = Pair(row, col) // Выбираем другую свою шашку
                    updateState()
                } else {
                    selectedPiece = null // Снимаем выделение
                    updateState()
                }
            }
        }
    }

    private fun switchPlayer() {
        val nextPlayer = if (gameState.value.currentPlayer == Player.WHITE) Player.BLACK else Player.WHITE
        val message = "Ход ${if (nextPlayer == Player.WHITE) "белых" else "чёрных"}"
        gameState.value = gameState.value.copy(currentPlayer = nextPlayer, statusMessage = message)
    }

    private fun updateState(newStatus: String? = null) {
        val status = newStatus ?: "Ход ${if (gameState.value.currentPlayer == Player.WHITE) "белых" else "чёрных"}"
        gameState.value = gameState.value.copy(
            board = board,
            selectedPiece = selectedPiece,
            statusMessage = status
        )
    }

    private fun tryMove(sr: Int, sc: Int, dr: Int, dc: Int): Boolean {
        if (!board.inside(dr, dc) || board.getPiece(dr, dc) != null) return false
        if (mustCapture && abs(dr - sr) != 2) return false // Если должны бить, обычный ход запрещен

        val piece = board.getPiece(sr, sc)!!
        val dir = if (piece.owner == Player.WHITE) -1 else 1
        val dy = dr - sr
        val dx = dc - sc

        // Обычный ход
        if (abs(dy) == 1 && abs(dx) == 1 && !mustCapture) {
            if (piece.type == PieceType.MAN && dy != dir) return false
            board.setPiece(dr, dc, piece)
            board.setPiece(sr, sc, null)
            return true
        }

        // Захват
        if (abs(dy) == 2 && abs(dx) == 2) {
            val midR = sr + dy / 2
            val midC = sc + dx / 2
            val middle = board.getPiece(midR, midC)
            if (middle != null && middle.owner != piece.owner) {
                board.setPiece(midR, midC, null) // Удаляем захваченную
                board.setPiece(dr, dc, piece)
                board.setPiece(sr, sc, null)
                return true
            }
        }
        return false
    }

    /** Проверяет, может ли шашка в (r, c) совершить захват */
    private fun canCaptureFrom(r: Int, c: Int): Boolean {
        val piece = board.getPiece(r, c) ?: return false
        val dirs = listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))

        for ((dr, dc) in dirs) {
            if (board.inside(r + dr * 2, c + dc * 2) && board.getPiece(r + dr * 2, c + dc * 2) == null) {
                val middlePiece = board.getPiece(r + dr, c + dc)
                if (middlePiece != null && middlePiece.owner != piece.owner) {
                    return true
                }
            }
        }
        return false
    }

    private fun maybeKing(r: Int, c: Int) {
        val piece = board.getPiece(r, c) ?: return
        if (piece.type == PieceType.MAN) {
            if ((piece.owner == Player.WHITE && r == 0) || (piece.owner == Player.BLACK && r == 7)) {
                piece.type = PieceType.KING
            }
        }
    }
}
