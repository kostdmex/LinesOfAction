package com.example.linesofaction

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.linesofaction.game.LinesOfAction

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun playGameButtonListener(view : View) {
        LinesOfAction.getNewGame()
        startActivity(Intent(this@MainActivity, PlayGameActivity::class.java))
    }

    fun aboutButtonListener(view : View) {
        startActivity(Intent(this@MainActivity, AboutActivity::class.java))
    }

    fun settingsButtonListener(view : View) {
        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
    }

    fun exitButtonListener(view : View) {
        finish()
    }
}
