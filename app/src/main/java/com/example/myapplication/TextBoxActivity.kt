import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityTextboxBinding
import yuku.ambilwarna.AmbilWarnaDialog

class TextBoxActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextboxBinding

    private var isDragging = false
    private var initialX = 0f
    private var initialY = 0f
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var textBoxColor = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textBox.setOnTouchListener { view, event ->
            handleTextBoxTouch(view, event)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                adjustTextBoxTransparency(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not used
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not used
            }
        })
        binding.colorBtn1.setOnClickListener {
            binding.textBox.setTextColor(Color.parseColor("#E55B62"))
        }
        binding.colorBtn2.setOnClickListener {
            binding.textBox.setTextColor(Color.parseColor("#5D65D0"))
        }
        binding.colorBtn3.setOnClickListener {
            binding.textBox.setTextColor(Color.parseColor("#E5D724"))
        }
        binding.colorBtn4.setOnClickListener {
            binding.textBox.setTextColor(Color.parseColor("#000000"))
        }
        binding.colorBtn5.setOnClickListener {
            openColorPicker()
        }
    }

    /*textBox 위치 이동 기능 구현*/
    private fun handleTextBoxTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val rect = Rect()
                view.getGlobalVisibleRect(rect)
                isDragging = rect.contains(event.rawX.toInt(), event.rawY.toInt())
                if (isDragging) {
                    initialX = view.x
                    initialY = view.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    view.x = initialX + dx
                    view.y = initialY + dy
                }
            }
            MotionEvent.ACTION_UP -> {
                isDragging = false
            }
        }
        return true
    }

    /*textBox 투명도 조절 기능 구현*/
    private fun adjustTextBoxTransparency(progress: Int) {
        val alpha = progress.toFloat() / 100
        val background = binding.textBox.background
        background.alpha = (255 * alpha).toInt()
        binding.textBox.background = background
    }


    /*색상 선택 기능 구현*/
    private fun openColorPicker() {
        val colorPicker = AmbilWarnaDialog(this, textBoxColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                // Not used
            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                textBoxColor = color
                binding.textBox.setTextColor(textBoxColor)
            }
        })
        colorPicker.show()
    }
}
