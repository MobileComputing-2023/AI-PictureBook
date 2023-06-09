package com.example.myapplication

import MyDatabase
import TextBoxFragment
import android.R
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityDrawBinding

import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream

class DrawActivity : AppCompatActivity() {
    private lateinit var myDatabase: MyDatabase
    private lateinit var dbHelper: MyDatabase.MyDbHelper
    private lateinit var db: SQLiteDatabase
    private var currentPageId: Int = 0
    private lateinit var myView: MyView
    private var overlayView: View? = null
    private lateinit var bookId: String
    private var lastPageId: Int = 0
    private lateinit var title: String

    // 2초 내에 두 번 backpress하면 sub로 이동
    private var backPressedTime: Long = 0
    private val backPressedTimeout: Long = 2000

    inner class Point(var x: Float, var y: Float, var check: Boolean, var color: Int)

    inner class MyView(context: Context) : View(context) {

        val points = ArrayList<Point>()
        var color = Color.BLACK
        override fun onDraw(canvas: Canvas) {
            val p = Paint().apply {
                strokeWidth = 15f
            }
            for (i in 1 until points.size) {
                val point = points[i]
                if (!point.check) continue
                p.color = point.color
                canvas.drawLine(points[i - 1].x, points[i - 1].y, point.x, point.y, p)
            }
        }
        fun undoLastAction() {
            var lastIndex = -1 //되돌릴 대상이 되는 마지막 획의 인덱스를 저장
            for (i in points.size - 1 downTo 0) {
                if (!points[i].check) { //point 리스트 역순으로, check가 false면 그림 없는 거임
                    lastIndex = i
                    break
                }
            }
            if (lastIndex != -1) { //ㅣlastindex가 -1아니면 지울 그림있음
                val removedPoints = points.subList(lastIndex, points.size).toMutableList() //기존의 리스트나 배열을 변경 가능한 리스트로 변환
                points.removeAll(removedPoints) //지우기
                invalidate() //뷰를 다시 요청. 화면에 변경된 그림 표시

                // 삭제된 획 이전 획으로 마지막 획 인덱스 변경
                lastIndex = -1
                for (i in points.size - 1 downTo 0) {
                    if (!points[i].check) {
                        lastIndex = i
                        break
                    }
                }
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> points.add(Point(x, y, false, color))
                MotionEvent.ACTION_MOVE -> points.add(Point(x, y, true, color))
                MotionEvent.ACTION_UP -> {
                }
            }
            invalidate()
            return true
        }
    }

    override fun onBackPressed() {
        if (currentPageId == 0) {
            myDatabase.deleteBook(bookId)
            finish()
            overridePendingTransition(com.example.myapplication.R.anim.fromleft_toright, com.example.myapplication.R.anim.none)
        } else {
            // currentPageId가 0이 아닌 경우에는 두 번 누르면 뒤로 가기 가능
            if (System.currentTimeMillis() - backPressedTime < backPressedTimeout) {
                myDatabase.deleteBook(bookId)
                finish()
                overridePendingTransition(com.example.myapplication.R.anim.fromleft_toright, com.example.myapplication.R.anim.none)
            } else {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(this, "한 번 더 누르면\n그림책이 저장되지 않고 종료됩니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDrawBinding.inflate(layoutInflater)
        dbHelper = MyDatabase.MyDbHelper(this)
        db = dbHelper.writableDatabase
        myDatabase = MyDatabase.getInstance(this)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        myView = MyView(this)

        lastPageId = intent.getIntExtra("lastPageId", 0)
        bookId = intent.getStringExtra("bookId") ?:""
        title = intent.getStringExtra("title")?:""
        supportActionBar?.title = "$title"

        Log.d("BookID", bookId)
        Log.d("title", title)

        binding.drawLinear.addView(myView)

        binding.clearBtn.setOnClickListener {
            myView.points.clear()
            myView.invalidate()
        }

        binding.colorPickerButton.setOnClickListener {
            openColorPicker(myView)
        }

        binding.nextBtn.setOnClickListener { // next 버튼 누르면 작동
            // 그림 정보를 저장한 후, 다음 페이지로 이동
            saveDrawingDataAndMoveToNextPage()
        }
        binding.undoBtn.setOnClickListener{
            // 직전에 그린 획 지우기
            myView.undoLastAction()
        }

        if (currentPageId == 0) {
            showTextBoxEdit(bookId, currentPageId)
        }

    }

    private fun showTextBoxEdit(bookId: String, currentPageId: Int) {
        val fragment = TextBoxFragment().apply {
            arguments = Bundle().apply {
                putInt("currentPageID", currentPageId)
                putString("bookId", bookId)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun saveDrawingDataAndMoveToNextPage() {

        val bitmap = Bitmap.createBitmap(myView.width, myView.height, Bitmap.Config.ARGB_8888)
        val bitmapCanvas = Canvas(bitmap)
        myView.draw(bitmapCanvas)

        // 비트맵을 바이트 배열로 변환
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        // 이미지를 DB에 저장하는 코드를 추가해야 함
        saveImageToDB(bookId, currentPageId, byteArray)

        myView.points.clear()
        myView.invalidate()

        if (currentPageId == lastPageId) {
            // 모든 페이지를 그림 데이터로 채웠을 때
            showPopupFragment(bookId, title)

        } else {
            currentPageId += 1
            showTextBoxEdit(bookId, currentPageId)
        }

    }
    private fun saveImageToDB(bookId: String, pageId: Int, byteArray: ByteArray) {
        val values = ContentValues().apply {
            put(MyDatabase.MyDBContract.DrawEntry.COLUMN_IMAGE, byteArray)
        }

        val selection = "${MyDatabase.MyDBContract.DrawEntry.COLUMN_BOOK_ID} = ? AND " +
                "${MyDatabase.MyDBContract.DrawEntry.COLUMN_PAGE_ID} = ?"
        val selectionArgs = arrayOf(bookId, pageId.toString())

        val rowsAffected = db.update(
            MyDatabase.MyDBContract.DrawEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs
        )

        if (rowsAffected > 0) {
            Log.d(
                "DB",
                "Image data updated successfully. Book ID: $bookId, Page ID: $pageId Bitmap: $byteArray"
            )
        } else {
            Log.d("DB", "Failed to update image data.")
            val intent: Intent = Intent(this@DrawActivity, ErrorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showPopupFragment(bookId: String, title: String) {
        val fragment = PopupFragment().apply {
            arguments = Bundle().apply {
                putString("title", title)
                putString("bookId", bookId)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun removeOverlayView() {
        val rootLayout = findViewById<ViewGroup>(R.id.content).getChildAt(0)
        overlayView?.let {
            (rootLayout as ViewGroup).removeView(it)
            overlayView = null
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // 팝업이 열려있고 오버레이가 추가되었을 때만 터치 이벤트를 소비하여 아무런 동작이 발생X
        if (overlayView != null) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티가 종료될 때 오버레이 뷰를 제거
        removeOverlayView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                myDatabase.deleteBook(bookId)
                finish()
                overridePendingTransition(com.example.myapplication.R.anim.fromright_toleft, com.example.myapplication.R.anim.none)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun openColorPicker(myView: MyView) {
        val originalColor = myView.color

        val colorPicker = AmbilWarnaDialog(this, myView.color, object :
            AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                myView.color = originalColor
            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                myView.color = color
            }
        })
        colorPicker.show()
    }

}
