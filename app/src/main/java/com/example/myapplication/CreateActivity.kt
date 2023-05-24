package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.myapplication.databinding.ActivityCreateBinding

class CreateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
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