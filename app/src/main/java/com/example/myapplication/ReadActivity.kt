package com.example.myapplication

import MyDatabase
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivityReadBinding

class ReadActivity : AppCompatActivity() {
    private lateinit var bookId: String
    private lateinit var myDatabase: MyDatabase
    private var currentPage = 0
    private lateinit var title: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityReadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookId = intent.getStringExtra("bookId") ?: ""
        myDatabase = MyDatabase.getInstance(this)

        title = myDatabase.getTitle(bookId) ?: ""
        supportActionBar?.title = title

        displayImageForPage(binding)

        binding.nextBtn.setOnClickListener {
            displayImageForPage(binding)
            currentPage++
        }
    }
    override fun onBackPressed() { //뒤로가기 버튼 누르면 booklist로 이동
        startActivity(Intent(this, ListActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //actionbar 뒤로가기 버튼 누르면 booklist로 이동
                startActivity(Intent(this, ListActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayImageForPage(binding: ActivityReadBinding) {
        val totalPages = myDatabase.getTotalPages(bookId)

        if (currentPage > totalPages) {
            // 모든 페이지를 그림 데이터로 채웠을 때
            showPopupActivity()
        } else {
            val image = myDatabase.getImageForPage(bookId, currentPage)
            binding.imageView.setImageBitmap(image)
            Log.d("DB","totalPage: $totalPages, currentPage: $currentPage")

        }
    }

    private fun showPopupActivity() {
        val intent = Intent(this, ReadAllPopActivity::class.java).apply {
            putExtra("bookId", bookId)
            putExtra("title", title)
        }
        startActivity(intent)
    }
}