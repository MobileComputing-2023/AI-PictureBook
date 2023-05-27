package com.example.myapplication

import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
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
        builder.setView(binding.root)

        binding.saveBtn.setOnClickListener {
            // 저장 버튼 클릭 시 동작을 구현
            // TODO: 저장 버튼 동작 구현

            // 팝업창 닫기
            finish()
        }

        binding.deleteBtn.setOnClickListener {
            // 삭제 버튼 클릭 시 동작 구현
            // TODO: 삭제 버튼 동작 구현

            // 팝업창 닫기
            finish()
        }
        // 팝업창 생성 및 표시
        val alertDialog = builder.create()
        alertDialog.show()
    }
}
