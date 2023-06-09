package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.myapplication.databinding.ActivityTermsBinding
import com.google.android.material.tabs.TabLayout

class TermsActivity : AppCompatActivity() {
    // 앱 상태(동의여부) 저장 : 앱 내부 초기 값 설정
    private lateinit var sharedPreferences: SharedPreferences

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fromleft_toright, R.anim.none)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fromleft_toright, R.anim.none)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "약관 동의"

        //termsAgree false로 지정
        sharedPreferences = getSharedPreferences("TermsAgree", Context.MODE_PRIVATE)
        val termsAgreed = sharedPreferences.getBoolean("termsAgreed", false)

        val tab = binding.tab
        tab.addTab(tab.newTab().setText("Image Tailer"))
        tab.addTab(tab.newTab().setText("이용약관"))

        tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val transaction = supportFragmentManager.beginTransaction()
                when (tab?.position) {
                    0 -> transaction.replace(binding.tabContent.id, TermFragment())
                    1 -> transaction.replace(binding.tabContent.id, TermFragmentTwo())
                }
                transaction.commit()
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        // 초기 탭 설정
        supportFragmentManager.beginTransaction()
            .replace(binding.tabContent.id, TermFragment())
            .commit()

        tab.selectTab(tab.getTabAt(0)) // 첫 번째 탭 선택

        // SelectAll 선택 시 전체 체크
        binding.selectAll.setOnCheckedChangeListener { _, isChecked ->
            binding.readUS.isChecked = isChecked
            binding.readTerms.isChecked = isChecked
        }

        // readUS 또는 readTerms 체크 해제 시 selectAll도 체크 해제
        binding.readUS.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                binding.selectAll.isChecked = false
            }
            else if (isChecked && binding.readTerms.isChecked){
                binding.selectAll.isChecked = true
            }
        }

        binding.readTerms.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                binding.selectAll.isChecked = false
            }
            else if (isChecked && binding.readUS.isChecked){
                binding.selectAll.isChecked = true
            }
        }

        val termsAgreeButton = binding.termsAgree
        termsAgreeButton.setOnClickListener {
            val isAgree = binding.selectAll.isChecked
            //전체 동의 클릭되어 있으면 sharedPreference true로 변경
            if (isAgree) {
                sharedPreferences.edit {
                    putBoolean("termsAgreed", true)
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fromleft_toright, R.anim.none)
                finish()
            } else {
                Toast.makeText(this, "모든 약관에 동의해야 합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
