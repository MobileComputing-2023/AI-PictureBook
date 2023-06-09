package com.example.myapplication

import MyDatabase
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityPopupBinding

class AI_EditPopupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPopupBinding
    private lateinit var bookId: String
    private lateinit var title: String
    private lateinit var myDatabase: MyDatabase
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopupBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 상태바 제거
        setContentView(binding.root)
        bookId = intent.getStringExtra("bookId") ?: ""
        title = intent.getStringExtra("title") ?: ""

        myDatabase = MyDatabase.getInstance(this)

        if (!isFinishing) {
            showPopupDialog(bookId, title)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 다이얼로그를 닫기 위해 onDestroy에서 호출
        alertDialog?.dismiss()
    }

    private fun showPopupDialog(bookId: String, title: String) {
        Log.d("PopupActivity", "BookId: $bookId")
        Log.d("PopupActivity", "Title: $title")

        // 팝업창 생성
        val builder = AlertDialog.Builder(this)

        //버튼 외 화면, backpress 눌러도 화면 꺼지지 않음
        builder.setCancelable(false)

        // LinearLayout을 생성하고 binding의 루트 뷰를 추가
        val container = LinearLayout(this)

        // view를 현재 parent에서 제거
        val parent = binding.root.parent as? ViewGroup
        parent?.removeView(binding.root)

        // view의 클릭, 포커스 비활성화
        binding.root.isClickable = false
        binding.root.isFocusable = false
        val image=myDatabase.getImageForPage(bookId, 0)
        binding.cover.setImageBitmap(image)
        binding.titleText.text = "『$title』의\n 마지막 페이지입니다."

        // view를 새로운 parent에 추가
        container.addView(binding.root)

        binding.saveBtn.setOnClickListener {
            // 저장 버튼 클릭 시 동작을 구현
            // TODO: 저장 버튼 동작 구현

            // 팝업창 닫기
            finish()

            // Toast 메시지 표시
            Toast.makeText(this, "텍스트 박스 설정이 완료되었습니다.", Toast.LENGTH_SHORT).show()

            // 동화읽는 화면으로 가기
            val intent = Intent(this, ReadActivity::class.java)
            intent.putExtra("bookId", bookId) // Pass the bookId as an extra
            startActivity(intent)
            overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
        }

        binding.deleteBtn.setOnClickListener {
            // DB 삭제
            myDatabase.deleteBook(bookId)

            // 팝업창 닫기
            finish()
            // Toast 메시지 표시
            Toast.makeText(this, "텍스트 박스 설정이 저장되지 않았습니다.\n메인 화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show()

            // 메인 화면으로 돌아가기
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
        }

        // 팝업창 생성 및 표시
        if (!isFinishing) {
            alertDialog = builder.create()
            alertDialog?.setView(container)
            alertDialog?.show()
        }
    }
}
