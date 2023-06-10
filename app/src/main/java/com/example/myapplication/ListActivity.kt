package com.example.myapplication

import MyDatabase
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.ActivityListBinding

class ListActivity : AppCompatActivity() {
    private val dbHelper = MyDatabase.MyDbHelper(this)
    private var isDeleteMenuChecked:Boolean = false
    override fun onBackPressed() {//뒤로가기 누르면 main으로 이동
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(R.anim.fromleft_toright, R.anim.none)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0,1,0,"삭제하기")
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //actionbar 뒤로가기 버튼 누르면 main으로 이동
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(R.anim.fromleft_toright, R.anim.none)
                return true
            }
            1 -> {
                isDeleteMenuChecked = true
                Toast.makeText(this@ListActivity, "삭제하려는 책을 선택해주세요", Toast.LENGTH_SHORT).show()
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
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.noBooksTextView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }

        adapter.setItemClickListener(object : MyAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val selectedElement = adapter.getElement(position)
                val bookId = selectedElement.bookId
                val myDatabase = MyDatabase.getInstance(this@ListActivity)

                if (!isDeleteMenuChecked) {
                    //클릭 시 ReadActivity로 이동
                    val intent = Intent(this@ListActivity, ReadActivity::class.java)
                    intent.putExtra("bookId", bookId)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
                } else {
                    // 삭제하기 선택 후 클릭 시 내용
                    adapter.setDeletePosition(position)

                    // AlertDialog로 사용자에게 확인 받기
                    val builder = AlertDialog.Builder(this@ListActivity)
                        .setTitle("『${myDatabase.getTitle(bookId)}』을 삭제하시겠습니까?")
                        .setMessage("내 그림책 목록에서 이 책을 삭제하시겠습니까? 한 번 삭제한 책은 다시 불러올 수 없습니다.")
                        .setPositiveButton("삭제", DialogInterface.OnClickListener {dialog, which ->
                            // AlertDialog의 삭제 버튼 눌렀을 경우 DB에서 삭제
                            myDatabase.deleteBook(bookId)

                            adapter.setDeletePosition(-1)
                            isDeleteMenuChecked = false

                            val newList = dbHelper.selectAll()
                            adapter.setList(newList)
                            adapter.notifyDataSetChanged()
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener{dialog, which ->
                            // AlertDialog의 취소 버튼 눌렀을 경우 원상태로 복귀
                            adapter.setDeletePosition(-1)
                            isDeleteMenuChecked = false
                        } )
                    builder.show()
                }
            }
        })
    }

}
