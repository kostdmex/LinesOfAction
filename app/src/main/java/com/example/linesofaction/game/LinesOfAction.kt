package com.example.linesofaction.game

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point

object LinesOfAction {
    var board = Board()
    private const val firstLineCoord = 0
    private const val secondLineCoord = 7
    private const val startLineCoord = 1
    private const val endLineCoord = 6
    val paintFirstPlayer = Paint()
    val paintSecondPlayer = Paint()
    var playerTurn = paintFirstPlayer
    var wonGameFlag = 0
    var firstPlayerAsAI = false
    var secondPlayerAsAI = false
    var movesCounter = 1

    init {
        paintFirstPlayer.color = Color.BLUE
        paintSecondPlayer.color = Color.RED
        getNewGame()
    }

    fun handleTouchedScreen(x: Int, y: Int) {
        if ((firstPlayerAsAI && playerTurn == paintFirstPlayer) || (secondPlayerAsAI && playerTurn == paintSecondPlayer)) return
        if (wonGameFlag == 0) {
            val bitset = if (playerTurn == paintFirstPlayer) board.firstPlayerBoard else board.secondPlayerBoard
            val enemyBitSet = if (playerTurn != paintFirstPlayer) board.firstPlayerBoard else board.secondPlayerBoard
            val possibleTouchedPoint = if (BitSetInterface.getByXYBoard(x, y, bitset)) {
                Point(x, y)
            } else {
                null
            }
            when {
                board.activePoint == null && possibleTouchedPoint != null -> {
                    board.activePoint = Point(x, y)
                }
                board.activePoint == null && possibleTouchedPoint == null -> return
                board.activePoint != null && board.activePoint == possibleTouchedPoint -> board.activePoint = null
                board.activePoint != null -> {
                    val flag = BitSetInterface.movePiece(board.activePoint!!.x, board.activePoint!!.y, x, y, bitset, enemyBitSet)
                    if (flag) {
                        markMovedPosition(board.activePoint!!, Point(x, y), board)
                        changeturn()
                    }
                    board.activePoint = null
                }
            }
        }
    }

    fun movePieceByAI(from: Point, to: Point) {
        var flag = BitSetInterface.movePiece(from.x, from.y, to.x, to.y, board.firstPlayerBoard, board.secondPlayerBoard)
        markMovedPosition(from, to, board)
        changeturn()
        if (!flag) println("INVALID MOVE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    }

    private fun changeturn() {
        wonGameFlag = checkWonGame()
        playerTurn = if (playerTurn == paintFirstPlayer) paintSecondPlayer else paintFirstPlayer
        if (playerTurn == paintFirstPlayer) movesCounter++
    }

    private fun markMovedPosition(from: Point, to: Point, board: Board){
        board.lastPosition = from
        board.currentPosition = to
    }

    private fun checkWonGame() : Int{
        if (BitSetInterface.bfsToCountFormation(board.firstPlayerBoard) == board.firstPlayerBoard.cardinality()) {
            return 1
        } else if (BitSetInterface.bfsToCountFormation(board.secondPlayerBoard) == board.secondPlayerBoard.cardinality()) {
            return 2
        }
        return 0
    }

    fun getNewGame() {
        board = Board()
        wonGameFlag = 0
        playerTurn = paintFirstPlayer
        movesCounter = 1
        for (piece in (startLineCoord..endLineCoord)) {
            BitSetInterface.setByXYBoard(piece, firstLineCoord, board.firstPlayerBoard)
            BitSetInterface.setByXYBoard(piece, secondLineCoord, board.firstPlayerBoard)
            BitSetInterface.setByXYBoard(firstLineCoord, piece, board.secondPlayerBoard)
            BitSetInterface.setByXYBoard(secondLineCoord, piece, board.secondPlayerBoard)
        }
    }
}