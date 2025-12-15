// Файл: feture/checkers/logic/CheckersGame.kt
package com.shu.conversation.logic


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.shu.conversation.ai.AIFactory
import com.shu.conversation.ai.AIFactory.Difficulty
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.math.abs

/**
 * Перечисление для игроков (цветов шашек)
 */
enum class Player {
    WHITE, BLACK //, AI_BLACK, AI_WHITE
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
data class Piece(val owner: Player, var type: PieceType = PieceType.MAN, val position: Position)


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
    fun getPiece(position: Position): Piece? {
        //Log.d("mov", "inside ${inside(position)}  ")
        return if (inside(position)) cells[position.row][position.col] else null
    }

    /**
     * Устанавливает шашку в заданную клетку
     */
    fun setPiece(position: Position, piece: Piece) {
        if (inside(position)) {
            cells[position.row][position.col] = Piece(
                owner = piece.owner,
                type = piece.type,
                position = position
            )
        }
    }

    fun setPieceNull(position: Position, piece: Piece) {
        if (inside(position)) {

            cells[position.row][position.col] = null

        }
    }

    /**
     * Проверяет, находятся ли координаты в пределах доски
     */
    fun inside(position: Position): Boolean {
        return position.row >= 0 && position.row < size && position.col >= 0 && position.col < size
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
                    setPiece(
                        Position(r, c), Piece(
                            Player.BLACK,
                            position = Position(r, c)
                        )
                    )
                }
            }
        }

        for (r in 5 until size) {
            for (c in 0 until size) {
                if ((r + c) % 2 != 0) {
                    setPiece(
                        Position(r, c), Piece(
                            Player.WHITE,
                            position = Position(r, c)
                        )
                    )
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

    private var count = 1
    private val mediumAI = AIFactory.createAI(Difficulty.HARD)
    private var selectedPiece: Pair<Int, Int>? = null

    private var moveAI: Boolean = false
    private var moveRight: Move? = null
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

    fun restart() {
        board = Board()
        val state = createInitialGameState()
        gameState.value = gameState.value.copy(
            board = state.board,
            selectedPiece = state.selectedPiece,
            statusMessage = state.statusMessage
        )
    }

    /** Обрабатывает нажатие на клетку (row, col) */
    fun handleCellClick(row: Int, col: Int) {
        val currentSelected = selectedPiece
        val clickedPiece = board.getPiece(Position(row, col))
        clickedPiece?.let {
            Log.d(
                "mov",
                "$count clickedPiece row ${clickedPiece?.position?.row} col ${clickedPiece?.position?.col}"
            )
        } ?: Log.d("mov", "$count clickedPiece null $row $col")
        if (currentSelected == null) {
            // 1. Попытка выбрать шашку
            Log.d("mov", "$count currentSelected null $row $col")
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
                Log.d("mov", "$count mustCapture $mustCapture isCapture ${row - srcRow}  src $srcRow $srcCol can capture $row $col")
                // Проверяем, возможен ли следующий захват той же шашкой
                if (isCapture && canCaptureFrom(row, col)) {

                    Log.d("mov", "$count isCapture $mustCapture ")
                    mustCapture = true
                    selectedPiece = Pair(row, col) // Оставляем шашку выбранной для следующего хода
                    if(gameState.value.currentPlayer == Player.WHITE) {
                        updateState(newStatus = "$count Необходимо продолжить захват!")
                    } else {
                       // moveBlackTo()
                        updateState(newStatus = "$count moveBlack r $row - $col захват!")
                    }
                } else {
                    // Цепочка захватов окончена или это был обычный ход
                    Log.d("mov", "$count end $row $col and switch Player ")
                    mustCapture = false
                    selectedPiece = null
                    moveRight = null
                    moveAI = true
                    switchPlayer()
                    updateState()
                }
            } else {
                // 3. Ход не удался, возможно, игрок хочет выбрать другую шашку
                if (gameState.value.currentPlayer == Player.WHITE) {
                    Log.d("mov", "$count Ход не удался, возможно, игрок хочет выбрать другую шашку ")
                    if (clickedPiece != null && clickedPiece.owner == gameState.value.currentPlayer) {
                        Log.d("mov", "$count Выбираем другую свою шашку ")
                        selectedPiece = Pair(row, col) // Выбираем другую свою шашку
                        updateState()
                    } else {
                        selectedPiece = null // Снимаем выделение
                        updateState()
                    }
                } else {
                    if (moveAI) {
                        Log.d("mov", "$count ход moveAI r $row, c $col ")
                        mustCapture = false
                        selectedPiece = null
                        moveRight = null
                        switchPlayer()
                        updateState()
                    } else {
                        Log.d("mov", "$count Неправильный ход r $row, c $col ")
                        selectedPiece = null // Снимаем выделение
                        updateState()
                        moveBlack()
                    }
                }
            }
        }
    }

    private fun switchPlayer() {
        val nextPlayer = if (gameState.value.currentPlayer == Player.WHITE) {
            Player.BLACK
        } else Player.WHITE
        count++
        val message = "Ход ${if (nextPlayer == Player.WHITE) "белых $count" else "чёрных $count"}"
        gameState.value = gameState.value.copy(currentPlayer = nextPlayer, statusMessage = message)
    }

    private fun updateState(newStatus: String? = null) {
        val status = newStatus
            ?: "Ход ${if (gameState.value.currentPlayer == Player.WHITE) "белых" else "чёрных"}"
        gameState.value = gameState.value.copy(
            board = board,
            selectedPiece = selectedPiece,
            statusMessage = status
        )
    }

    fun moveBlack() {
        var move: List<Move?> = emptyList()
        moveAI = false
        move = mediumAI.findBestMove(board.cells, Player.BLACK)

        if(move.size == 1) {
            move.first()?.let { move ->
                Log.d(
                    "mov",
                    "$count ход moveAI r ${move.from.row}, ${move.from.col} ${move.to.row},${move.to.col}"
                )
                moveRight = move
                handleCellClick(move.from.row, move.from.col)
            }
        } else {
            var t = ""
            move.forEach { m ->

                t = t + "[${m.from.row}-${m.from.col} ${m.to.row}-${m.to.col}] "
            }
            Log.d(
                "mov",
                t
            )
        }
    }

    fun moveBlackTo() {
        moveRight?.let { move ->
            moveAI = true
            handleCellClick(move.to.row, move.to.col)
            // updateState()
        }
    }

    private fun tryMove(sr: Int, sc: Int, dr: Int, dc: Int): Boolean {
        if (!board.inside(Position(dr, dc)) || board.getPiece(
                Position(
                    dr,
                    dc
                )
            ) != null
        ) return false
        if (mustCapture && abs(dr - sr) != 2) return false // Если должны бить, обычный ход запрещен

        val piece = board.getPiece(Position(sr, sc))
        piece?.let {
            val dir = if (piece?.owner == Player.WHITE) -1 else 1
            val dy = dr - sr
            val dx = dc - sc

            // Обычный ход
            if (abs(dy) == 1 && abs(dx) == 1 && !mustCapture) {
                Log.d("mov", "Обычный ход $dy $dx")
                if (piece?.type == PieceType.MAN && dy != dir) return false
                board.setPiece(
                    Position(dr, dc),
                    piece
                )
                board.setPieceNull(Position(sr, sc), piece)
                return true
            }

            // Захват
            if (abs(dy) == 2 && abs(dx) == 2) {
                Log.d("mov", "Захват $dy $dx")
                val midR = sr + dy / 2
                val midC = sc + dx / 2
                val middle = board.getPiece(Position(midR, midC))
                if (middle != null && middle.owner != piece?.owner) {
                    board.setPieceNull(Position(midR, midC), piece) // Удаляем захваченную
                    board.setPiece(Position(dr, dc), piece)
                    board.setPieceNull(Position(sr, sc), piece)
                    return true
                }
            }
        }
        return false
    }

    /** Проверяет, может ли шашка в (r, c) совершить захват */
    private fun canCaptureFrom(r: Int, c: Int): Boolean {
        val piece = board.getPiece(Position(r, c)) ?: return false
        val dirs = listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))

        for ((dr, dc) in dirs) {
            if (board.inside(
                    Position(
                        r + dr * 2,
                        c + dc * 2
                    )
                ) && board.getPiece(Position(r + dr * 2, c + dc * 2)) == null
            ) {
                val middlePiece = board.getPiece(Position(r + dr, c + dc))
                if (middlePiece != null && middlePiece.owner != piece.owner) {
                    return true
                }
            }
        }
        return false
    }

    private fun maybeKing(r: Int, c: Int) {
        val piece = board.getPiece(Position(r, c)) ?: return
        if (piece.type == PieceType.MAN) {
            if ((piece.owner == Player.WHITE && r == 0) || (piece.owner == Player.BLACK && r == 7)) {
                piece.type = PieceType.KING //переделать на val
            }
        }
    }
}
