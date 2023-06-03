package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityListBinding

class ListActivity : AppCompatActivity() {
    private val dbHelper = MyDatabase.MyDbHelper(this)
    override fun onBackPressed() {//뒤로가기 누르면 main으로 이동
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //actionbar 뒤로가기 버튼 누르면 main으로 이동
                startActivity(Intent(this, ListActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setTitle("내 그림책 목록")

        val getList = dbHelper.selectAll()
        val adapter = MyAdapter(getList)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter
        //binding.recyclerView.addItemDecoration(DividerItemDecoration(this, GridLayoutManager.HORIZONTAL))

        if (adapter.itemCount == 0) {
            binding.noBooksTextView.visibility = View.VISIBLE
        }

        // read 이동, 삭제 기능 추가 예정
    }
}