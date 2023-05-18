package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.myapplication.databinding.ActivitySubBinding

class SubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setTitle("그림책 콘티 확인하기");
        val numMan = intent.getIntExtra("NumMan", 0)
        binding.NManView.text = "남자 수: $numMan"

        val numWoman = intent.getIntExtra("NumWoman", 0)
        binding.NWomanView.text = "여자 수: $numWoman"

        val selectedGenre = intent.getStringExtra("selectedGenre")
        binding.genreView.text = "장르: $selectedGenre"

        val selectedEra = intent.getStringExtra("selectedEra")
        binding.eraView.text = "시대: $selectedEra"

        binding.sumView.text = "이야기 줄거리: ${intent.getStringExtra("summary")}"

        binding.btncreate.setOnClickListener {
            val intent: Intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}