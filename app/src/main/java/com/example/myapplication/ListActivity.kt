package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.ActivityListBinding

class ListActivity : AppCompatActivity() {
    private lateinit var bookTitles: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setTitle("내 그림책 목록")

        bookTitles = resources.getStringArray(R.array.book_titles)

        val adapter = BookListAdapter(bookTitles)
        binding.buttonRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.buttonRecyclerView.adapter = adapter

        if (bookTitles.isEmpty()) {
            binding.noBooksTextView.visibility = View.VISIBLE
        }
    }
}