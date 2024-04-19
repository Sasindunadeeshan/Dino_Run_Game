import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.example.dinorun.GameOverActivity
import com.example.dinorun.R
import java.util.*

class DinoRunView(context: Context, private val resources: Resources) : View(context) {

    private val dino = arrayOfNulls<Bitmap>(10) // Array to store all dinosaur frames
    private var currentFrameIndex = 0 // Index to keep track of the current frame
    private val frameDelayMillis = 100 // Delay between frame updates in milliseconds
    private var dinoX = 10
    private var dinoY: Int = 0
    private var dinoSpeed: Int = 0
    private var touch = false
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    private var yellowX: Int = 0
    private var yellowY: Int = 0
    private var yellowSpeed = 16

    private var greenX: Int = 0
    private var greenY: Int = 0
    private var greenSpeed = 20

    private var redX: Int = 0
    private var redY: Int = 0
    private var redSpeed = 25

    private var score: Int = 0
    private var lifeOfDino: Int = 0
    private lateinit var backgroundImage: Bitmap
    private val scorePaint = Paint()
    private val life = arrayOfNulls<Bitmap>(2)
    private lateinit var coinBitmap: Bitmap
    private val coinWidth = 100
    private val coinHeight = 100
    private lateinit var gemBitmap: Bitmap
    private val gemWidth = 100
    private val gemHeight = 100
    private lateinit var bombBitmap: Bitmap
    private val bombWidth = 150
    private val bombHeight = 150

    init {
        // Load dinosaur frames from resources
        dino[0] = BitmapFactory.decodeResource(resources, R.drawable.run1)
        dino[1] = BitmapFactory.decodeResource(resources, R.drawable.run2)
        dino[2] = BitmapFactory.decodeResource(resources, R.drawable.run3)
        dino[3] = BitmapFactory.decodeResource(resources, R.drawable.run4)
        dino[4] = BitmapFactory.decodeResource(resources, R.drawable.run5)
        dino[5] = BitmapFactory.decodeResource(resources, R.drawable.run6)
        dino[6] = BitmapFactory.decodeResource(resources, R.drawable.run7)
        dino[7] = BitmapFactory.decodeResource(resources, R.drawable.run8)
        dino[8] = BitmapFactory.decodeResource(resources, R.drawable.run9)
        dino[9] = BitmapFactory.decodeResource(resources, R.drawable.run10)

        // Define the desired width and height for the dinosaur image
        val desiredWidth = 378 // Adjust this value as needed
        val desiredHeight = 472 // Adjust this value as needed

        // Scale the original bitmaps to the desired size
        for (i in 0..9) {
            dino[i] = Bitmap.createScaledBitmap(dino[i]!!, desiredWidth, desiredHeight, true)
        }

        // Load other bitmaps from resources
        backgroundImage = BitmapFactory.decodeResource(resources, R.drawable.night_bg)
        coinBitmap = BitmapFactory.decodeResource(resources, R.drawable.coin)
        gemBitmap = BitmapFactory.decodeResource(resources, R.drawable.gem)
        bombBitmap = BitmapFactory.decodeResource(resources, R.drawable.bomb)


        scorePaint.color = Color.WHITE
        scorePaint.textSize = 100f
        scorePaint.isAntiAlias = true
        val typeface = ResourcesCompat.getFont(context, R.font.nabla_regular)
        scorePaint.typeface = typeface

        life[0] = BitmapFactory.decodeResource(resources, R.drawable.life1)
        life[1] = BitmapFactory.decodeResource(resources, R.drawable.life2)

        // Set initial position and score
        dinoY = 700
        score = 0
        lifeOfDino = 3
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Get canvas dimensions
        canvasWidth = canvas.width
        canvasHeight = canvas.height

        // Draw background image scaled to fit the canvas
        val scaledBackground = Bitmap.createScaledBitmap(backgroundImage, canvasWidth, canvasHeight, true)
        canvas.drawBitmap(scaledBackground, 0f, 0f, null)

        // Ensure correct range for dinosaur y-coordinate
        val minDinoY = dino[0]!!.height
        val maxDinoY = canvasHeight - dino[0]!!.height * 2
        dinoY += dinoSpeed
        if (dinoY < minDinoY) {
            dinoY = minDinoY
        }
        if (dinoY > maxDinoY) {
            dinoY = maxDinoY
        }
        dinoSpeed += 2

        // Draw dinosaur frame based on touch state
        if (touch) {
            canvas.drawBitmap(dino[1]!!, dinoX.toFloat(), dinoY.toFloat(), null) // Display walking animation
            touch = false
        } else {
            // Draw the current dinosaur frame
            canvas.drawBitmap(dino[currentFrameIndex]!!, dinoX.toFloat(), dinoY.toFloat(), null)
        }

        yellowX -= yellowSpeed
        if (hitBallChecker(yellowX, yellowY)) {
            score += 10
            yellowX = canvasWidth + 21 // Move the ball out of the screen
            yellowY = generateRandomY(minDinoY, maxDinoY)
        }
        if (yellowX < 0) {
            yellowX = canvasWidth + 21 // Move the ball out of the screen
            yellowY = generateRandomY(minDinoY, maxDinoY)
        }
        val resizedCoinBitmap = Bitmap.createScaledBitmap(coinBitmap, coinWidth, coinHeight, true)
        canvas.drawBitmap(resizedCoinBitmap, yellowX.toFloat(), yellowY.toFloat(), null)

        greenX -= greenSpeed
        if (hitBallChecker(greenX, greenY)) {
            score += 20
            greenX = canvasWidth + 21 // Move the ball out of the screen
            greenY = generateRandomY(minDinoY, maxDinoY)
        }
        if (greenX < 0) {
            greenX = canvasWidth + 21 // Move the ball out of the screen
            greenY = generateRandomY(minDinoY, maxDinoY)
        }
        val resizedGemBitmap = Bitmap.createScaledBitmap(gemBitmap, gemWidth, gemHeight, true)
        canvas.drawBitmap(resizedGemBitmap, greenX.toFloat(), greenY.toFloat(), null)

        redX -= redSpeed
        if (hitBallChecker(redX, redY)) {
            redX = -100
            lifeOfDino--
            if (lifeOfDino == 0) {
                Toast.makeText(context, "Game Over", Toast.LENGTH_SHORT).show()
                val gameOverIntent = Intent(context, GameOverActivity::class.java)
                gameOverIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                gameOverIntent.putExtra("Score", score)
                context.startActivity(gameOverIntent)
            }
        }
        if (redX < 0) {
            redX = canvasWidth + 21 // Move the ball out of the screen
            redY = generateRandomY(minDinoY, maxDinoY)
        }
        val resizedBombBitmap = Bitmap.createScaledBitmap(bombBitmap, bombWidth, bombHeight, true)
        canvas.drawBitmap(resizedBombBitmap, redX.toFloat(), redY.toFloat(), null)

        // Draw score text
        val scoreTextX = 100
        val scoreTextY = 250
        canvas.drawText("" + score, scoreTextX.toFloat(), scoreTextY.toFloat(), scorePaint)

        // Draw life bitmaps
        val lifeBitmapWidth = 120
        val lifeBitmapHeight = 120
        val lifeBitmapSpacing = 10
        for (i in 0 until 3) {
            val lifeBitmapX = 650 + (lifeBitmapWidth + lifeBitmapSpacing) * i
            val lifeBitmapY = 150
            if (i < lifeOfDino) {
                val scaledLifeBitmap = Bitmap.createScaledBitmap(life[0]!!, lifeBitmapWidth, lifeBitmapHeight, true)
                canvas.drawBitmap(scaledLifeBitmap, lifeBitmapX.toFloat(), lifeBitmapY.toFloat(), null)
            } else {
                val scaledLifeBitmap2 = Bitmap.createScaledBitmap(life[1]!!, lifeBitmapWidth, lifeBitmapHeight, true)
                canvas.drawBitmap(scaledLifeBitmap2, lifeBitmapX.toFloat(), lifeBitmapY.toFloat(), null)
            }
        }

        // Increment frame index for the next frame
        currentFrameIndex = (currentFrameIndex + 1) % 10

        // Request redraw after delay
        postInvalidateDelayed(frameDelayMillis.toLong())
    }

    // Method to check if ball hits dinosaur
    private fun hitBallChecker(x: Int, y: Int): Boolean {
        return dinoX < x && x < dinoX + dino[0]!!.width && dinoY < y && y < dinoY + dino[0]!!.height
    }

    // Touch event handler
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            touch = true
            dinoSpeed = -22
        }
        return true
    }

    // Method to generate random y-coordinate
    private fun generateRandomY(min: Int, max: Int): Int {
        val random = Random()
        return random.nextInt(max - min) + min
    }
}
