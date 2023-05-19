package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ActivityLoadingBinding
import java.util.*

class LoadingActivity : AppCompatActivity() {
    private val handler = Handler()
    private lateinit var randomTxT: Array<String>
    private lateinit var txt: TextView
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        Glide.with(this).load(R.drawable.loading).into(binding.loadingGif)

        txt = findViewById(R.id.textView)
        randomTxT = resources.getStringArray(R.array.loadingTxt)

        // 초기 텍스트 설정
        updateText()

        // 초마다 텍스트 갱신
        handler.postDelayed(object : Runnable {
            override fun run() {
                currentIndex = (currentIndex + 1) % randomTxT.size
                updateText()
                handler.postDelayed(this, 7000) // 7초마다 텍스트 갱신
            }
        }, 7000) // 1초 후에 시작
    }

    private fun updateText() {
        txt.text = randomTxT[currentIndex]
        txt.gravity = Gravity.CENTER
    }
}