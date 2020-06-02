package com.example.linesofaction

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import com.example.linesofaction.ai.Evaluation
import com.example.linesofaction.game.LinesOfAction
import com.example.linesofaction.graphic.GraphicThread

class SurfaceViewBoard(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes),
    SurfaceHolder.Callback {
    private val screenWidth = Resources.getSystem().displayMetrics.widthPixels.toFloat()
    private val screenWidthDivByEight = screenWidth / 8
    private val startBoardXCoord = screenWidth / 6
    private lateinit var graphicThread: GraphicThread
    private lateinit var firstPlayerEvaluation: Evaluation
    private lateinit var playerEvaluation: Evaluation

    init {
        holder.addCallback(this)
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        if (LinesOfAction.firstPlayerAsAI) {
            firstPlayerEvaluation.stopThread()
        }
        if (LinesOfAction.secondPlayerAsAI) {
            playerEvaluation.stopThread()
        }
        graphicThread.stopDrawing()
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        graphicThread = GraphicThread()
        graphicThread.startDrawing(this)
        if (LinesOfAction.firstPlayerAsAI) {
            firstPlayerEvaluation = Evaluation(LinesOfAction.paintFirstPlayer, false)
            firstPlayerEvaluation.startThread()
        }
        if (LinesOfAction.secondPlayerAsAI) {
            playerEvaluation = Evaluation(LinesOfAction.paintSecondPlayer, false)
            playerEvaluation.startThread()
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y - startBoardXCoord
            val posX = Math.floor(x / screenWidthDivByEight.toDouble()).toInt()
            val posY = Math.floor(y / screenWidthDivByEight.toDouble()).toInt()
            LinesOfAction.handleTouchedScreen(posX, posY)
        }
        return true
    }
}
    