package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.myapplication.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding= ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setTitle("그림책 만들기")
        var minMan=findViewById<Button>(R.id.minusMan)
        var plusMan = findViewById<Button>(R.id.plusMan)
        var minWoman=findViewById<Button>(R.id.minusWoman)
        var plusWoman = findViewById<Button>(R.id.plusWoman)
        val output_man=findViewById<TextView>(R.id.NumMan)
        val output_woman=findViewById<TextView>(R.id.NumWoman)
        var NumMan=0;
        var NumWoman=0;

        output_man.text = NumMan.toString()
        output_woman.text = NumWoman.toString()

        minMan.setOnClickListener {
            if (NumMan==0){
                output_man.setText(NumMan.toString())
            }
            else {
            NumMan-- //0일때 처리 해줘야함
            output_man.setText(NumMan.toString())}
        }
        plusMan.setOnClickListener {
            NumMan++
            output_man.setText(NumMan.toString())
        }
        minWoman.setOnClickListener {
            NumWoman-- //0일때 처리 해줘야함
            output_woman.setText(NumWoman.toString())
        }
        plusWoman.setOnClickListener {
            NumWoman++
            output_woman.setText(NumWoman.toString())
        }

        binding.btnwrite.setOnClickListener {
            val intent: Intent = Intent(this,SubActivity::class.java).apply {
                putExtra("next", "level")
            }
            val selectedGenre = binding.genre.selectedItem.toString()
            val selectedEra = binding.era.selectedItem.toString()
            intent.putExtra("selectedGenre", selectedGenre)
            intent.putExtra("selectedEra", selectedEra)
            intent.putExtra("NumMan", NumMan)
            intent.putExtra("NumWoman", NumWoman)
            intent.putExtra("num", 30)
            intent.putExtra("summary", binding.writesum.text.toString())
            startActivity(intent)
        }

    }
}