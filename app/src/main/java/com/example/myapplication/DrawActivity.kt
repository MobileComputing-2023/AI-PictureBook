package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityDrawBinding

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

        val myView = MyView(this)
        binding.drawLinear.addView(myView)

        binding.drawRedBtn.setOnClickListener {
            myView.color = Color.RED
        }

        binding.drawBlueBtn.setOnClickListener {
            myView.color = Color.BLUE
        }

        binding.drawBlackBtn.setOnClickListener {
            myView.color = Color.BLACK
        }

        binding.clearBtn.setOnClickListener {
            myView.points.clear()
            myView.invalidate()
        }
    }
}
