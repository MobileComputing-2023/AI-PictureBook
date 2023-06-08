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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
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
    private var textBoxFragment: TextBoxFragment? = null

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
        // DB 삭제- 뒤로 갔다가 다시 버튼 누르면 같은 거 또 DB에 들어옴
        myDatabase.deleteBook(bookId)

        super.onBackPressed()
        overridePendingTransition(com.example.myapplication.R.anim.fromleft_toright, com.example.myapplication.R.anim.none)
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

        binding.nextBtn.setOnClickListener {
            // 그림 정보를 저장한 후, 다음 페이지로 이동
            saveDrawingDataAndMoveToNextPage()
            textBoxFragment = TextBoxFragment()
            textBoxFragment?.show(supportFragmentManager, "TextBoxFragment")
        }

        // TextBoxFragment를 생성하고 표시
        textBoxFragment = TextBoxFragment()
        textBoxFragment?.show(supportFragmentManager, "TextBoxFragment")


    }
    private fun saveDrawingDataAndMoveToNextPage() {
        // 그림을 bitmap으로 저장
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
            showPopupActivity()
        } else {
            currentPageId += 1
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

    private fun showPopupActivity() {
        val intent = Intent(this, PopupActivity::class.java).apply {
            putExtra("bookId", bookId)
            putExtra("title", title)
        }
        startActivity(intent)

        // 팝업이 뜰 때만 오버레이 뷰를 추가
        addOverlayView()
    }
    private fun addOverlayView() {
        val rootLayout = findViewById<ViewGroup>(R.id.content).getChildAt(0)
        if (overlayView == null) {
            overlayView = object : View(this) {
                override fun onTouchEvent(event: MotionEvent): Boolean {
                    // 오버레이 영역의 터치 이벤트를 소비하여 아무런 동작이 발생X
                    return true
                }
            }
            overlayView?.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.TRANSPARENT)
                isClickable = true // 오버레이 위를 클릭할 수 없도록 설정
                isFocusable = true // 오버레이 위를 포커스할 수 없도록 설정
            }
            (rootLayout as ViewGroup).addView(overlayView)
        }
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
                overridePendingTransition(com.example.myapplication.R.anim.fromleft_toright, com.example.myapplication.R.anim.none)
                finish()
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
