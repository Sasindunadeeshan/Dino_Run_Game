package com.example.dinorun

import DinoRunView
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var dinoRunView: DinoRunView
    private val handler = Handler()
    private val interval: Long = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        dinoRunView = DinoRunView(this, resources)
        setContentView(dinoRunView)

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    dinoRunView.invalidate()
                }
            }
        }, 0, interval)
    }
}
