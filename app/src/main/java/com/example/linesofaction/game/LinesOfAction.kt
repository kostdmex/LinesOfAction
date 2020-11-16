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
            val tempBoard = if (playerTurn == paintFirstPlayer) board else board.reverseSides()
            val possibleTouchedPoint = if (BitSetInterface.getByXYBoard(x, y, tempBoard.playerBoard)) {
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
                    val flag = BitSetInterface.movePiece(board.activePoint!!.x, board.activePoint!!.y, x, y, tempBoard)
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
        if(playerTurn== paintFirstPlayer)
        {
            BitSetInterface.movePiece(from.x, from.y, to.x, to.y, board)
        } else {
            BitSetInterface.movePiece(from.x, from.y, to.x, to.y, board.reverseSides())
        }
        markMovedPosition(from, to, board)
        changeturn()
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
        if (BitSetInterface.bfsToCountFormation(board.playerBoard) == board.playerBoard.cardinality()) {
            return 1
        } else if (BitSetInterface.bfsToCountFormation(board.opponentBoard) == board.opponentBoard.cardinality()) {
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
            BitSetInterface.setByXYBoard(piece, firstLineCoord, board.playerBoard)
            BitSetInterface.setByXYBoard(piece, secondLineCoord, board.playerBoard)
            BitSetInterface.setByXYBoard(firstLineCoord, piece, board.opponentBoard)
            BitSetInterface.setByXYBoard(secondLineCoord, piece, board.opponentBoard)
        }
    }
}