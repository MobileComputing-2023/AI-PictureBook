package com.example.myapplication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityDrawBinding
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //2초 내에 두 번 backpress하면 앱 꺼지도록 설정
    private var backPressedTime: Long = 0
    private val backPressedTimeout: Long = 2000

    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime < backPressedTimeout) {
            // 앱 종료
            finishAffinity()
        } else {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.readTerms.setOnClickListener {
            termsPopFragment()

        }

        // isFirstTime이랑 TermsActivity의 termsAgreed랑은 반대임
        // isFirstTime가 termsAgree받은 거니까 false일 때 동의한 거
        val sharedPreferences = getSharedPreferences("TermsAgree", Context.MODE_PRIVATE) // 앱 자체에 데이터 저장
        val isFirstTime = sharedPreferences.getBoolean("termsAgreed", false)

        if (!isFirstTime) {
            // isFirstTime이 true인 경우 동작 실행
            binding.settingBtn.setOnClickListener {
                val intent = Intent(this, TermsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
            }

            binding.readBookListBtn.setOnClickListener {
                val intent = Intent(this, TermsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
            }

        } else {
            binding.settingBtn.setOnClickListener {
                val intent: Intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
            }

            binding.readBookListBtn.setOnClickListener {
                val intent: Intent = Intent(this, ListActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
            }
        }
    }

    private fun termsPopFragment() {
        val fragment = TermsPopFragment().apply {
            arguments = Bundle().apply {
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

}
