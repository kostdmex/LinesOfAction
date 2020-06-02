package com.example.linesofaction.game

import android.graphics.Point
import java.lang.Math.abs
import java.util.*
import kotlin.collections.ArrayList

class Board() {
    var firstPlayerBoard = BitSet(64)
    var secondPlayerBoard = BitSet(64)
    var activePoint: Point? = null
    var lastPosition : Point? = null
    var currentPosition : Point? = null

    constructor(board : Board) : this() {
        this.firstPlayerBoard = board.firstPlayerBoard.clone() as BitSet
        this.secondPlayerBoard = board.secondPlayerBoard.clone() as BitSet
        if(activePoint!=null) {
            this.activePoint = Point(board.activePoint)
        }
    }
}

