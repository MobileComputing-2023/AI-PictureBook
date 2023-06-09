package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import com.example.myapplication.databinding.ActivitySubBinding
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class SubActivity : AppCompatActivity() {
    private lateinit var dbHelper: MyDatabase.MyDbHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var saveButton: Button
    private lateinit var AIsaveButton: Button

    override fun onBackPressed() {
        startActivity(Intent(this, SettingActivity::class.java))
        overridePendingTransition(R.anim.fromleft_toright, R.anim.none)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySubBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(R.anim.fromright_toleft, R.anim.none)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true); // 뒤로가기
        getSupportActionBar()?.setTitle("그림책 콘티 확인하기");

        // 레이아웃 화면에 띄움
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
        val customGenre = intent.getStringExtra("customGenre")
        val customEra = intent.getStringExtra("customEra")

        binding.btnDraw.setOnClickListener { // 직접 그리기
            val intent: Intent = Intent(this, DrawActivity::class.java) // DrawActivity로 이동
            startActivity(intent)
        }

        binding.btnAI.setOnClickListener { // AI 그리기
            val intent: Intent = Intent(this, CreateActivity::class.java) // CreateActivity로 이동
            startActivity(intent)
        }

        // DB 헬퍼 초기화
        dbHelper = MyDatabase.MyDbHelper(this)
        db = dbHelper.writableDatabase

        // 뷰 초기화
        saveButton = findViewById<Button>(R.id.btnDraw)
        AIsaveButton = findViewById<Button>(R.id.btnAI)

        // 저장 버튼 클릭 이벤트 처리
        saveButton.setOnClickListener {
            val bookId = generateBookId()
            val title = extractTitle(summary ?: "") // 첫 줄을 타이틀로 추출
            val textLines = summary?.split("[.!?\\r\\n]".toRegex()) // summary 자름
                ?.filter { it.isNotBlank()} // 공백 삭제
                ?.map { if (it.startsWith("제목:")) it.substring(4) else it } // 제목에서 제목: 삭제
                ?.map { if (it.startsWith("제목 :")) it.substring(4) else it } // 제목에서 제목 : 삭제
                ?.map { it.replace("\"", "")}// 각 줄을 나누어 리스트로 가져옴(공백, "", 제목: 는 무시)
            val textLinesCount = textLines?.size ?: 0 // 0부터 세니까 -1 해서 넘겨야 함
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
            val intent: Intent = Intent(this, DrawActivity::class.java) // DrawActivity로 값 넘김
            intent.putExtra("bookId", bookId)
            intent.putExtra("title", title)
            intent.putExtra("lastPageId", textLinesCount - 1)
            startActivity(intent)

            overridePendingTransition(R.anim.fromright_toleft, R.anim.none) // 화면 전환 애니메이션
        }

        // AI 그리기 부분
        AIsaveButton.setOnClickListener {

            val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            editor.putInt("nextPromptIndex", 0)
            editor.apply()

            val bookId = generateBookId()
            val title = extractTitle(summary ?: "") // 첫 줄을 타이틀로 추출
            val textLines = summary?.split("[.!?\\r\\n]".toRegex())
                ?.filter { it.isNotBlank() && !it.contains("'") && !it.contains("\"") } // 각 줄을 나누어 리스트로 가져옴(공백, '', ""는 무시)
            val textLinesCount = textLines?.size ?: 0 //0부터 세니까 -1 해서 넘겨야함
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

            val intent: Intent = Intent(this, CreateActivity::class.java) // CreateActivity에 값을 넘김
            intent.putExtra("bookId", bookId)
            intent.putExtra("title", title)
            intent.putExtra("lastPageId", textLinesCount-1)
            intent.putExtra("summary", summary)
            intent.putExtra("originalsummary", originalsummary)
            intent.putExtra("selectedGenre", selectedGenre)
            intent.putExtra("selectedEra", selectedEra)
            intent.putExtra("customEra", customEra)
            intent.putExtra("customGenre", customGenre)

            startActivity(intent)
            overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
        }
    }

    //전체 라인을 구분하고 첫번째 줄("제목:"은 제외)
    private fun extractTitle(summary: String?): String {
        val lines = summary?.split("[.!?\\r\\n]".toRegex())
            ?.filter { it.isNotBlank()}
            ?.map { if (it.startsWith("제목:")) it.substring(4) else it }
            ?.map { if (it.startsWith("제목 :")) it.substring(4) else it }
            ?.map { it.replace("\"", "")}
        var title = ""
        if (!lines.isNullOrEmpty()) {
            title = lines[0]
        }
        return title

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

    private fun checkDrawDataExists(bookId: String, pageId: Int): Boolean { // 같은 DB 데이터가 있는지 확인
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
                startActivity(Intent(this, ErrorActivity::class.java))
            }
        }

    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    // bookid는 생성 시점의 년월분초
    private fun generateBookId(): String {
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val formattedTime = dateFormat.format(Date(currentTime))

        return formattedTime
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // 액션 바에 있는 뒤로가기 버튼
        when (item.itemId) {
            android.R.id.home -> {
                //뒤로 가기 버튼 누르면 setting으로 이동
                startActivity(Intent(this, SettingActivity::class.java))
                overridePendingTransition(R.anim.fromleft_toright, R.anim.none)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}