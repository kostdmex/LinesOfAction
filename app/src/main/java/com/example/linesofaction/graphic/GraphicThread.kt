package com.example.linesofaction.graphic

import android.graphics.Canvas
import android.view.SurfaceHolder
import com.example.linesofaction.SurfaceViewBoard
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class GraphicThread{
    private lateinit var canvas : Canvas
    private val drawingInterface : DrawingInterface =
        DrawingInterface()
    private val milisecondsInterval = 100L
    private lateinit var surfaceViewBoard : SurfaceViewBoard
    private lateinit var surfaceHolder: SurfaceHolder

    private fun run(miliseconds: Long) {
        timer.scheduleAtFixedRate(0,miliseconds){
            synchronized(surfaceHolder) {
                canvas = surfaceHolder.lockCanvas()
                drawingInterface.drawBoard(canvas)
                surfaceViewBoard.postInvalidate()
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun stop(){
        timer.cancel()
    }

    fun startDrawing(surfaceViewBoard: SurfaceViewBoard){
        this.surfaceViewBoard = surfaceViewBoard
        this.surfaceHolder = surfaceViewBoard.holder
        run(milisecondsInterval)
    }
    fun stopDrawing(){
        stop()
    }
    private val timer = Timer("Update graphic", false)

}