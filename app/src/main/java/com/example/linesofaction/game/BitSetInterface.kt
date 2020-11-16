package com.example.linesofaction.game

import android.graphics.Point
import java.util.*
import kotlin.collections.ArrayList

object BitSetInterface {
    private const val startBoard = 0
    private const val endBoard = 7
    fun setByXYBoard(x: Int, y: Int, bitSet: BitSet) {
        if (x in startBoard..endBoard && y in startBoard..endBoard) {
            bitSet.flip(y * 8 + x)
        }
    }

    fun getByXYBoard(x: Int, y: Int, bitSet: BitSet): Boolean {
        return if (x in startBoard..endBoard && y in startBoard..endBoard) {
            bitSet.get(y * 8 + x)
        } else {
            false
        }
    }

    fun getMovesFromXY(x: Int, y: Int, board : Board): ArrayList<Point> {
        val moves = ArrayList<Point>()

        /* Method returns array of integers. Those indexes contains number of pieces in unique directions.
        First index: Pieces below and above from point.
        Second index: Pieces on the left and right from point.
        Third index: Pieces in diagonal direction on the upper left and bottom right side from the point.
        Fourth index: Pieces in diagonal direction on the upper right and bottom left side from the point. */
        val piecesInDirections = countPiecesInDirections(x, y, board)

        addMovesFromPosition(x, y, 0, piecesInDirections[0], board, moves)
        addMovesFromPosition(x, y, piecesInDirections[1], 0, board, moves)
        addMovesFromPosition(x, y, piecesInDirections[2], piecesInDirections[2], board, moves)
        addMovesFromPosition(x, y, piecesInDirections[3], -piecesInDirections[3], board, moves)
        return moves
    }

    private fun countPiecesInDirections(x: Int, y: Int, board : Board): Array<Int> {
        var rightUpDirection = 0
        var upDownDirection = 0
        var leftUpDirection = 0
        var rightLeftDirection = 0
        for (tempX in startBoard until endBoard + 1 step 1) {
            if (getByXYBoard(tempX, y, board.playerBoard) || getByXYBoard(tempX, y, board.opponentBoard)) {
                rightLeftDirection++
            }
        }
        for (tempY in startBoard until endBoard + 1 step 1) {
            if (getByXYBoard(x, tempY, board.playerBoard) || getByXYBoard(x, tempY, board.opponentBoard)) {
                upDownDirection++
            }
        }
        var array = calculateFixedIndex(x, y, "r")
        var tempX = array.get(0)
        var tempY = array.get(1)
        while (tempX in startBoard..endBoard && tempY in startBoard..endBoard) {
            if (getByXYBoard(tempX, tempY, board.playerBoard) || getByXYBoard(tempX, tempY, board.opponentBoard)) {
                rightUpDirection++
            }
            tempX++
            tempY--
        }
        array = calculateFixedIndex(x, y, "l")
        tempX = array.get(0)
        tempY = array.get(1)
        while (tempX in startBoard..endBoard && tempY in startBoard..endBoard) {
            if (getByXYBoard(tempX, tempY, board.playerBoard) || getByXYBoard(tempX, tempY, board.opponentBoard)) {
                leftUpDirection++
            }
            tempX++
            tempY++
        }
        return arrayOf(upDownDirection, rightLeftDirection, leftUpDirection, rightUpDirection)
    }
    private fun addMovesFromPosition(startX: Int, startY: Int, diffX: Int, diffY: Int,  board : Board, moves : ArrayList<Point>){
        if(diffX!=0&&diffY!=0){
            if(validPositionDiagonal(startX, startY, startX+diffX, startY+diffY, board))
                moves.add(Point(startX+diffX,startY+diffY))
            if(validPositionDiagonal(startX, startY, startX-diffX, startY-diffY, board))
                moves.add(Point(startX-diffX,startY-diffY))
        } else {
            if(diffY==0){
                if (validPositionStraight(startX, startY, startX + diffX, startY, board))
                    moves.add(Point(startX + diffX, startY))
                if (validPositionStraight(startX, startY, startX - diffX, startY, board))
                    moves.add(Point(startX - diffX, startY))
            } else {
                if (validPositionStraight(startX, startY, startX, startY + diffY, board))
                    moves.add(Point(startX, startY + diffY))
                if (validPositionStraight(startX, startY, startX, startY - diffY, board))
                    moves.add(Point(startX, startY - diffY))
            }
        }
    }
    private fun validPositionStraight(startX: Int, startY: Int, endX: Int, endY: Int,  board: Board): Boolean {
        if (endX !in startBoard..endBoard || endY !in startBoard..endBoard) return false
        if (isAlly(endX, endY, startX, startY,  board)) return false
        when {
            startX == endX && startY != endY -> {
                for (index in startY until endY) {
                    if (getByXYBoard(startX, index, board.playerBoard) || getByXYBoard(startX, index, board.opponentBoard)) {
                        if (!isAlly(startX, startY, startX, index, board)) {
                            return false
                        }
                    }
                }
                for (index in startY downTo endY + 1) {
                    if (getByXYBoard(startX, index, board.playerBoard) || getByXYBoard(startX, index, board.opponentBoard)) {
                        if (!isAlly(startX, startY, startX, index, board)) {
                            return false
                        }
                    }
                }
            }
            startX != endX && startY == endY -> {
                for (index in startX until endX) {
                    if (getByXYBoard(index, startY, board.playerBoard) || getByXYBoard(index, startY, board.opponentBoard)) {
                        if (!isAlly(startX, startY, index, startY,  board)) {
                            return false
                        }
                    }
                }
                for (index in startX downTo endX + 1) {
                    if (getByXYBoard(index, startY, board.playerBoard) || getByXYBoard(index, startY, board.opponentBoard)) {
                        if (!isAlly(startX, startY, index, startY,  board)) {
                            return false
                        }
                    }
                }
            }
            startX != endX && startY != endY -> {
                for (index in startX until endX) {
                    if (getByXYBoard(index, startY, board.playerBoard) || getByXYBoard(index, startY, board.opponentBoard)) {
                        if (!isAlly(startX, startY, index, startY,  board)) {
                            return false
                        }
                    }
                }
                for (index in startX downTo endX + 1) {
                    if (getByXYBoard(index, startY, board.playerBoard) || getByXYBoard(index, startY, board.opponentBoard)) {
                        if (!isAlly(startX, startY, index, startY,  board)) {
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    private fun validPositionDiagonal(startX: Int, startY: Int, endX: Int, endY: Int,  board : Board): Boolean {
        if (endX !in startBoard..endBoard || endY !in startBoard..endBoard) return false
        if (isAlly(startX, startY, endX, endY, board)) return false
        when {
            startX < endX && startY < endY -> {
                var indexY = startY
                for (indexX in startX until endX) {
                    if (getByXYBoard(indexX, indexY, board.playerBoard) || getByXYBoard(indexX, indexY, board.opponentBoard)) {
                        if (!isAlly(startX, startY, indexX, indexY, board)) {
                            return false
                        }
                    }
                    indexY++
                }
            }
            startX > endX && startY > endY -> {
                var indexY = startY
                for (indexX in startX downTo endX + 1) {
                    if (getByXYBoard(indexX, indexY, board.playerBoard) || getByXYBoard(indexX, indexY, board.opponentBoard)) {
                        if (!isAlly(startX, startY, indexX, indexY, board)) {
                            return false
                        }
                    }
                    indexY--
                }
            }
            startX < endX && startY > endY -> {
                var indexY = startY
                for (indexX in startX until endX) {
                    if (getByXYBoard(indexX, indexY, board.playerBoard) || getByXYBoard(indexX, indexY, board.opponentBoard)) {
                        if (!isAlly(startX, startY, indexX, indexY,  board)) {
                            return false
                        }
                    }
                    indexY--
                }
            }
            startX > endX && startY < endY -> {
                var indexY = startY
                for (indexX in startX downTo endX + 1) {
                    if (getByXYBoard(indexX, indexY, board.playerBoard) || getByXYBoard(indexX, indexY, board.opponentBoard)) {
                        if (!isAlly(startX, startY, indexX, indexY, board)) {
                            return false
                        }
                    }
                    indexY++
                }
            }
        }
        return true
    }

    private fun calculateFixedIndex(x: Int, y: Int, flag: String): Array<Int> {
        var resultX = x
        var resultY = y
        while (x in startBoard..endBoard && y in startBoard..endBoard) {
            if (flag.equals("r", true)) {
                if (resultX > 0 && resultY < 7) {
                    resultX--
                    resultY++
                } else break
            } else if (flag.equals("l", true)) {
                if (resultX > 0 && resultY > 0) {
                    resultX--
                    resultY--
                } else break
            }
        }
        return arrayOf(resultX, resultY)
    }

    private fun isAlly(firstX: Int, firstY: Int, secondX: Int, secondY: Int,  board : Board): Boolean {
        return (getByXYBoard(firstX, firstY, board.playerBoard) && (getByXYBoard(secondX, secondY, board.playerBoard)))
                || (getByXYBoard(firstX, firstY, board.opponentBoard) && (getByXYBoard(secondX, secondY, board.opponentBoard)))
    }

    fun movePiece(fromX: Int, fromY: Int, toX: Int, toY: Int,  board: Board): Boolean {
        if (!(toX in startBoard..endBoard && toY in startBoard..endBoard)) return false
        if(!getMovesFromXY(fromX, fromY, board).contains(Point(toX, toY))) return false
        setByXYBoard(fromX, fromY, board.playerBoard)
        setByXYBoard(toX, toY, board.playerBoard)
        if(getByXYBoard(toX, toY, board.opponentBoard)) setByXYBoard(toX, toY, board.opponentBoard)
        return true
    }

    fun bfsToCountFormation(boardBitSet: BitSet): Int {
        val tempBoard = boardBitSet.clone() as BitSet
        val startIndex = tempBoard.nextSetBit(0)
        val q = LinkedList<Point>()
        var count = 0
        val startingPiece = fromIndexToPoint(startIndex)
        q.push(startingPiece)
        setByXYBoard(startingPiece.x, startingPiece.y, tempBoard)
        while (!q.isEmpty()) {
            val v = q.pop()
            count++
            for (node in findNeighbours(v, tempBoard)) {
                setByXYBoard(node.x, node.y, tempBoard)
                q.push(node)
            }
        }
        return count
    }

    fun bfsToCountFormation(index: Int, boardBitSet: BitSet): Int {
        val tempBoard = boardBitSet.clone() as BitSet
        val q = LinkedList<Point>()
        var count = 0
        val startingPiece = fromIndexToPoint(index)
        q.push(startingPiece)
        setByXYBoard(startingPiece.x, startingPiece.y, tempBoard)
        while (!q.isEmpty()) {
            val v = q.pop()
            count++
            for (node in findNeighbours(v, tempBoard)) {
                setByXYBoard(node.x, node.y, tempBoard)
                q.push(node)
            }
        }
        return count
    }

    fun findNeighbours(point: Point, board: BitSet): ArrayList<Point> {
        val neighbours = ArrayList<Point>()
        var index = 0
        for (i in 0..board.cardinality()) {
            index = board.nextSetBit(index)
            if(index>0) {
                val tempPoint = fromIndexToPoint(index)
                if (Math.abs(point.x - tempPoint.x) <= 1 && Math.abs(point.y - tempPoint.y) <= 1) {
                    neighbours.add(tempPoint)
                }
                index++
            }
        }
        return neighbours
    }

    fun fromIndexToPoint(index: Int): Point {
        return Point(index % 8, index / 8)
    }

    fun checkIfGameWon(board : Board) : Int{
        return when {
            bfsToCountFormation(board.playerBoard) == board.playerBoard.cardinality() -> 1
            bfsToCountFormation(board.opponentBoard) == board.opponentBoard.cardinality() -> 2
            else -> 0
        }
    }
}