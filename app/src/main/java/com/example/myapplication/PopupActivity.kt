package com.example.myapplication

import MyDatabase
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityPopupBinding

class PopupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPopupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopupBinding.inflate(layoutInflater)
        requestWindowFeature( Window.FEATURE_NO_TITLE );  // 타이틀 상태바 제거
        setContentView(binding.root)

        showPopupDialog();
    }
    private fun showPopupDialog() {
        // 팝업창 생성
        val builder = AlertDialog.Builder(this)

        // LinearLayout을 생성하고 binding의 루트 뷰를 추가합니다.
        val container = LinearLayout(this)
        // Remove the view from its current parent
        val parent = binding.root.parent as? ViewGroup
        parent?.removeView(binding.root)

        // Disable clicks and focus for the root view
        binding.root.isClickable = false
        binding.root.isFocusable = false

        // Add the view to the new parent
        container.addView(binding.root)

        binding.saveBtn.setOnClickListener {
            // 저장 버튼 클릭 시 동작을 구현
            // TODO: 저장 버튼 동작 구현

            // 팝업창 닫기
            finish()

            // Toast 메시지 표시
            Toast.makeText(this, "동화가 생성되었습니다.", Toast.LENGTH_SHORT).show()
            // 동화리스트 화면으로 가기
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.deleteBtn.setOnClickListener {
            // Retrieve the instance of MyDatabase
            val myDatabase = MyDatabase.getInstance(this)
            // Delete the last created database
            myDatabase.deleteLastCreatedRow(MyDatabase.MyDBContract.DrawEntry.TABLE_NAME)
            // 팝업창 닫기
            finish()
            // Toast 메시지 표시
            Toast.makeText(this, "동화가 저장되지않았습니다.\n 메인화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show()

            // 메인 화면으로 돌아가기
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        // 팝업창 생성 및 표시
        val alertDialog = builder.create()
        alertDialog.setView(container) // 이 부분에서 뷰를 설정합니다.
        alertDialog.show()
    }
}
