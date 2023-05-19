package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.myapplication.databinding.ActivitySettingBinding
import okhttp3.*

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

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
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val apiKey = "myKey"
        val url = "https://api.openai.com/v1/engines/text-davinci-003/completions"

        val requestBody = """
        {
            "prompt": "The characters are two handsome men, one pretty woman, the background is Rome, the times are 1500s, and the plot is about the universe, so please write short 10 sentences",
            "max_tokens": 500,
            "temperature": 0
        }
    """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle request failure
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val jsonObject = JSONObject(body)
                val jsonArray = jsonObject.getJSONArray("choices")
                val textResult = jsonArray.getJSONObject(0).getString("text")
                callback(textResult)
            }

        })
    }
}