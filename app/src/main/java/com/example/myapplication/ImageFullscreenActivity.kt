package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ImageFullscreenActivity : AppCompatActivity() {

    private lateinit var bookId: String
    private var lastPageId: Int = 0
    private lateinit var title: String
    private lateinit var summary: String
    private var textLinesCount: Int = 1

    private lateinit var fullscreenImageView: ImageView
    private var nextPromptIndex: Int = 1
    private lateinit var originalsummary: String

    private lateinit var textBox: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_fullscreen)

        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val summary = sharedPrefs.getString("summary", "")

        bookId = intent.getStringExtra("bookId") ?: ""
        title = intent.getStringExtra("title") ?: ""
        lastPageId = intent.getIntExtra("lastPageId", 0)
        originalsummary = intent.getStringExtra("originalsummary") ?: ""
        nextPromptIndex = intent.getIntExtra("nextPromptIndex", 1)

        fullscreenImageView = findViewById(R.id.fullImageView)

        textBox = findViewById<TextView>(R.id.textBox)
        displayLineFromSummary(summary, nextPromptIndex)

        val imageUrl = intent.getStringExtra("imageUrl")

        Glide.with(this)
            .load(imageUrl)
            .into(fullscreenImageView)

        val downloadButton = findViewById<Button>(R.id.downloadButton)
        downloadButton.setOnClickListener {

            Log.d("지금 몇 번째 문장을 읽고 있나요? (전)", nextPromptIndex.toString())

            nextPromptIndex++
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra("nextPromptIndex", nextPromptIndex)

            Log.d("지금 몇 번째 문장을 읽고 있나요?", nextPromptIndex.toString())
            displayLineFromSummary(summary, nextPromptIndex)
            startActivity(intent)
        }
    }

    private fun displayLineFromSummary(summary: String?, index: Int) {
        val textLines = summary?.split("[.!?\\r\\n]".toRegex())
            ?.filter { it.isNotBlank() && !it.contains("'") && !it.contains("\"") }

        textLines?.let { lines ->
            if (index >= 0 && index < lines.size) {
                val line = lines[index]
                Log.d("CreateActivity", line)
                textBox.text = line
            }
        }

        Log.d("지금 몇 번째 문장을 띄우고 있나요?", index.toString())
    }


}
