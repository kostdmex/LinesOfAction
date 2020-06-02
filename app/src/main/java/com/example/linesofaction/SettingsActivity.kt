package com.example.linesofaction

import android.app.Activity
import android.os.Bundle
import android.widget.Switch
import com.example.linesofaction.game.LinesOfAction

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val firstPlayer = findViewById<Switch>(R.id.firstPlayerSwitch)
        firstPlayer.setOnCheckedChangeListener { _, isChecked ->
            LinesOfAction.firstPlayerAsAI = isChecked
        }
        val secondPlayer = findViewById<Switch>(R.id.secondPlayerSwitch)
        secondPlayer.setOnCheckedChangeListener { _, isChecked ->
            LinesOfAction.secondPlayerAsAI = isChecked
        }
        if(LinesOfAction.firstPlayerAsAI) firstPlayer.isChecked = true
        if(LinesOfAction.secondPlayerAsAI) secondPlayer.isChecked = true
    }
}
