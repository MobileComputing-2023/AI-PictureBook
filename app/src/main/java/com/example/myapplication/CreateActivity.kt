package com.example.myapplication

import MyDatabase
import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
    private lateinit var dbHelper: MyDatabase.MyDbHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var bookId: String
    private var lastPageId: Int = 0
    private lateinit var title: String

    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView
    private lateinit var generatedImagesGrid: GridLayout

    private val apiKey = "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"
    private val numImages = 4
    private var nextPromptIndex: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_generate_image)

        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val originalSummary = sharedPrefs.getString("originalSummary", "")
        val bookId = sharedPrefs.getString("bookId", "")
        val title = sharedPrefs.getString("title", "")
        val lastPageId = sharedPrefs.getString("lastPageId", "")

        val summary = sharedPrefs.getString("summary", "")

        nextPromptIndex = intent.getIntExtra("nextPromptIndex", 1)

        initializeViews()
        extractIntentData(lastPageId, bookId, title)
        setupActionBar()

        generateImagesFromSummary(originalSummary)
        generateLinesFromSummary(summary, nextPromptIndex)

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

    private fun extractIntentData(lastPageId: String?, bookId: String?, title: String?) {
        this.lastPageId = intent.getIntExtra("lastPageId", 0)
        this.bookId = intent.getStringExtra("bookId") ?: ""
        this.title = intent.getStringExtra("title") ?: ""

        Log.d("BookID", this.bookId)
        Log.d("title", this.title)
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
                Log.d("CreateActivity", line)
                generatedImageText1.text = line
            }

            Log.d("지금 몇 번째 문장을 띄우고 있나요???", index.toString())
            Log.d("4장 사진 뜰 때 보이는 한글 문장.", summary)

        }
    }

    private fun generateImagesFromSummary(originalSummary: String?) {
        val textLines = originalSummary?.split("[.!?\\r\\n]".toRegex())
            ?.filter { it.isNotBlank() && !it.contains("'") && !it.contains("\"") }

        textLines?.let { lines ->
            lines.forEach { line ->
                Log.d("CreateActivity", line)
            }

            val prompt = buildPrompt(lines, nextPromptIndex)
            generateImages(prompt)

            Log.d("지금 몇 번째 문장으로 그림을 만들었나요? (generateImagesFromSummary)", nextPromptIndex.toString())

        } ?: Log.d("CreateActivity", "textLines is null")
    }


    private fun buildPrompt(lines: List<String>, nextPromptIndex: Int): String {
        val promptIndex = nextPromptIndex % lines.size
        return lines.getOrNull(promptIndex) ?: ""
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
                    displayImages(imageUrls)
                    setClickListeners(imageUrls)
                }
            }
        })
    }

    // 응답에서 이미지 URL 추출
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

    private fun displayImages(imageUrls: List<String>) {
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

    private fun setClickListeners(imageUrls: List<String>) {
        imageView1.setOnClickListener {
            if (imageUrls.isNotEmpty()) {
                showImageDetail(imageUrls[0])
            }
        }
        imageView2.setOnClickListener {
            if (imageUrls.size > 1) {
                showImageDetail(imageUrls[1])
            }
        }
        imageView3.setOnClickListener {
            if (imageUrls.size > 2) {
                showImageDetail(imageUrls[2])
            }
        }
        imageView4.setOnClickListener {
            if (imageUrls.size > 3) {
                showImageDetail(imageUrls[3])
            }
        }
    }

    private fun showImageDetail(imageUrl: String) {
        val fragment = ImageDetailFragment()
        val bundle = Bundle()
        bundle.putString("imageUrl", imageUrl)
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }
}