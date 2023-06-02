package com.example.myapplication

import MyDatabase
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityReadallpopBinding

class ReadAllPopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadallpopBinding
    private lateinit var title: String
    private lateinit var myDatabase: MyDatabase
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadallpopBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)


        title = intent.getStringExtra("title") ?: ""
        myDatabase = MyDatabase.getInstance(this)
        if (!isFinishing) {
            showPopupDialog(title)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 다이얼로그를 닫기 위해 onDestroy에서 호출합니다.
        alertDialog?.dismiss()
    }

    private fun showPopupDialog(title: String) {
        Log.d("ReadAllPopActivity", "Title: $title")

        // 팝업창 생성
        val builder = AlertDialog.Builder(this)

        //버튼 외 화면, backpress 눌러도 화면 꺼지지않음
        builder.setCancelable(false)

        // LinearLayout을 생성하고 binding의 루트 뷰를 추가합니다.
        val container = LinearLayout(this)

        // Remove the view from its current parent
        val parent = binding.root.parent as? ViewGroup
        parent?.removeView(binding.root)

        // Disable clicks and focus for the root view
        binding.root.isClickable = false
        binding.root.isFocusable = false

        binding.titleText.text = "『$title』을\n마지막 페이지까지 다 읽었습니다!"

        // Add the view to the new parent
        container.addView(binding.root)

        binding.toListBtn.setOnClickListener {
            // 팝업창 닫기
            finish()

            // 동화리스트 화면으로 가기
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }

        binding.toMainBtn.setOnClickListener {
            // 팝업창 닫기
            finish()

            // 메인 화면으로 돌아가기
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 팝업창 생성 및 표시
        if (!isFinishing) {
            alertDialog = builder.create()
            alertDialog?.setView(container)
            alertDialog?.show()
        }
    }
}
