package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
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

    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView

    private val apiKey = "mykey"
    private val prompt = "Background: Future, Genre: Fantasy, Draw Background Images, Content: Poor developer Jimmy who was eaten by a programming monster."
    private val numImages = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_generate_image)

        shimmerLayout = findViewById(R.id.shimmerLayout)
        imageView1 = findViewById(R.id.generatedImageView1)
        imageView2 = findViewById(R.id.generatedImageView2)
        imageView3 = findViewById(R.id.generatedImageView3)
        imageView4 = findViewById(R.id.generatedImageView4)

        generateImages()
    }

    private fun generateImages() {

        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        val client = OkHttpClient()

        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBodyJson = JSONObject()
        requestBodyJson.put("n", numImages)
        requestBodyJson.put("prompt", prompt)
        requestBodyJson.put("size", "512x512")

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
        val imageViews = listOf(imageView1, imageView2, imageView3, imageView4)

        for (i in imageUrls.indices) {
            val url = imageUrls[i]
            val imageView = imageViews[i]

            Glide.with(this)
                .load(url)
                .apply(RequestOptions.overrideOf(200, 200)) // 필요에 따라 이미지 크기 조정
                .into(imageView)
        }

        shimmerLayout.visibility = View.GONE
        shimmerLayout.stopShimmer()
    }
}
