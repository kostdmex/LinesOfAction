package com.example.linesofaction.ai

import android.graphics.Paint
import android.graphics.Point
import com.example.linesofaction.game.*
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.math.*

class Evaluation(private val paint: Paint, randomWeights: Boolean) {
    private val millisecondsInterval = 500L
    private val timer = Timer("AI Thread Move", false)
    private var board = Board()
    private val isFirstBoard = LinesOfAction.paintFirstPlayer == paint
    private val range = 20
    private var weightConcentration = 20F
    private var weightCentralisation = 14F
    private var weightMobility = 16F
    private var weightQuads = 15F
    private var weightUniformity = 12F
    private var weightConnectedness = 12F
    private var weightWalls = 18F
    private var weightCentreOfMass = 10F
    private var weightPlayerToMove = 5F
    private val THOUSAND = 1000
    private val TEN = 10
    private val FIVE = 5

    private val pieceSquareTable = intArrayOf(
        -80, -25, -20, -20, -20, -20, -25, -80,
        -25, 10, 10, 10, 10, 10, 10, -25,
        -20, 10, 25, 25, 25, 25, 10, -20,
        -20, 10, 25, 50, 50, 25, 10, -20,
        -20, 10, 25, 50, 50, 25, 10, -20,
        -20, 10, 25, 25, 25, 25, 10, -20,
        -25, 10, 10, 10, 10, 10, 10, -25,
        -80, -25, -20, -20, -20, -20, -25, -80
    )
    private val centreOfMassSquareTable = intArrayOf(
        40, 25, 20, 20, 20, 20, 25, 40,
        25, 25, 10, 10, 10, 10, 25, 25,
        20, 10, -10, -10, -10, -10, 10, 20,
        20, 10, -10, -20, -20, -10, 10, 20,
        20, 10, -10, -20, -20, -10, 10, 20,
        20, 10, -10, -10, -10, -10, 10, 20,
        25, 25, 10, 10, 10, 10, 25, 25,
        40, 25, 20, 20, 20, 20, 25, 40
    )

    init {
        if (randomWeights) randomWeights()
    }

    fun randomWeights() {
        weightConcentration = (Math.random() * range + 1).toInt().toFloat()
        weightCentralisation = (Math.random() * range + 1).toInt().toFloat()
        weightMobility = (Math.random() * range + 1).toInt().toFloat()
        weightQuads = (Math.random() * range + 1).toInt().toFloat()
        weightUniformity = (Math.random() * range + 1).toInt().toFloat()
        weightConnectedness = (Math.random() * range + 1).toInt().toFloat()
        weightWalls = (Math.random() * range + 1).toInt().toFloat()
        weightCentreOfMass = (Math.random() * range + 1).toInt().toFloat()
        weightPlayerToMove = (Math.random() * range + 1).toInt().toFloat()
    }

    fun concentration(com: Point, bitSet: BitSet): Float {
        var sumOfDistances = 0F
        var index = 0
        for (i in 0 until bitSet.cardinality()) {
            index = bitSet.nextSetBit(index)
            val point = BitSetInterface.fromIndexToPoint(index)
            val difrow = abs(com.y - point.y)
            val difcol = abs(com.x - point.x)
            sumOfDistances += if (difrow > difcol) difrow else difcol
            index++
        }
        val sumOfMinDistances = calculateSumOfminDistances(com, bitSet.cardinality() - 1)
        val surplusOfDistances = sumOfDistances - sumOfMinDistances
        return (1.0F / surplusOfDistances)
    }

    fun calculateSumOfminDistances(point: Point, size: Int): Int {
        var sum = 0
        if ((point.x == 0 || point.x == 7) && (point.y == 0 || point.y == 7)) {
            for (i in 0 until size) {
                if (i > 7) sum += 3
                else if (i > 2) sum += 2
                else sum++
            }
        } else if ((point.x == 0 || point.x == 7) || (point.y == 0 || point.y == 7)) {
            for (i in 0 until size) {
                if (i > 4) sum += 2
                else sum++
            }
        } else {
            for (i in 0 until size) {
                if (i > 7) sum += 2
                else sum++
            }
        }
        return sum
    }

    private fun centralisation(bitSet: BitSet): Float {
        var averageOfPieceValues = 0F
        var index = 0
        for (i in 0 until bitSet.cardinality()) {
            index = bitSet.nextSetBit(index)
            averageOfPieceValues += pieceSquareTable[index]
            index++
        }
        averageOfPieceValues /= bitSet.cardinality()
        return averageOfPieceValues
    }

    private fun calculateCentreOfMass(bitSet: BitSet): Point {
        var centreOfMassIndex = bitSet.nextSetBit(0)
        val point = BitSetInterface.fromIndexToPoint(centreOfMassIndex)
        var index = centreOfMassIndex + 1
        for (i in 0 until bitSet.cardinality() - 1) {
            index = bitSet.nextSetBit(index)
            val tempPoint = BitSetInterface.fromIndexToPoint(index)
            point.x = (point.x + tempPoint.x) / 2
            point.y = (point.y + tempPoint.y) / 2
            index++
        }
        return point
    }

    private fun countQuads(bitSet: BitSet): Float {
        var result = 0F
        for (y in 0 until 7) {
            for (x in 0 until 7) {
                result += isQuad(x, y, bitSet)
            }
        }
        return result
    }

    private fun isQuad(x: Int, y: Int, bitSet: BitSet): Float {
        var result = 0F
        if (BitSetInterface.getByXYBoard(x, y, bitSet)) result++
        if (BitSetInterface.getByXYBoard(x + 1, y, bitSet)) result++
        if (BitSetInterface.getByXYBoard(x, y + 1, bitSet)) result++
        if (BitSetInterface.getByXYBoard(x + 1, y + 1, bitSet)) result++
        return if (result > 2F) result else 0F
    }

    private fun mobility(board: Board): Float {
        var value = 0F
        var index = 0
        for (i in 0 until board.playerBoard.cardinality()) {
            index = board.playerBoard.nextSetBit(index)
            val point = BitSetInterface.fromIndexToPoint(index)
            val moves = BitSetInterface.getMovesFromXY(point.x, point.y, board)
            if (moves.size > 0) {
                for (dst in moves) {
                    var tempValue = 1F
                    if (BitSetInterface.getByXYBoard(dst.x, dst.y, board.opponentBoard)) tempValue *= 2F
                    if (dst.x == 0 || dst.x == 7 || dst.y == 0 || dst.y == 7) {
                        tempValue /= 2F
                        if (point.x == 0 || point.x == 7 || point.y == 0 || point.y == 7) {
                            tempValue /= 2F
                        }
                    }
                    value += tempValue
                }
            }
            index++
        }
        return value
    }

    private fun connectedness(bitSet: BitSet): Float {
        var averageConnectedness = 0F
        var index = 0
        for (i in 0 until bitSet.cardinality()) {
            index = bitSet.nextSetBit(index)
            averageConnectedness += BitSetInterface.bfsToCountFormation(index, bitSet)
            index++
        }
        averageConnectedness /= bitSet.cardinality()
        return averageConnectedness
    }

    private fun uniformity(bitSet: BitSet): Float {
        var area = 0F
        var firstPointIndex = bitSet.nextSetBit(0)
        val point = BitSetInterface.fromIndexToPoint(firstPointIndex)
        var leftUpPoint = point
        var leftDownPoint = point
        var rightUpPoint = point
        var rightDownPoint = point
        var calculatedLeftUpPoint = calculateDistance(Point(0, 0), leftUpPoint)
        var calculatedLeftDownPoint = calculateDistance(Point(0, 7), leftDownPoint)
        var calculatedRightUpPoint = calculateDistance(Point(7, 0), rightUpPoint)
        var calculatedRightDownPoint = calculateDistance(Point(7, 7), rightDownPoint)
        var index = firstPointIndex + 1
        for (i in 0 until bitSet.cardinality() - 1) {
            index = bitSet.nextSetBit(index)
            val tempPoint = BitSetInterface.fromIndexToPoint(index)
            val calculatedLeftUpPointInList = calculateDistance(Point(0, 0), tempPoint)
            val calculatedLeftDownPointInList = calculateDistance(Point(0, 7), tempPoint)
            val calculatedRightUpPointInList = calculateDistance(Point(7, 0), tempPoint)
            val calculatedRightDownPointInList = calculateDistance(Point(7, 7), tempPoint)
            when {
                calculatedLeftUpPointInList < calculatedLeftUpPoint -> {
                    leftUpPoint = tempPoint
                    calculatedLeftUpPoint = calculatedLeftUpPointInList
                }
                calculatedLeftDownPointInList < calculatedLeftDownPoint -> {
                    leftDownPoint = tempPoint
                    calculatedLeftDownPoint = calculatedLeftDownPointInList
                }
                calculatedRightUpPointInList < calculatedRightUpPoint -> {
                    rightUpPoint = tempPoint
                    calculatedRightUpPoint = calculatedRightUpPointInList
                }
                calculatedRightDownPointInList < calculatedRightDownPoint -> {
                    rightDownPoint = tempPoint
                    calculatedRightDownPoint = calculatedRightDownPointInList
                }
            }
            index++
        }
        area = (calculateDistance(leftUpPoint, rightUpPoint) + calculateDistance(rightUpPoint, rightDownPoint) + calculateDistance(rightDownPoint, leftDownPoint)
                + calculateDistance(leftDownPoint, leftUpPoint)).toFloat()
        return 1.0F / area
    }

    private fun calculateDistance(fromPoint: Point, toPoint: Point): Double {
        return sqrt((toPoint.y.toDouble() - fromPoint.y.toDouble()).pow(2.0) + ((toPoint.x.toDouble() - fromPoint.x.toDouble()).pow(2.0)))
    }

    private fun evaluateCentreOfMass(point: Point): Float {
        val index = 8 * point.y + point.x
        return centreOfMassSquareTable[index].toFloat()
    }

    private fun playerToMove(isPlayerMoving: Boolean): Float {
        return if (isPlayerMoving) 10F else 0F
    }

    private fun walls(board: Board): Float {
        var blockedDirections = 0F
        blockedDirections = calculateWallsInCorner(board)
        for (i in 1..6) {
            if (BitSetInterface.getByXYBoard(i, 0, board.opponentBoard)) {
                if (BitSetInterface.getByXYBoard(i, 1, board.playerBoard)) {
                    blockedDirections++
                    if (BitSetInterface.getByXYBoard(i - 1, 1, board.playerBoard)) blockedDirections++
                    if (BitSetInterface.getByXYBoard(i + 1, 1, board.playerBoard)) blockedDirections++
                }
            }
            if (BitSetInterface.getByXYBoard(0, i, board.opponentBoard)) {
                if (BitSetInterface.getByXYBoard(1, i, board.playerBoard)) {
                    blockedDirections++
                    if (BitSetInterface.getByXYBoard(1, i - 1, board.playerBoard)) blockedDirections++
                    if (BitSetInterface.getByXYBoard(1, i + 1, board.playerBoard)) blockedDirections++
                }
            }
            if (BitSetInterface.getByXYBoard(i, 7, board.opponentBoard)) {
                if (BitSetInterface.getByXYBoard(i, 6, board.playerBoard)) {
                    blockedDirections++
                    if (BitSetInterface.getByXYBoard(i - 1, 6, board.playerBoard)) blockedDirections++
                    if (BitSetInterface.getByXYBoard(i + 1, 6, board.playerBoard)) blockedDirections++
                }
            }
            if (BitSetInterface.getByXYBoard(7, i, board.opponentBoard)) {
                if (BitSetInterface.getByXYBoard(6, i, board.playerBoard)) {
                    blockedDirections++
                    if (BitSetInterface.getByXYBoard(6, i - 1, board.playerBoard)) blockedDirections++
                    if (BitSetInterface.getByXYBoard(6, i + 1, board.playerBoard)) blockedDirections++
                }
            }
        }
        return blockedDirections
    }

    private fun calculateWallsInCorner(board: Board): Float {
        var blockedDirections = 0F
        if (BitSetInterface.getByXYBoard(0, 0, board.opponentBoard)) {
            if (BitSetInterface.getByXYBoard(1, 0, board.playerBoard) && BitSetInterface.getByXYBoard(1, 1, board.playerBoard)) {
                blockedDirections += 3F
            } else if (BitSetInterface.getByXYBoard(1, 0, board.playerBoard) || BitSetInterface.getByXYBoard(1, 1, board.playerBoard)) {
                blockedDirections += 2F
            }
            if (BitSetInterface.getByXYBoard(0, 1, board.playerBoard) && BitSetInterface.getByXYBoard(1, 1, board.playerBoard)) {
                blockedDirections += 3F
            } else if (BitSetInterface.getByXYBoard(0, 1, board.playerBoard) || BitSetInterface.getByXYBoard(1, 1, board.playerBoard)) {
                blockedDirections += 2F
            }
        }
        if (BitSetInterface.getByXYBoard(0, 7, board.opponentBoard)) {
            if (BitSetInterface.getByXYBoard(0, 6, board.playerBoard) && BitSetInterface.getByXYBoard(1, 6, board.playerBoard)) {
                blockedDirections += 3F
            } else if (BitSetInterface.getByXYBoard(0, 6, board.playerBoard) || BitSetInterface.getByXYBoard(1, 6, board.playerBoard)) {
                blockedDirections += 2F
            }
            if (BitSetInterface.getByXYBoard(1, 7, board.playerBoard) && BitSetInterface.getByXYBoard(1, 6, board.playerBoard)) {
                blockedDirections += 3F
            } else if (BitSetInterface.getByXYBoard(1, 7, board.playerBoard) || BitSetInterface.getByXYBoard(1, 6, board.playerBoard)) {
                blockedDirections += 2F
            }
        }
        if (BitSetInterface.getByXYBoard(7, 7, board.opponentBoard)) {
            if (BitSetInterface.getByXYBoard(6, 7, board.playerBoard) && BitSetInterface.getByXYBoard(6, 6, board.playerBoard)) {
                blockedDirections += 3F
            } else if (BitSetInterface.getByXYBoard(6, 7, board.playerBoard) || BitSetInterface.getByXYBoard(6, 6, board.playerBoard)) {
                blockedDirections += 2F
            }
            if (BitSetInterface.getByXYBoard(7, 6, board.playerBoard) && BitSetInterface.getByXYBoard(6, 6, board.playerBoard)) {
                blockedDirections += 3F
            } else if (BitSetInterface.getByXYBoard(7, 6, board.playerBoard) || BitSetInterface.getByXYBoard(6, 6, board.playerBoard)) {
                blockedDirections += 2F
            }
        }
        if (BitSetInterface.getByXYBoard(7, 0, board.opponentBoard)) {
            if (BitSetInterface.getByXYBoard(7, 1, board.playerBoard) && BitSetInterface.getByXYBoard(6, 1, board.playerBoard)) {
                blockedDirections += 3F
            } else if (BitSetInterface.getByXYBoard(7, 1, board.playerBoard) || BitSetInterface.getByXYBoard(6, 1, board.playerBoard)) {
                blockedDirections += 2F
            }
            if (BitSetInterface.getByXYBoard(6, 0, board.playerBoard) && BitSetInterface.getByXYBoard(6, 1, board.playerBoard)) {
                blockedDirections += 3F
            } else if (BitSetInterface.getByXYBoard(6, 0, board.playerBoard) || BitSetInterface.getByXYBoard(6, 1, board.playerBoard)) {
                blockedDirections += 2F
            }
        }
        return blockedDirections
    }

    private fun minimaxWithPrunning(board : Board, node: Point, move: Point, depth: Int, alpha: Float, beta: Float, maximizingPlayer: Boolean): Float {
        var alpha = alpha
        var beta = beta
        if (depth == 0 || BitSetInterface.checkIfGameWon(board) != 0) {
            if(maximizingPlayer) return evaluateMove(maximizingPlayer, board)
            return evaluateMove(maximizingPlayer, board.reverseSides())
        }
        var index = 0
        if (maximizingPlayer) {
            var maxValue = Float.NEGATIVE_INFINITY
            for (i in 0 until board.playerBoard.cardinality()) {
                index = board.playerBoard.nextSetBit(index)
                val root = BitSetInterface.fromIndexToPoint(index)
                val moves = BitSetInterface.getMovesFromXY(root.x, root.y, board)
                if (moves.size == 0) continue;
                for (point in moves) {
                    val tempBoard = Board(board)
                    BitSetInterface.movePiece(root.x, root.y, point.x, point.y, tempBoard)
                    val value = minimaxWithPrunning(tempBoard.reverseSides(), Point(), Point(), depth - 1, alpha, beta, false)
                    if (value > maxValue) {
                        maxValue = value
                        node.set(root.x, root.y)
                        move.set(point.x, point.y)
                    }
                    alpha = max(maxValue, alpha)
                    if (beta <= alpha) break
                }
                index++
            }
            return maxValue
        }
        var minValue = Float.POSITIVE_INFINITY
        for (i in 0 until board.opponentBoard.cardinality()) {
            index = board.opponentBoard.nextSetBit(index)
            val root = BitSetInterface.fromIndexToPoint(index)
            val moves = BitSetInterface.getMovesFromXY(root.x, root.y, board)
            if (moves.size == 0) continue;
            for (point in moves) {
                val tempBoard = Board(board)
                BitSetInterface.movePiece(root.x, root.y, point.x, point.y, tempBoard)
                val value = minimaxWithPrunning(tempBoard.reverseSides(), Point(), Point(), depth - 1, alpha, beta, true)
                minValue = min(minValue, value)
                beta = min(beta, minValue)
                if (beta <= alpha) break
            }
            index++
        }
        return minValue
    }

    private fun pickBestMove() {
        synchronized(LinesOfAction.board) {
            board = Board(LinesOfAction.board)
        }
        if (!isFirstBoard) {
            board = board.reverseSides()
        }
        var bestRoot = Point()
        var bestMove = Point()
        minimaxWithPrunning(board, bestRoot, bestMove, 3, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, true)
        LinesOfAction.movePieceByAI(bestRoot, bestMove)
    }

    private fun evaluateMove(isPlayerMoving: Boolean, board: Board): Float {
        val friendlyCOM = calculateCentreOfMass(board.playerBoard)
        val friendlySituation = evaluateSituation(friendlyCOM, board.playerBoard)
        val enemyCOM = calculateCentreOfMass(board.opponentBoard)
        val enemySituation = evaluateSituation(enemyCOM, board.opponentBoard)
        val mobility = weightMobility * mobility(board)
        val walls = weightWalls * walls(board) * TEN
        val playerToMove = weightPlayerToMove * playerToMove(isPlayerMoving)
        val random = (Math.random() * TEN).toFloat()
        return (friendlySituation - enemySituation) + mobility + walls + playerToMove + random
    }

    private fun evaluateSituation(point: Point, bitSet: BitSet): Float {
        val concentration = weightConcentration * concentration(point, bitSet) * THOUSAND
        val centralisation = weightCentralisation * centralisation(bitSet)
        val quads = weightQuads * countQuads(bitSet) * TEN
        val uniformity = weightUniformity * uniformity(bitSet)
        val connectedness = weightConnectedness * connectedness(bitSet) * FIVE
        val centreOfMass = weightCentreOfMass * evaluateCentreOfMass(point) * THOUSAND
        return concentration + centralisation + quads + uniformity + connectedness + centreOfMass
    }

    private fun run(miliseconds: Long) {
        timer.scheduleAtFixedRate(0, miliseconds) {
            if (LinesOfAction.playerTurn == paint) {
                pickBestMove()
            }
        }
    }

    fun startThread() {
        run(millisecondsInterval)
    }

    fun stopThread() {
        timer.cancel()
    }
}