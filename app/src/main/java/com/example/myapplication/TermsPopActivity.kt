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
import com.example.myapplication.databinding.ActivityTermspopBinding

class TermsPopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTermspopBinding
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermspopBinding.inflate(layoutInflater)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        if (!isFinishing) {
            showPopupDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 다이얼로그를 닫기 위해 onDestroy에서 호출합니다.
        alertDialog?.dismiss()
    }

    private fun showPopupDialog() {

        // 팝업창 생성
        val builder = AlertDialog.Builder(this)

        //버튼 외 화면, backpress 눌러도 화면 꺼지지않음
        builder.setCancelable(false)

        // LinearLayout을 생성하고 binding의 루트 뷰를 추가합니다.
        val container = LinearLayout(this)

        // Remove the view from its current parent
        val parent = binding.root.parent as? ViewGroup
        parent?.removeView(binding.root)

        // 클릭, 포커스 막음
        binding.root.isClickable = false
        binding.root.isFocusable = false

        container.addView(binding.root)

        binding.closeBtn.setOnClickListener {
            // 팝업창 닫기
            finish()
        }
        // 팝업창 생성 및 표시
        if (!isFinishing) {
            alertDialog = builder.create()
            alertDialog?.setView(container)
            alertDialog?.show()
        }
    }
}
