package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityDrawBinding
import yuku.ambilwarna.AmbilWarnaDialog

class DrawActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDrawBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        val myView = MyView(this)
        binding.drawLinear.addView(myView)

        binding.clearBtn.setOnClickListener {
            myView.points.clear()
            myView.invalidate()
        }

        binding.colorPickerButton.setOnClickListener{
            openColorPicker(myView)
        }
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
        val colorPicker = AmbilWarnaDialog(this, myView.color, object :
            AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                // 취소
            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                myView.color = color
            }
        })
        colorPicker.show()
    }

}
