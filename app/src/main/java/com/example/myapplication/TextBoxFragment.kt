import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.FragmentTextboxBinding
import yuku.ambilwarna.AmbilWarnaDialog

class TextBoxFragment : DialogFragment() {
    private lateinit var binding: FragmentTextboxBinding
    private var textBoxColor = Color.BLACK
    private var textBoxText: String = ""
    private var isEditing = false

    private var isLinearLayoutDragging = false
    private var initialLinearLayoutY = 0f
    private var initialTouchLinearLayoutY = 0f

    private lateinit var bookId: String
    private lateinit var myDatabase: MyDatabase
    private var currentPage = 0 //읽기 위해 현재 위치 count


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = FragmentTextboxBinding.inflate(requireActivity().layoutInflater)
        val view = binding.root

        builder.setView(view)

        myDatabase = MyDatabase.getInstance(requireContext()) // myDatabase 초기화

        return builder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTextboxBinding.inflate(inflater, container, false)

        val whiteBackground = ColorDrawable(Color.WHITE)
        binding.textBox.background = whiteBackground

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myDatabase = MyDatabase.getInstance(requireContext())

        // bookId와 currentPage 초기화
        bookId = arguments?.getString("bookId") ?: ""
        currentPage = arguments?.getInt("currentPageID") ?: 0

        val text = myDatabase.getTextForPage(bookId, currentPage)
        binding.textBox.text = text

        binding.linearLayout.setOnTouchListener { view, event ->
            handleLinearLayoutTouch(view, event)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                adjustTextBoxTransparency(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // 사용되지 않음
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // 사용되지 않음
            }
        })

        binding.colorBtn1.setOnClickListener {

            val color = Color.parseColor("#E55B62")
            binding.textBox.setBackgroundColor(color)
            textBoxText = binding.textBox.text.toString()
            Log.d("TextBoxFragment", "Background color changed to: #E55B62")
            Log.d("TextBoxFragment", "TextBox text: $textBoxText")
        }

        binding.colorBtn2.setOnClickListener {
            val color = Color.parseColor("#5D65D0")
            binding.textBox.setBackgroundColor(color)
            textBoxText = binding.textBox.text.toString()
            Log.d("TextBoxFragment", "Background color changed to: #5D65D0")
            Log.d("TextBoxFragment", "TextBox text: $textBoxText")
        }

        binding.colorBtn3.setOnClickListener {
            val color = Color.parseColor("#E5D724")
            binding.textBox.setBackgroundColor(color)
            textBoxText = binding.textBox.text.toString()
            Log.d("TextBoxFragment", "Background color changed to: #E5D724")
            Log.d("TextBoxFragment", "TextBox text: $textBoxText")
        }

        binding.colorBtn4.setOnClickListener {
            val color = Color.parseColor("#000000")
            binding.textBox.setBackgroundColor(color)
            textBoxText = binding.textBox.text.toString()
            Log.d("TextBoxFragment", "Background color changed to: #000000")
            Log.d("TextBoxFragment", "TextBox text: $textBoxText")
        }

        binding.colorBtn5.setOnClickListener {
            openColorPicker()
        }

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

        adjustTextBoxTransparency(0)
    }

    private fun handleLinearLayoutTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isLinearLayoutDragging = true
                initialLinearLayoutY = view.y
                initialTouchLinearLayoutY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                if (isLinearLayoutDragging) {
                    val dy = event.rawY - initialTouchLinearLayoutY
                    view.y = initialLinearLayoutY + dy
                    Log.d("TextBoxFragment", "LinearLayout position: x=${view.x}, y=${view.y}")
                }
            }
            MotionEvent.ACTION_UP -> {
                isLinearLayoutDragging = false
            }
        }
        return true
    }

    private fun adjustTextBoxTransparency(progress: Int) {
        val alpha = progress.toFloat() / 100
        val background = binding.textBox.background
        if (background is ColorDrawable) {
            background.alpha = (255 * alpha).toInt()
        } else {
            val newBackground = ColorDrawable(Color.TRANSPARENT)
            newBackground.alpha = (255 * alpha).toInt()
            binding.textBox.background = newBackground
        }
        Log.d("TextBoxFragment", "TextBox transparency: $progress")
    }

    private fun openColorPicker() {
        val colorPicker = AmbilWarnaDialog(requireContext(), textBoxColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                // 사용되지 않음
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
