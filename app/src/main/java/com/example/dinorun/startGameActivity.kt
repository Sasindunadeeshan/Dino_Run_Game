package com.example.dinorun

import DinoRunView
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class startGameActivity : AppCompatActivity() {

    private lateinit var playButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_game)

        playButton=findViewById(R.id.paly)

        // Set OnClickListener for the PLAY button
        playButton.setOnClickListener {
            val startGameIntent = Intent(this, MainActivity::class.java)
            startActivity(startGameIntent);
        }
    }
}