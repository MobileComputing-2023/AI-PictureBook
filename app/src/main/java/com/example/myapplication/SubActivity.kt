package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import com.example.myapplication.databinding.ActivitySubBinding
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class SubActivity : AppCompatActivity() {
    private lateinit var dbHelper: MyDatabase.MyDbHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var saveButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true); //뒤로가기
        getSupportActionBar()?.setTitle("그림책 콘티 확인하기");
        val numMan = intent.getIntExtra("NumMan", 0)
        binding.NManView.text = "남자 수: $numMan"

        val numWoman = intent.getIntExtra("NumWoman", 0)
        binding.NWomanView.text = "여자 수: $numWoman"

        val selectedGenre = intent.getStringExtra("selectedGenre")
        binding.genreView.text = "장르: $selectedGenre"

        val selectedEra = intent.getStringExtra("selectedEra")
        binding.eraView.text = "시대: $selectedEra"

        binding.sumView.text = "이야기 줄거리: ${intent.getStringExtra("key")}"

        val summary = intent.getStringExtra("summary")
        binding.summaryTextView.text = "$summary"

        val originalsummary = intent.getStringExtra("originalsummary")

        binding.btnDraw.setOnClickListener {
            val intent: Intent = Intent(this, DrawActivity::class.java)
            startActivity(intent)
        }

        binding.btnAI.setOnClickListener {
            val intent: Intent = Intent(this, CreateActivity::class.java).apply {
                val summary = intent.getStringExtra("summary")
                val originalsummary = intent.getStringExtra("originalsummary")
                putExtra("summary", summary)
                putExtra("originalsummary", originalsummary)
            }
            startActivity(intent)
        }
        // DB 헬퍼 초기화
        dbHelper = MyDatabase.MyDbHelper(this)
        db = dbHelper.writableDatabase

        // 뷰 초기화
        saveButton = findViewById<Button>(R.id.btnDraw)

        // 저장 버튼 클릭 이벤트 처리
        saveButton.setOnClickListener {
            val bookId = generateBookId()
            val title = extractFirstLine(summary ?: "") // 첫 줄을 타이틀로 추출
            val textLines = summary?.split("[.!?\\r\\n]".toRegex())?.filter { it.isNotBlank() }// 각 줄을 나누어 리스트로 가져옴
            val textLinesCount = textLines?.size ?: 0
            // DB에 Book 데이터 삽입
            insertBookData(bookId, title)

            // DB에 Draw 데이터 삽입
            if (textLines != null) {
                for (i in textLines.indices) {
                    val line = textLines[i]
                    val pageId = i
                    insertDrawData(pageId, bookId, line.trim())
                }
            }

            Toast.makeText(this, "Data saved successfully.", Toast.LENGTH_SHORT).show()

            val intent: Intent = Intent(this, DrawActivity::class.java)
            intent.putExtra("bookId", bookId)
            intent.putExtra("title", title)
            intent.putExtra("lastPageId", textLinesCount)
            startActivity(intent)
        }
    }
    private fun extractFirstLine(text: String): String {
        val firstLineEnd = text.indexOf('\n') // 첫 번째 줄의 개행 문자 위치
        return if (firstLineEnd != -1) {
            text.substring(0, firstLineEnd) // 개행 문자 이전까지의 부분 문자열 반환
        } else {
            text // 개행 문자가 없으면 전체 문자열 반환
        }
    }

    private fun insertBookData(bookId: String, title: String) {
        val values = ContentValues().apply {
            put(MyDatabase.MyDBContract.BookEntry.COLUMN_BOOK_ID, bookId)
            put(MyDatabase.MyDBContract.BookEntry.COLUMN_TITLE, title)
        }

        val newRowId = db.insert(MyDatabase.MyDBContract.BookEntry.TABLE_NAME, null, values)
        if (newRowId != -1L) {
            Log.d("DB", "Book data inserted successfully. Book ID: $bookId, Title: $title")
        } else {
            Log.d("DB", "Failed to insert book data.")
        }
    }

    private fun checkDrawDataExists(bookId: String, pageId: Int): Boolean {
        val selection = "${MyDatabase.MyDBContract.DrawEntry.COLUMN_BOOK_ID} = ? AND ${MyDatabase.MyDBContract.DrawEntry.COLUMN_PAGE_ID} = ?"
        val selectionArgs = arrayOf(bookId, pageId.toString())

        val cursor = db.query(
            MyDatabase.MyDBContract.DrawEntry.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val exists = cursor.count > 0

        cursor.close()

        return exists
    }



    private fun insertDrawData(pageId: Int, bookId: String, text: String) {
        if (checkDrawDataExists(bookId, pageId)) {
            Log.d("DB", "Draw data already exists for Page ID: $pageId, Book ID: $bookId")
            return  // 이미 데이터가 존재하면 함수를 종료
        }
        else {
            Log.d("DB", "$pageId, Book ID: $bookId, Text: $text")
            val values = ContentValues().apply {
                put(MyDatabase.MyDBContract.DrawEntry.COLUMN_PAGE_ID, pageId)
                put(MyDatabase.MyDBContract.DrawEntry.COLUMN_BOOK_ID, bookId)
                put(MyDatabase.MyDBContract.DrawEntry.COLUMN_TEXT, text)
            }

            val newRowId = db.insert(MyDatabase.MyDBContract.DrawEntry.TABLE_NAME, null, values)
            if (newRowId != -1L) {
                Log.d("DB", "Draw data inserted successfully. Page ID: $pageId, Book ID: $bookId, Text: $text")
            } else {
                Log.d("DB", "Failed to insert draw data.")
            }
        }

    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    private fun generateBookId(): String {
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val formattedTime = dateFormat.format(Date(currentTime))

        return formattedTime
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