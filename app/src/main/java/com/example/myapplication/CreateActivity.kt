package com.example.myapplication

import MyDatabase
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.shimmer.ShimmerFrameLayout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class CreateActivity : AppCompatActivity() {

    private lateinit var generatedImageText1: TextView
    private lateinit var myDatabase: MyDatabase

    private lateinit var bookId: String
    private var lastPageId: Int = 0
    private lateinit var title: String

    private var nextPromptIndex: Int = 0

    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView
    private lateinit var generatedImagesGrid: GridLayout

    private val apiKey = "mykey"
    private val numImages = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_generate_image)

        val intent = intent
        val originalsummary = intent.getStringExtra("originalsummary")
        val summary = intent.getStringExtra("summary")
        this.bookId = intent.getStringExtra("bookId") ?: ""
        this.lastPageId = intent.getIntExtra("lastPageId", 0)
        this.title = intent.getStringExtra("title") ?: ""
        myDatabase = MyDatabase.getInstance(this)

        nextPromptIndex = intent.getIntExtra("nextPromptIndex", 0)

        initializeViews()
        setupActionBar()

        generateImagesFromSummary(originalsummary, nextPromptIndex)
        generateLinesFromSummary(summary, nextPromptIndex)

        if (originalsummary != null) {
            Log.d("originalsummary", originalsummary)
        } else {
            Log.d("originalsummary", "originalsummary null")
        }
        Log.d("nextPromptIndex", nextPromptIndex.toString())
        if (summary != null) {
            Log.d("summary", summary)
        } else {
            Log.d("summary", "summary null")
        }
    }

    private fun initializeViews() {
        shimmerLayout = findViewById(R.id.shimmerLayout)
        imageView1 = findViewById(R.id.generatedImageView1)
        imageView2 = findViewById(R.id.generatedImageView2)
        imageView3 = findViewById(R.id.generatedImageView3)
        imageView4 = findViewById(R.id.generatedImageView4)
        generatedImagesGrid = findViewById(R.id.generatedImagesGrid)
        generatedImageText1 = findViewById(R.id.generatedImageText1)
    }

    private fun setupActionBar() {
        supportActionBar?.title = "$title"
    }

    private fun generateLinesFromSummary(summary: String?, index: Int) {
        val textLines = summary?.split("[.!?\\r\\n]".toRegex())
            ?.filter { it.isNotBlank() && !it.contains("'") && !it.contains("\"") }

        textLines?.let { lines ->
            if (index >= 0 && index < lines.size) {
                val line = lines[index]
                Log.d("CreateActivity_한국어", line)
                generatedImageText1.text = line
            } else {
                Log.d("문장 끝", index.toString())
            }

            Log.d("지금 몇 번째 문장을 띄우고 있나요???", index.toString())
            Log.d("4장 사진 뜰 때 보이는 한글 문장.", lines[index])

        }
    }

    private fun generateImagesFromSummary(originalSummary: String?, index: Int) {
        val textLines = originalSummary?.split("[.!?\\r\\n]".toRegex())
            ?.filter { it.isNotBlank() && !it.contains("'") && !it.contains("\"") }

        textLines?.let { lines ->
            if (index >= 0 && index < lines.size) {
                val line = lines[index]
                Log.d("CreateActivity_영어", line)
                generateImages(line)

                Log.d("지금 이 영어 문장으로 만들어졌어요", line)
            } else if (index == lines.size){
                val line = lines.lastOrNull()
                if(line != null){
                    Log.d("CreateActivity_영어, 마지막", line)
                    generateImages(line)
                    Log.d("지금 이 영어 문장(마지막)으로 만들어졌어요", line)
                }
            }

            Log.d("지금 몇 번째 문장으로 그림을 만들었나요? (generateImagesFromSummary)", index.toString())

        } ?: Log.d("CreateActivity", "textLines is null")
    }


    private fun generateImages(prompt: String) {

        Log.d("지금 무슨 문장으로 이미지를 만들고 있나요?", prompt)

        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        val client = OkHttpClient()

        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBodyJson = JSONObject()
        requestBodyJson.put("n", numImages)
        requestBodyJson.put("prompt", prompt)
        requestBodyJson.put("size", "256x256")

        Log.d("CreateActivity", "Request Body: $requestBodyJson")

        val request = Request.Builder()
            .url("https://api.openai.com/v1/images/generations")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .post(requestBodyJson.toString().toRequestBody(mediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 요청 실패 처리
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                Log.d("CreateActivity", "Response Body: $json")
                val imageUrls = extractImageUrlsFromResponse(json)

                runOnUiThread {
                    displayImages(imageUrls, prompt)
                    setClickListeners(imageUrls, prompt)
                }
            }
        })
    }

    private fun extractImageUrlsFromResponse(json: String?): List<String> {
        val urls = mutableListOf<String>()

        try {
            val jsonObject = JSONObject(json)
            val dataArray = jsonObject.getJSONArray("data")

            for (i in 0 until dataArray.length()) {
                val dataObject = dataArray.getJSONObject(i)
                val url = dataObject.getString("url")
                urls.add(url)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return urls
    }

    private fun displayImages(imageUrls: List<String>, prompt: String) {
        val imageViews: List<ImageView> = listOf(imageView1, imageView2, imageView3, imageView4)

        for (i in imageUrls.indices) {
            val url = imageUrls[i]
            val imageView = imageViews[i]

            Glide.with(this)
                .load(url)
                .apply(RequestOptions.overrideOf(200, 200))
                .into(imageView)
        }

        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
        generatedImagesGrid.visibility = View.VISIBLE
    }

    private fun setClickListeners(imageUrls: List<String>, prompt: String) {
        imageView1.setOnClickListener {
            if (imageUrls.isNotEmpty()) {
                showImageDetail(imageUrls[0], prompt)
            }
        }
        imageView2.setOnClickListener {
            if (imageUrls.size > 1) {
                showImageDetail(imageUrls[1], prompt)
            }
        }
        imageView3.setOnClickListener {
            if (imageUrls.size > 2) {
                showImageDetail(imageUrls[2], prompt)
            }
        }
        imageView4.setOnClickListener {
            if (imageUrls.size > 3) {
                showImageDetail(imageUrls[3], prompt)
            }
        }
    }

    private fun showImageDetail(imageUrl: String, prompt: String) {
        val fragment = ImageDetailFragment()
        val bundle = Bundle().apply {
            putString("imageUrl", imageUrl)
            putString("prompt", prompt)
            putString("bookId", bookId) // bookId 전달
            putInt("lastPageId", lastPageId) // lastPageId 전달
            putInt("nextPromptIndex", nextPromptIndex) // Pass the current nextPromptIndex value
        }
        fragment.arguments = bundle

        Log.d("showImageDetail에 전달되는 nextPromptIndex", nextPromptIndex.toString())

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }


    private fun showPopupActivity() {
        val intent = Intent(this, PopupActivity::class.java)
        startActivity(intent)
    }





}
