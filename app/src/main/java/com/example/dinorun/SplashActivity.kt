package com.example.dinorun

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val thread = Thread {
            try {
                Thread.sleep(3000)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                val mainIntent = Intent(this , MainActivity::class.java)
                startActivity(mainIntent)
            }
        }
        thread.start()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}
