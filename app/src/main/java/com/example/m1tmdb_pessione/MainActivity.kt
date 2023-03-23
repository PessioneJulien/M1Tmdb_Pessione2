package com.example.m1tmdb_pessione

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import retrofit2.Callback
import retrofit2.Response
import com.example.m1tmdb_pessione.TempoApi.TempoApi

data class TempoData(val redCount: Int, val blueCount: Int, val whiteCount: Int)

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var colorWatch: ImageView
    private lateinit var redCount: TextView
    private lateinit var blueCount: TextView
    private lateinit var whiteCount: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        colorWatch = findViewById(R.id.colorWatch)
        redCount = findViewById(R.id.redCount)
        blueCount = findViewById(R.id.blueCount)
        whiteCount = findViewById(R.id.whiteCount)

        // Set up the toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        // Make API call to get Tempo data
        val api = TempoApi.create()
        api.getTempoData().enqueue(object : Callback<TempoData> {
            override fun onResponse(call: retrofit2.Call<TempoData>, response: Response<TempoData>) {
                if (response.isSuccessful) {
                    val tempoData = response.body()
                    // Update the UI with Tempo data
                    tempoData?.let {
                        redCount.text = it.redCount.toString()
                        blueCount.text = it.blueCount.toString()
                        whiteCount.text = it.whiteCount.toString()
                        setColorWatch(it)
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<TempoData>, t: Throwable) {
                // Handle error
            }
        })
    }

    private fun setSupportActionBar(toolbar: Toolbar?) {
        TODO("Not yet implemented")
    }

    // Set the color of the color watch based on the color of the day
    private fun setColorWatch(tempoData: TempoData) {
        val color = when {
            tempoData.redCount > tempoData.blueCount && tempoData.redCount > tempoData.whiteCount -> R.color.red
            tempoData.blueCount > tempoData.redCount && tempoData.blueCount > tempoData.whiteCount -> R.color.blue
            else -> R.color.white
        }

        // Create the color watch programmatically
        val colorWatchBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(colorWatchBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = ContextCompat.getColor(this, color)
        canvas.drawCircle(100f, 100f, 90f, paint)

        // Set the color watch bitmap to the ImageView
        colorWatch.setImageBitmap(colorWatchBitmap)
    }

    // Inflate the menu options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Handle menu option selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
        /*return when (item.itemId) {
            R.id.calendar -> {
                // Navigate to the calendar screen
                true
            }
            R.id.parameters -> {
                // Navigate to the parameters screen
                true
            }
            else -> super.onOptionsItemSelected(item)
        }*/
    }
}
