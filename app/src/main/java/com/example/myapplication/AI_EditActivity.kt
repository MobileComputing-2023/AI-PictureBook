package com.example.myapplication

import MyDatabase
import TextBoxFragment
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityReadBinding

class AI_EditActivity : AppCompatActivity() {

        private lateinit var bookId: String
        private lateinit var myDatabase: MyDatabase
        private var currentPage = 0 //읽기 위해 현재 위치 count
        private lateinit var title: String
        private lateinit var binding: ActivityReadBinding
        private lateinit var gestureDetector: GestureDetector
        private var currentPageId: Int = 0

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

            //제스쳐 감지
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
                showTextBoxEdit(bookId, currentPageId)
                Log.d("DB", "totalPage: $totalPages, currentPage: $currentPage")
            } else if (currentPage == totalPages) { // 마지막 페이지
                val image = myDatabase.getImageForPage(bookId, currentPage)
                binding.imageView.setImageBitmap(image)
                binding.previousBtn.visibility = View.VISIBLE
                binding.nextBtn.visibility = View.VISIBLE
                showTextBoxEdit(bookId, currentPageId)
                Log.d("DB", "totalPage: $totalPages, currentPage: $currentPage")
            } else { //2-마지막장 앞
                val image = myDatabase.getImageForPage(bookId, currentPage)
                binding.imageView.setImageBitmap(image)
                binding.nextBtn.visibility = View.VISIBLE
                binding.previousBtn.visibility = View.VISIBLE
                binding.imageView.alpha = 0f
                showTextBoxEdit(bookId, currentPageId)

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

    private fun showTextBoxEdit(bookId: String, currentPageId:Int) {
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

        private fun showPopupActivity() {
            val intent = Intent(this, AI_EditPopupActivity::class.java).apply {
                putExtra("bookId", bookId)
                putExtra("title", title)
            }
            startActivity(intent)
        }

        private inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100 //스와이프로 간주 최소 거리
            private val SWIPE_VELOCITY_THRESHOLD = 100 //스와이프로 간주 최소 속도

            override fun onFling(
                e1: MotionEvent, //시작점
                e2: MotionEvent, //끝점
                velocityX: Float, //x속도
                velocityY: Float //y속도
            ): Boolean {
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (Math.abs(diffX) > Math.abs(diffY)) { //좌우로 움직이는게 맞는지 확인
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) { //x 기리 속도가 양수
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