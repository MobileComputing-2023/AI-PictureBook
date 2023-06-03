package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var backPressedTime: Long = 0
    private val backPressedTimeout: Long = 2000
    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime < backPressedTimeout) {
            // 앱 종료
            finishAffinity()
        } else {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.settingBtn.setOnClickListener {
            val intent: Intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        binding.readBookListBtn.setOnClickListener {
            val intent: Intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }
    }
}