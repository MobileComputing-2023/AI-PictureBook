package com.example.myapplication

import MyDatabase
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivityReadBinding
import android.view.GestureDetector
import android.view.MotionEvent

class ReadActivity : AppCompatActivity() {
    private lateinit var bookId: String
    private lateinit var myDatabase: MyDatabase
    private var currentPage = 0
    private lateinit var title: String
    private lateinit var binding: ActivityReadBinding
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bookId = intent.getStringExtra("bookId") ?: ""
        myDatabase = MyDatabase.getInstance(this)

        title = myDatabase.getTitle(bookId) ?: ""
        supportActionBar?.title = title

        displayImageForPage(binding)

        binding.previousBtn.setOnClickListener {
            currentPage--
            displayImageForPage(binding)
        }

        binding.nextBtn.setOnClickListener {
            currentPage++
            displayImageForPage(binding)
        }

        gestureDetector = GestureDetector(this, SwipeGestureListener())

        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, ListActivity::class.java))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, ListActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayImageForPage(binding: ActivityReadBinding) {
        val totalPages = myDatabase.getTotalPages(bookId)

        if (currentPage > totalPages) {
            showPopupActivity()
        } else if (currentPage == 0) { // 1페이지(표지)
            val image = myDatabase.getImageForPage(bookId, currentPage)
            binding.imageView.setImageBitmap(image)
            binding.nextBtn.visibility = View.VISIBLE
            binding.previousBtn.visibility = View.GONE
            Log.d("DB", "totalPage: $totalPages, currentPage: $currentPage")
        } else if (currentPage == totalPages) { // 마지막 페이지
            val image = myDatabase.getImageForPage(bookId, currentPage)
            binding.imageView.setImageBitmap(image)
            binding.previousBtn.visibility = View.VISIBLE
            binding.nextBtn.visibility = View.GONE
            Log.d("DB", "totalPage: $totalPages, currentPage: $currentPage")
        } else { //2-마지막장 앞
            val image = myDatabase.getImageForPage(bookId, currentPage)
            binding.imageView.setImageBitmap(image)
            binding.nextBtn.visibility = View.VISIBLE
            binding.previousBtn.visibility = View.VISIBLE
            binding.imageView.alpha = 0f

            val fadeInAnimator = ObjectAnimator.ofFloat(binding.imageView, "alpha", 0f, 1f).apply {
                duration = 500 // 애니메이션의 지속 시간
            }
            fadeInAnimator.start()

            // 애니메이션 없이 즉시 버튼 나타내기
            binding.previousBtn.post { binding.previousBtn.visibility = View.VISIBLE }
            binding.nextBtn.post { binding.nextBtn.visibility = View.VISIBLE }
            Log.d("DB", "totalPage: $totalPages, currentPage: $currentPage")
        }
    }

    private fun showPopupActivity() {
        val intent = Intent(this, ReadAllPopActivity::class.java).apply {
            putExtra("bookId", bookId)
            putExtra("title", title)
        }
        startActivity(intent)
    }

    private inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100 //스와이프로 간주 최소 거리
        private val SWIPE_VELOCITY_THRESHOLD = 100 //스와이프로 간주 최소 속도

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeLeft()
                    } else {
                        onSwipeRight()
                    }
                    return true
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }

    private fun onSwipeRight() {
        currentPage++
        displayImageForPage(binding)
    }

    private fun onSwipeLeft() {
        if (currentPage > 0) {
            currentPage--
            displayImageForPage(binding)
        }
    }
}

