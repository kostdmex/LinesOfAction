package com.example.linesofaction.graphic

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import com.example.linesofaction.game.Board
import com.example.linesofaction.game.BitSetInterface
import com.example.linesofaction.game.LinesOfAction

class DrawingInterface {
    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels.toFloat()
    private val screenWidthDivByEight = screenWidth / 8
    private val screenWidthDivByFour = screenWidth / 4
    private val centerX = screenWidth / 16
    private val radiusX = screenWidth / 20
    private val startDrawingPoint = screenWidth / 6
    private lateinit var canvas: Canvas
    private val ZERO = 0F
    //Method for drawing the whole board with pieces
    fun drawBoard(canvas: Canvas) {
        this.canvas = canvas
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawRect(ZERO, startDrawingPoint, screenWidth, screenWidth + startDrawingPoint, paint)

        drawTurnInformation()
        drawRowsInBoard()
        drawMoves(LinesOfAction.board)
        drawPieces(LinesOfAction.board)
        drawMarkingMoveLine(LinesOfAction.board.lastPosition, LinesOfAction.board.currentPosition)
    }

    //Method for drawing two rows, first is starting with white, second with gray
    private fun drawDoubleRow(y: Float) {
        val paint = Paint()
        paint.color = Color.GRAY
        //first row starting with white
        for (x in screenWidthDivByEight.toInt() until screenWidth.toInt() + startDrawingPoint.toInt() step screenWidthDivByFour.toInt()) {
            canvas.drawRect(x.toFloat(), y, x.toFloat() + screenWidthDivByEight, y + screenWidthDivByEight, paint)
        }
        //second row starting with gray
        val secondY = y + screenWidthDivByEight

        for (x in ZERO.toInt() until screenWidth.toInt() + startDrawingPoint.toInt() step screenWidthDivByFour.toInt()) {
            canvas.drawRect(
                x.toFloat(),
                secondY,
                x.toFloat() + screenWidthDivByEight,
                secondY + screenWidthDivByEight,
                paint
            )
        }
    }

    //drawing double rows in the whole board
    private fun drawRowsInBoard() {
        for (y in startDrawingPoint.toInt() until screenWidth.toInt() + startDrawingPoint.toInt() step screenWidthDivByFour.toInt()) {
            drawDoubleRow(y.toFloat())
        }
    }

    //drawing pieces on board
    private fun drawPieces(board : Board) {
        lateinit var tempBoard: Board
        synchronized(board) {
            tempBoard = Board(board)
        }
        var index = 0
        for (i in 0..tempBoard.playerBoard.cardinality()) {
            index = tempBoard.playerBoard.nextSetBit(index)
            val piece = BitSetInterface.fromIndexToPoint(index)
            canvas.drawCircle(piece.x * screenWidthDivByEight + centerX, piece.y * screenWidthDivByEight + centerX + startDrawingPoint, radiusX, LinesOfAction.paintFirstPlayer)
            index++
        }
        index = 0
        for (i in 0..tempBoard.opponentBoard.cardinality()) {
            index = tempBoard.opponentBoard.nextSetBit(index)
            val piece = BitSetInterface.fromIndexToPoint(index)
            canvas.drawCircle(piece.x * screenWidthDivByEight + centerX, piece.y * screenWidthDivByEight + centerX + startDrawingPoint, radiusX, LinesOfAction.paintSecondPlayer)
            index++
        }
    }

    private fun drawMoves(board: Board) {
        if (board.activePoint != null) {
            val point = board.activePoint
            lateinit var list : ArrayList<Point>
            synchronized(board) {
                if (point != null) {
                    list = BitSetInterface.getMovesFromXY(point.x, point.y, board)
                }
            }
            val paint = Paint()
            paint.color = Color.GREEN
            paint.alpha = 40
            for (point in list) {
                val x = point.x * screenWidthDivByEight
                val y = point.y * screenWidthDivByEight + startDrawingPoint
                canvas.drawRect(x, y, x + screenWidthDivByEight, y + screenWidthDivByEight, paint)
            }
        }
    }

    private fun drawMarkingMoveLine(from:Point?, to:Point?){
        if(to!=null&&from!=null){
            val fromX = from.x * screenWidthDivByEight + centerX
            val fromY = from.y * screenWidthDivByEight + centerX + startDrawingPoint
            val toX = to.x * screenWidthDivByEight + centerX
            val toY = to.y * screenWidthDivByEight + centerX + startDrawingPoint
            val paint = if(LinesOfAction.playerTurn==LinesOfAction.paintFirstPlayer) LinesOfAction.paintSecondPlayer else LinesOfAction.paintFirstPlayer
            paint.strokeWidth = 5F
            canvas.drawLine(fromX, fromY, toX, toY, paint)
        }
    }
    private fun drawTurnInformation(){
        var paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = screenWidth / 15
        canvas.drawRect(ZERO, ZERO, screenWidth, startDrawingPoint, paint)
        if(LinesOfAction.wonGameFlag==0) {
            if (LinesOfAction.playerTurn != LinesOfAction.paintFirstPlayer) paint.color = Color.RED else paint.color = Color.BLUE
            val turnInformation = if (LinesOfAction.playerTurn != LinesOfAction.paintFirstPlayer) "Red turn" else "Blue turn"
            drawTextCentered(turnInformation, screenWidth / 2F, startDrawingPoint - startDrawingPoint / 5, paint)
        } else {
            if(LinesOfAction.wonGameFlag==1){
                paint.color = Color.BLUE
                val winInformation = "Blue side won!"
                drawTextCentered(winInformation, screenWidth / 2F, startDrawingPoint / 2F, paint)
            } else {
                paint.color = Color.RED
                val winInformation = "Red side won!"
                drawTextCentered(winInformation, screenWidth / 2F, startDrawingPoint / 2F, paint)
            }
        }
    }
    private fun drawTextCentered(text: String, x: Float, y: Float, paint: Paint) {
        val xPos = x - (paint.measureText(text) / 2).toInt()
        val yPos = (y - (paint.descent() + paint.ascent()) / 2)

        canvas.drawText(text, xPos, yPos, paint)
    }
}