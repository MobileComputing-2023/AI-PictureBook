package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.myapplication.databinding.ActivityCreateBinding

class CreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "AI 그림 그리기"

        val summary = intent.getStringExtra("summary")
        val originalsummary = intent.getStringExtra("originalsummary")

        binding.translatedResponseBodyTextView.text = "번역 후: $summary"
        binding.originalResponseBodyTextView.text = "번역 전: $originalsummary"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0,1,0,"다시 생성하기")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            } //여기에 AI 그림 다시 생성하는 코드 추가해야함
        }
        return super.onOptionsItemSelected(item)
    }
}
