package com.example.dinorun

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class GameOverActivity : AppCompatActivity() {

    private lateinit var startGameAgain: Button
    private lateinit var displayScore: TextView
    private var score: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game_over)

        // Get the score from the intent extras
        score = intent.getIntExtra("Score", 0)

        // Initialize views
        startGameAgain = findViewById(R.id.playAgain)
        displayScore = findViewById(R.id.displayScore)

        // Set OnClickListener for the play again button
        startGameAgain.setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish() // Finish the current activity when starting MainActivity
        }

        // Set the score text
        displayScore.text = "$score"
    }
}
