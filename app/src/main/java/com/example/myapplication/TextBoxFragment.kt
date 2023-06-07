import android.app.Dialog
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTextboxBinding
import yuku.ambilwarna.AmbilWarnaDialog

class TextBoxFragment : DialogFragment() {
    private lateinit var binding: FragmentTextboxBinding
    private var isDragging = false
    private var initialX = 0f
    private var initialY = 0f
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var textBoxColor = Color.BLACK
    private var textBoxText: String = ""
    private var isEditing = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = FragmentTextboxBinding.inflate(requireActivity().layoutInflater)
        val view = binding.root

        builder.setView(view)
        return builder.create()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTextboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            textBoxText = binding.textBox.text.toString()
            Log.d("TextBoxFragment", "Text color changed to: #E55B62")
            Log.d("TextBoxFragment", "TextBox text: $textBoxText")
        }

        binding.colorBtn2.setOnClickListener {
            binding.textBox.setTextColor(Color.parseColor("#5D65D0"))
            textBoxText = binding.textBox.text.toString()
            Log.d("TextBoxFragment", "Text color changed to: #5D65D0")
            Log.d("TextBoxFragment", "TextBox text: $textBoxText")
        }

        binding.colorBtn3.setOnClickListener {
            binding.textBox.setTextColor(Color.parseColor("#E5D724"))
            textBoxText = binding.textBox.text.toString()
            Log.d("TextBoxFragment", "Text color changed to: #E5D724")
            Log.d("TextBoxFragment", "TextBox text: $textBoxText")
        }

        binding.colorBtn4.setOnClickListener {
            binding.textBox.setTextColor(Color.parseColor("#000000"))
            textBoxText = binding.textBox.text.toString()
            Log.d("TextBoxFragment", "Text color changed to: #000000")
            Log.d("TextBoxFragment", "TextBox text: $textBoxText")
        }

        binding.colorBtn5.setOnClickListener {
            openColorPicker()
        }

        // 편집 버튼을 클릭하면 투명도와 색상 조정 버튼 보여짐
        binding.editBtn.setOnClickListener {
            isEditing = !isEditing
            if (isEditing) {
                binding.seekBar.visibility = View.VISIBLE
                binding.colorBtn1.visibility = View.VISIBLE
                binding.colorBtn2.visibility = View.VISIBLE
                binding.colorBtn3.visibility = View.VISIBLE
                binding.colorBtn4.visibility = View.VISIBLE
                binding.colorBtn5.visibility = View.VISIBLE
            } else {
                binding.seekBar.visibility = View.GONE
                binding.colorBtn1.visibility = View.GONE
                binding.colorBtn2.visibility = View.GONE
                binding.colorBtn3.visibility = View.GONE
                binding.colorBtn4.visibility = View.GONE
                binding.colorBtn5.visibility = View.GONE
            }
        }
        binding.textBox.setOnTouchListener { view, event ->
            handleTextBoxTouch(view, event)
        }
    }

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
                    Log.d("TextBoxFragment", "TextBox position: x=${view.x}, y=${view.y}")
                }
            }
            MotionEvent.ACTION_UP -> {
                isDragging = false
            }
        }
        return true
    }

    private fun adjustTextBoxTransparency(progress: Int) {
        val alpha = progress.toFloat() / 100
        val background = binding.textBox.background
        background.alpha = (255 * alpha).toInt()
        binding.textBox.background = background
        Log.d("TextBoxFragment", "TextBox transparency: $progress")
    }

    private fun openColorPicker() {
        val colorPicker = AmbilWarnaDialog(requireContext(), textBoxColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                // Not used
            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                textBoxColor = color
                binding.textBox.setTextColor(textBoxColor)
                textBoxText = binding.textBox.text.toString()
                Log.d("TextBoxFragment", "Text color changed to: $textBoxColor")
                Log.d("TextBoxFragment", "TextBox text: $textBoxText")
            }
        })
        colorPicker.show()
    }

}
