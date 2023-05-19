package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.myapplication.databinding.ActivitySettingBinding

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setTitle("그림책 만들기")
        var minMan = findViewById<Button>(R.id.minusMan)
        var plusMan = findViewById<Button>(R.id.plusMan)
        var minWoman = findViewById<Button>(R.id.minusWoman)
        var plusWoman = findViewById<Button>(R.id.plusWoman)
        val output_man = findViewById<TextView>(R.id.NumMan)
        val output_woman = findViewById<TextView>(R.id.NumWoman)
        var NumMan = 0;
        var NumWoman = 0;

        output_man.text = NumMan.toString()
        output_woman.text = NumWoman.toString()

        minMan.setOnClickListener {
            if (NumMan == 0) {
                output_man.setText(NumMan.toString())
            } else {
                NumMan-- //0일때 처리 해줘야함
                output_man.setText(NumMan.toString())
            }
        }
        plusMan.setOnClickListener {
            NumMan++
            output_man.setText(NumMan.toString())
        }
        minWoman.setOnClickListener {
            NumWoman-- //0일때 처리 해줘야함
            output_woman.setText(NumWoman.toString())
        }
        plusWoman.setOnClickListener {
            NumWoman++
            output_woman.setText(NumWoman.toString())
        }

        binding.btnwrite.setOnClickListener {
            val selectedGenre = binding.genre.selectedItem.toString()
            val selectedEra = binding.era.selectedItem.toString()

            runGPT3 { responseBody ->
                val intent = Intent(this@SettingActivity, SubActivity::class.java).apply {
                    putExtra("next", "level")
                    putExtra("selectedGenre", selectedGenre)
                    putExtra("selectedEra", selectedEra)
                    putExtra("NumMan", NumMan)
                    putExtra("NumWoman", NumWoman)
                    putExtra("num", 30)
                    putExtra("key", binding.writesum.text.toString())
                    putExtra("summary", responseBody)
                }
                startActivity(intent)
            }

        }
    }

    private fun runGPT3(callback: (String) -> Unit) {
        val client = OkHttpClient()
        val apiKey = "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" // Replace with your OpenAI API key
        val url = "https://api.openai.com/v1/chat/completions"
        val json = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {"role": "user", "content": "Can you help me with writing a story?"}
                ]
            }
        """.trimIndent()

        val mediaType = "application/json".toMediaType()
        val requestBody = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Handle request failure
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody)
                val choicesArray = jsonResponse.getJSONArray("choices")
                val messageContent = choicesArray.getJSONObject(0).getJSONObject("message").getString("content")

                callback(messageContent ?: "")
            }

        })
    }
}