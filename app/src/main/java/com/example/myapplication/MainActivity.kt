package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val reqLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
            if(result.resultCode == RESULT_OK){
                Log.d("TAG", "return")
                val intent = result.data
                val clickBtn : String? = intent?.getStringExtra("res")
                "btn $clickBtn".also{
                    binding.txtMainView1.text = it
                }
            }
        }

        binding.btnSub.setOnclickListener{
            val intent:Intent = Intent(this, SubActivity::class.java).apply{
                putExtra("next", "level")
        }
            intent.putExtra("num", 30)
            intent.putExtra("edit", binding.editTxt.text.toString())
            reqLauncher.launch(intent)
        }
    }
}