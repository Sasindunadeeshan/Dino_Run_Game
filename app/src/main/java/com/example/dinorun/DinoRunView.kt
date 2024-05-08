import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.appcompat.widget.SwitchCompat
import com.example.dinorun.GameOverActivity
import com.example.dinorun.R
import java.util.*

class DinoRunView(context: Context, private val resources: Resources) : View(context){

    private lateinit var sharedPreferences: SharedPreferences

    private val dino = arrayOfNulls<Bitmap>(10) // Array to store all dinosaur frames
    private var currentFrameIndex = 0 // Index to keep track of the current frame
    private val frameDelayMillis = 100 // Delay between frame updates in milliseconds
    private var dinoX = 10
    private var dinoY: Int = 0
    private var dinoSpeed: Int = 0
    private var touch = false
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    private var coinX: Int = 0
    private var coinY: Int = 0
    private var coinSpeed = 16

    private var gemX: Int = 0
    private var gemY: Int = 0
    private var gemSpeed = 20

    private var bombX: Int = 0
    private var bombY: Int = 0
    private var bombSpeed = 25

    private var score: Int = 0
    private var lifeOfDino: Int = 0
    private lateinit var backgroundImage: Bitmap
    private val scorePaint = Paint()
    private val life = arrayOfNulls<Bitmap>(2)
    private lateinit var scoreIc : Bitmap
    private lateinit var lifeIc : Bitmap
    private lateinit var settingIc : Bitmap
    private lateinit var coinBitmap: Bitmap
    private val coinWidth = 100
    private val coinHeight = 100
    private lateinit var gemBitmap: Bitmap
    private val gemWidth = 100
    private val gemHeight = 100
    private lateinit var bombBitmap: Bitmap
    private val bombWidth = 150
    private val bombHeight = 150

    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private lateinit var coinSoundPlayer: MediaPlayer
    private lateinit var gemSoundPlayer: MediaPlayer
    private lateinit var bombSoundPlayer: MediaPlayer

    init {

        sharedPreferences = context.getSharedPreferences("DinoRunSettings", Context.MODE_PRIVATE)

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
        val desiredWidth = 328 // Adjust this value as needed
        val desiredHeight = 422 // Adjust this value as needed

        // Scale the original bitmaps to the desired size
        for (i in 0..9) {
            dino[i] = Bitmap.createScaledBitmap(dino[i]!!, desiredWidth, desiredHeight, true)
        }

        // Load other bitmaps from resources
        backgroundImage = BitmapFactory.decodeResource(resources, R.drawable.background)
        coinBitmap = BitmapFactory.decodeResource(resources, R.drawable.coin)
        gemBitmap = BitmapFactory.decodeResource(resources, R.drawable.gem)
        bombBitmap = BitmapFactory.decodeResource(resources, R.drawable.bomb)
        settingIc = BitmapFactory.decodeResource(resources,R.drawable.settingic)
        scoreIc = BitmapFactory.decodeResource(resources,R.drawable.lifebord)
        lifeIc = BitmapFactory.decodeResource(resources,R.drawable.lifebord)

        scorePaint.color = Color.WHITE
        scorePaint.textSize = 80f
        scorePaint.isAntiAlias = true
        val typeface = ResourcesCompat.getFont(context, R.font.pixel)
        scorePaint.typeface = typeface

        life[0] = BitmapFactory.decodeResource(resources, R.drawable.life1)
        life[1] = BitmapFactory.decodeResource(resources, R.drawable.life2)

        // Initialize the MediaPlayer and load the sound file
        mediaPlayer.apply {
            val rawUri = Uri.parse("android.resource://${context.packageName}/${R.raw.soundtrack}")
            setDataSource(context, rawUri)
            isLooping = true
            prepareAsync()
            setOnPreparedListener {
                // Start playing the soundtrack only if the music switch is on
                if (isMusicOn()) {
                    start()
                }
            }
        }

        mediaPlayer.isLooping = true // Loop the soundtrack
        mediaPlayer.start() // Start playing the soundtrack

        // Initialize sound players with volume based on saved sound settings
        coinSoundPlayer = MediaPlayer.create(context, R.raw.coinsound).apply {
            val volume = if (isSoundOn()) 1f else 0f
            setVolume(volume, volume)
        }
        gemSoundPlayer = MediaPlayer.create(context, R.raw.gemsound).apply {
            val volume = if (isSoundOn()) 1f else 0f
            setVolume(volume, volume)
        }
        bombSoundPlayer = MediaPlayer.create(context, R.raw.bombsound).apply {
            val volume = if (isSoundOn()) 1f else 0f
            setVolume(volume, volume)
        }

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

        coinX -= coinSpeed
        if (hitBallChecker(coinX, coinY)) {
            score += 10
            coinX = canvasWidth + 21 // Move the ball out of the screen
            coinY = generateRandomY(minDinoY, maxDinoY)

            coinSoundPlayer.start()
        }
        if (coinX < 0) {
            coinX = canvasWidth + 21 // Move the ball out of the screen
            coinY = generateRandomY(minDinoY, maxDinoY)
        }
        val resizedCoinBitmap = Bitmap.createScaledBitmap(coinBitmap, coinWidth, coinHeight, true)
        canvas.drawBitmap(resizedCoinBitmap, coinX.toFloat(), coinY.toFloat(), null)

        gemX -= gemSpeed
        if (hitBallChecker(gemX, gemY)) {
            score += 20
            gemX = canvasWidth + 21 // Move the ball out of the screen
            gemY = generateRandomY(minDinoY, maxDinoY)

            gemSoundPlayer.start()
        }
        if (gemX < 0) {
            gemX = canvasWidth + 21 // Move the ball out of the screen
            gemY = generateRandomY(minDinoY, maxDinoY)
        }
        val resizedGemBitmap = Bitmap.createScaledBitmap(gemBitmap, gemWidth, gemHeight, true)
        canvas.drawBitmap(resizedGemBitmap, gemX.toFloat(), gemY.toFloat(), null)

        bombX -= bombSpeed
        if (hitBallChecker(bombX, bombY)) {
            bombX = -100
            lifeOfDino--
            bombSoundPlayer.start()
            if (lifeOfDino == 0) {
                Toast.makeText(context, "Game Over", Toast.LENGTH_SHORT).show()
                val gameOverIntent = Intent(context, GameOverActivity::class.java)
                gameOverIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                gameOverIntent.putExtra("Score", score)
                context.startActivity(gameOverIntent)
            }
        }
        if (bombX < 0) {
            bombX = canvasWidth + 21 // Move the ball out of the screen
            bombY = generateRandomY(minDinoY, maxDinoY)
        }
        val resizedBombBitmap = Bitmap.createScaledBitmap(bombBitmap, bombWidth, bombHeight, true)
        canvas.drawBitmap(resizedBombBitmap, bombX.toFloat(), bombY.toFloat(), null)

        val scoreBitmapWidth = 440
        val scoreBitmapHeight = 160
        val scoreBitmapX = 40
        val scoreBitmapY = 100
        val scaledScoreBitmap = Bitmap.createScaledBitmap(scoreIc, scoreBitmapWidth, scoreBitmapHeight, true)
        canvas.drawBitmap(scaledScoreBitmap, scoreBitmapX.toFloat(), scoreBitmapY.toFloat(), null)

        val lifebordBitmapWidth = 440
        val lifebordBitmapHeight = 160
        val lifebordBitmapX = 40
        val lifebordBitmapY = 300
        val scaledlifebordBitmap = Bitmap.createScaledBitmap(lifeIc, lifebordBitmapWidth, lifebordBitmapHeight, true)
        canvas.drawBitmap(scaledlifebordBitmap, lifebordBitmapX.toFloat(), lifebordBitmapY.toFloat(), null)

        // Draw score text
        val scoreTextX = 120
        val scoreTextY = 200
        canvas.drawText("" + score, scoreTextX.toFloat(), scoreTextY.toFloat(), scorePaint)

        // Draw life bitmaps
        val lifeBitmapWidth = 75
        val lifeBitmapHeight = 75
        val lifeBitmapSpacing = 10
        for (i in 0 until 3) {
            val lifeBitmapX = 120 + (lifeBitmapWidth + lifeBitmapSpacing) * i
            val lifeBitmapY = 340
            if (i < lifeOfDino) {
                val scaledLifeBitmap = Bitmap.createScaledBitmap(life[0]!!, lifeBitmapWidth, lifeBitmapHeight, true)
                canvas.drawBitmap(scaledLifeBitmap, lifeBitmapX.toFloat(), lifeBitmapY.toFloat(), null)
            } else {
                val scaledLifeBitmap2 = Bitmap.createScaledBitmap(life[1]!!, lifeBitmapWidth, lifeBitmapHeight, true)
                canvas.drawBitmap(scaledLifeBitmap2, lifeBitmapX.toFloat(), lifeBitmapY.toFloat(), null)
            }
        }

        //Draw setting ic

        val settingBitmapWidth = 150
        val settingBitmapHeight = 150
        val settingBitmapX = 850
        val settingBitmapY = 150
        val scaledSettingBitmap = Bitmap.createScaledBitmap(settingIc, settingBitmapWidth, settingBitmapHeight, true)
        canvas.drawBitmap(scaledSettingBitmap, settingBitmapX.toFloat(), settingBitmapY.toFloat(), null)

        // Increment frame index for the next frame
        currentFrameIndex = (currentFrameIndex + 1) % 10

        // Request redraw after delay
        postInvalidateDelayed(frameDelayMillis.toLong())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // Check if touch event is within setting icon bounds
            val settingBitmapX = 850
            val settingBitmapY = 150
            val settingBitmapWidth = 120
            val settingBitmapHeight = 120
            if (event.x >= settingBitmapX && event.x <= settingBitmapX + settingBitmapWidth &&
                event.y >= settingBitmapY && event.y <= settingBitmapY + settingBitmapHeight
            ) {
                // Show custom dialog
                showCustomDialog()
            } else {
                // Handle other touch events
                touch = true
                dinoSpeed = -22
            }
        }
        return true
    }
    private fun showCustomDialog() {
        // Create custom dialog
        val dialog = Dialog(context, R.style.CustomDialogTheme)
        dialog.setContentView(R.layout.custom_dialog_box)
        val musicSwitch = dialog.findViewById<SwitchCompat>(R.id.music)
        val soundSwitch = dialog.findViewById<SwitchCompat>(R.id.sound)

        // Set initial state of switches based on saved preferences
        musicSwitch.isChecked = isMusicOn()
        soundSwitch.isChecked = isSoundOn()

        musicSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the state of the music switch
            saveMusicSetting(isChecked)
            // Pause or resume the MediaPlayer based on the state of the music switch
            if (isChecked) {
                mediaPlayer.start()
            } else {
                mediaPlayer.pause()
            }
        }

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the state of the sound switch
            saveSoundSetting(isChecked)
        }

        dialog.show()
    }

    private fun saveMusicSetting(isMusicOn: Boolean) {
        // Save the state of the music switch to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putBoolean("MusicOn", isMusicOn)
        editor.apply()
    }

    private fun saveSoundSetting(isSoundOn: Boolean) {
        // Save the state of the sound switch to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putBoolean("SoundOn", isSoundOn)
        editor.apply() // Persist the changes
        // Update the state of sound players based on the switch state
        updateSoundPlayers(isSoundOn)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Release the MediaPlayer resources when the view is destroyed
        mediaPlayer.release()
        coinSoundPlayer.release()
        gemSoundPlayer.release()
        bombSoundPlayer.release()
    }
    private fun isMusicOn(): Boolean {
        // Retrieve the state of the music switch from SharedPreferences
        return sharedPreferences.getBoolean("MusicOn", true) // Default value is true if not found
    }

    private fun isSoundOn(): Boolean {
        // Retrieve the state of the sound switch from SharedPreferences
        return sharedPreferences.getBoolean("SoundOn", true) // Default value is true if not found
    }

    private fun updateSoundPlayers(isSoundOn: Boolean) {
        // Mute or unmute the sound players based on the switch state
        if (!isSoundOn) {
            coinSoundPlayer.setVolume(0f, 0f)
            gemSoundPlayer.setVolume(0f, 0f)
            bombSoundPlayer.setVolume(0f, 0f)
        } else {
            coinSoundPlayer.setVolume(1f, 1f)
            gemSoundPlayer.setVolume(1f, 1f)
            bombSoundPlayer.setVolume(1f, 1f)
        }
    }

    // Method to check if ball hits dinosaur
    private fun hitBallChecker(x: Int, y: Int): Boolean {
        return dinoX < x && x < dinoX + dino[0]!!.width && dinoY < y && y < dinoY + dino[0]!!.height
    }

    // Method to generate random y-coordinate
    private fun generateRandomY(min: Int, max: Int): Int {
        val random = Random()
        return random.nextInt(max - min) + min
    }
}
