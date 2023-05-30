package com.example.myapplication

import android.R
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityDrawBinding
import yuku.ambilwarna.AmbilWarnaDialog

class DrawActivity : AppCompatActivity() {
    private var currentPageId: Int = 0
    private val lastPageId = 10
    private val drawingData = HashMap<Int, ArrayList<Point>>()
    private lateinit var myView: MyView
    private var overlayView: View? = null
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
                    drawingData[currentPageId] = ArrayList(points)
                }
            }
            invalidate()
            return true
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDrawBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        myView = MyView(this)
        binding.drawLinear.addView(myView)



        binding.clearBtn.setOnClickListener {
            myView.points.clear()
            myView.invalidate()
        }

        binding.colorPickerButton.setOnClickListener{
            openColorPicker(myView)
        }

        binding.nextBtn.setOnClickListener {
            // 그림 정보를 저장한 후, 다음 페이지로 이동
            saveDrawingDataAndMoveToNextPage()
        }

    }
    private fun saveDrawingDataAndMoveToNextPage() {
        // drawingData에 현재 페이지의 그림 정보 저장
        drawingData[currentPageId] = ArrayList(myView.points)
        myView.points.clear()
        myView.invalidate()
        if (currentPageId == lastPageId) {
            // 모든 페이지를 그림 데이터로 채웠을 때
            showPopupActivity()
        } else {
            currentPageId += 1
        }

        // DB에 저장되는 정보 로그로 출력
        val drawingDataString = drawingData.entries.joinToString("\n") { entry ->
            val pageId = entry.key
            val points = entry.value.joinToString(", ") { point ->
                "(${point.x}, ${point.y})"
            }
            "Page $pageId: $points"
        }
        Log.d("DrawActivity", "Drawing data:\n$drawingDataString")
    }
    private fun showPopupActivity() {
        val intent = Intent(this, PopupActivity::class.java)
        startActivity(intent)

        // 팝업이 뜰 때만 오버레이 뷰를 추가합니다.
        addOverlayView()
    }
    private fun addOverlayView() {
        val rootLayout = findViewById<ViewGroup>(R.id.content).getChildAt(0)
        if (overlayView == null) {
            overlayView = object : View(this) {
                override fun onTouchEvent(event: MotionEvent): Boolean {
                    // 오버레이 영역의 터치 이벤트를 소비하여 아무런 동작이 발생하지 않도록 합니다.
                    return true
                }
            }
            overlayView?.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.TRANSPARENT)
                isClickable = true // 오버레이 위를 클릭할 수 없도록 설정합니다.
                isFocusable = true // 오버레이 위를 포커스할 수 없도록 설정합니다.
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
        // 팝업이 열려있고 오버레이가 추가되었을 때만 터치 이벤트를 소비하여 아무런 동작이 발생하지 않도록 합니다.
        if (overlayView != null) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티가 종료될 때 오버레이 뷰를 제거합니다.
        removeOverlayView()
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
