package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
            val writesumText = binding.writesum.text.toString()

            if (writesumText.length > 200) {
                // 글자 수가 200자를 초과하는 경우 처리
                // 예를 들어, 사용자에게 알림 메시지를 보여줄 수 있습니다.
                Toast.makeText(this@SettingActivity, "글자 수를 200자 이내로 제한해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent: Intent = Intent(this@SettingActivity, LoadingActivity::class.java)
            startActivity(intent)

            runGPT3 { responseBody ->
                val intent = Intent(this@SettingActivity, SubActivity::class.java).apply {
                    putExtra("next", "level")
                    putExtra("selectedGenre", selectedGenre)
                    putExtra("selectedEra", selectedEra)
                    putExtra("NumMan", NumMan)
                    putExtra("NumWoman", NumWoman)
                    putExtra("num", 30)
                    putExtra("key", writesumText)
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

        val apiKey = "mykey"
        val url = "https://api.openai.com/v1/chat/completions"

        val requestBody = """
    {
        "model": "gpt-3.5-turbo",
        "messages": [
            {"role": "user", "content": "This is a basic rule. Defend it no matter what."},
            {"role": "system", "content": "1. All results are printed in Korean."},
            {"role": "system", "content": "2. Limit the novel to a maximum of 10 sentences."},
            {"role": "system", "content": "3. No numbers are added before each sentence."},
            {"role": "system", "content": "4. Print each sentence on a separate line with enter"},
            {"role": "system", "content": "5. Produce sentences as quickly as possible, up to 30 sec."},
            {"role": "user", "content": "등장인물: 남자 1명, 여자 1명, 시대는 과거, 장르는 판타지, 아주 재미있는 농구 소설을 작성해 줘."}
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