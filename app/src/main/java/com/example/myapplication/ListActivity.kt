package com.example.myapplication

import MyDatabase
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.ActivityListBinding

class ListActivity : AppCompatActivity() {
    private val dbHelper = MyDatabase.MyDbHelper(this)
    private var isDeleteMenuChecked:Boolean = false
    override fun onBackPressed() {//뒤로가기 누르면 main으로 이동
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0,1,0,"삭제하기")
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //actionbar 뒤로가기 버튼 누르면 main으로 이동
                startActivity(Intent(this, ListActivity::class.java))
                return true
            }
            1 -> {
                Log.d("TAG", "DELETE TAB CLICKED")
                isDeleteMenuChecked = true
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

        if (adapter.itemCount == 0) {
            binding.noBooksTextView.visibility = View.VISIBLE
        } else {
            binding.noBooksTextView.visibility = View.GONE
        }

        adapter.setItemClickListener(object : MyAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                if (!isDeleteMenuChecked) {
                    val selectedElement = adapter.getElement(position)
                    val bookId = selectedElement.bookId.toString()

                    val intent = Intent(this@ListActivity, ReadActivity::class.java)
                    intent.putExtra("bookId", bookId)
                    startActivity(intent)
                } else {
                    adapter.setDeletePosition(position)

                    Toast.makeText(applicationContext, "삭제하시겠습니까?", Toast.LENGTH_SHORT)

                    var db = dbHelper.writableDatabase
                    db?.delete(MyDatabase.MyDBContract.BookEntry.TABLE_NAME, "${MyDatabase.MyDBContract.BookEntry.COLUMN_BOOK_ID}=?",
                        arrayOf(adapter.getElement(position).bookId.toString())
                    )
                    val newList = dbHelper.selectAll()
                    adapter.setList(newList)
                    adapter.notifyDataSetChanged()

                    adapter.setDeletePosition(-1)
                    isDeleteMenuChecked = false
                }
            }
        })
    }

}
