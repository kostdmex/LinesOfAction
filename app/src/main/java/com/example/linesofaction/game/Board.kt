package com.example.linesofaction.game

import android.graphics.Point
import java.lang.Math.abs
import java.util.*
import kotlin.collections.ArrayList

class Board() {
    var playerBoard = BitSet(64)
    var opponentBoard = BitSet(64)
    var activePoint: Point? = null
    var lastPosition : Point? = null
    var currentPosition : Point? = null

    constructor(board : Board) : this() {
        this.playerBoard = board.playerBoard.clone() as BitSet
        this.opponentBoard = board.opponentBoard.clone() as BitSet
        if(activePoint!=null) {
            this.activePoint = Point(board.activePoint)
        }
    }

    fun reverseSides() : Board{
        val reversedBoard = Board()
        reversedBoard.opponentBoard = playerBoard
        reversedBoard.playerBoard = opponentBoard
        return reversedBoard
    }
}

