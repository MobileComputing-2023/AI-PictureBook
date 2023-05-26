package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.databinding.ActivitySettingBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private var numMan = 0
    private var numWoman = 0

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "그림책 만들기"

        val outputMan = findViewById<TextView>(R.id.NumMan)
        val outputWoman = findViewById<TextView>(R.id.NumWoman)

        updateOutputText(outputMan, numMan)
        updateOutputText(outputWoman, numWoman)

        binding.minusMan.setOnClickListener {
            if (numMan > 0) {
                numMan--
            }
            updateOutputText(outputMan, numMan)
        }

        binding.plusMan.setOnClickListener {
            numMan++
            updateOutputText(outputMan, numMan)
        }

        binding.minusWoman.setOnClickListener {
            if (numWoman > 0) {
                numWoman--
            }
            updateOutputText(outputWoman, numWoman)
        }

        binding.plusWoman.setOnClickListener {
            numWoman++
            updateOutputText(outputWoman, numWoman)
        }

        binding.btnwrite.setOnClickListener {
            val selectedGenre = binding.genre.selectedItem.toString()
            val selectedEra = binding.era.selectedItem.toString()
            val writesumText = binding.writesum.text.toString()

            if (writesumText.length > 200) {
                Toast.makeText(this@SettingActivity, "글자 수를 200자 이내로 제한해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent: Intent = Intent(this@SettingActivity, LoadingActivity::class.java)
            startActivity(intent)

            runGPT3(selectedGenre, selectedEra, numMan, numWoman, writesumText) { responseBody ->
                val intent = Intent(this@SettingActivity, SubActivity::class.java).apply {
                    putExtra("next", "level")
                    putExtra("selectedGenre", selectedGenre)
                    putExtra("selectedEra", selectedEra)
                    putExtra("NumMan", numMan)
                    putExtra("NumWoman", numWoman)
                    putExtra("num", 30)
                    putExtra("key", writesumText)
                    putExtra("summary", responseBody)
                }
                startActivity(intent)
            }
        }
    }

    private fun updateOutputText(textView: TextView, value: Int) {
        textView.text = value.toString()
    }

    private fun runGPT3(
        selectedGenre: String,
        selectedEra: String,
        numMan: Int,
        numWoman: Int,
        writesumText: String,
        callback: (String) -> Unit
    ) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val apiKey = "mykey"
        val url = "https://api.openai.com/v1/chat/completions"

        val requestBody = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {"role": "user", "content": "The following are the default rules. Keep this rule no matter what."},
                    {"role": "user", "content": "Write a novel that contains the ending and is probable."},
                    {"role": "system", "content": "1. All results are printed in Korean."},
                    {"role": "system", "content": "2. Limit novels to a maximum of 10 sentences."},
                    {"role": "system", "content": "3. Novel creation time is up to 50 seconds."},
                    {"role": "system", "content": "4. Every novel's sentence should be clearly written, and the story should be smooth without the wrong words."},
                    {"role": "user", "content": "등장인물: 남자 `$numMan`명, 여자 `$numWoman`명, 시대: $selectedEra, 장르: $selectedGenre, 줄거리: `$writesumText`로 작성해 줘."}
                ]
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
                if (body != null) {
                    Log.d("API Response Body", body)
                    val jsonObject = JSONObject(body)
                    val jsonArray = jsonObject.getJSONArray("choices")
                    val content = jsonArray.getJSONObject(0).getJSONObject("message").getString("content")
                    callback(content)
                }
            }
        })
    }
}
