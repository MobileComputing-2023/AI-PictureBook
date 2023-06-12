import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import java.io.ByteArrayOutputStream
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

    private var isPinActivated = true // pin 클릭 여부 저장

    private var isLinearLayoutDragging = false
    private var initialLinearLayoutY = 0f
    private var initialTouchLinearLayoutY = 0f

    private lateinit var bookId: String
    private lateinit var myDatabase: MyDatabase
    private lateinit var db: SQLiteDatabase
    private var currentPage = 0 // 읽기 위해 현재 위치 count

    private var yCoordinate = 0f

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = FragmentTextboxBinding.inflate(requireActivity().layoutInflater)
        val view = binding.root

        builder.setView(view)

        myDatabase = MyDatabase.getInstance(requireContext())
        db = myDatabase.db

        return builder.create()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTextboxBinding.inflate(inflater, container, false)

        val whiteBackground = ColorDrawable(Color.WHITE)
        binding.textBox.background = whiteBackground

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myDatabase = MyDatabase.getInstance(requireContext())

        // bookId와 currentPage 초기화
        bookId = arguments?.getString("bookId") ?: ""
        currentPage = arguments?.getInt("currentPageID") ?: 0

        val text = myDatabase.getTextForPage(bookId, currentPage)
        binding.textBox.text = text

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

        binding.colorBtn1.setOnClickListener { // textbox 전체 배경 색상

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

        binding.colorBtn5.setOnClickListener { // text 색상 변경
            openColorPicker()
        }

        binding.pinBtn.visibility = View.GONE
        binding.unpinBtn.visibility = View.VISIBLE // pin 모양

        binding.pinBtn.setOnClickListener {
            isPinActivated = !isPinActivated
            if (isPinActivated) {
                // 고정 상태
                binding.pinBtn.visibility = View.GONE
                binding.unpinBtn.visibility = View.VISIBLE
            } else {
                // 고정되지 않은 상태
                binding.unpinBtn.visibility = View.GONE
                binding.pinBtn.visibility = View.VISIBLE
            }
        }

        binding.unpinBtn.setOnClickListener {
            isPinActivated = !isPinActivated
            if (!isPinActivated) {
                // 고정되지 않은 상태
                binding.unpinBtn.visibility = View.GONE
                binding.pinBtn.visibility = View.VISIBLE
            }
        }

        binding.saveBtn.setOnClickListener {
            val (bitmap, yCoordinate) = captureFragmentContent()
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray() // byteArray로 변환

            saveImageToDB(bookId, currentPage, byteArray, yCoordinate)

            isPinActivated = false
            binding.unpinBtn.performClick()

            binding.unpinBtn.visibility = View.GONE
            binding.pinBtn.visibility = View.GONE
            binding.saveBtn.visibility = View.GONE
            binding.editBtn.visibility = View.GONE

            isEditing = false // 편집 모드 비활성화
            isLinearLayoutDragging = false // 드래그 비활성화
        }

        binding.linearLayout.setOnTouchListener { view, event ->
            if (isPinActivated && !isEditing) {
                return@setOnTouchListener false // 고정된 상태에서는 터치 이벤트를 처리하지 않음
            } else {
                return@setOnTouchListener handleLinearLayoutTouch(view, event)
            }
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

    private fun saveImageToDB(bookId: String, pageId: Int, byteArray: ByteArray, yCoordinate: Float) {
        // Save the bitmap and y-coordinate to the database
        val values = ContentValues().apply {
            put(MyDatabase.MyDBContract.DrawEntry.COLUMN_TEXT_IMAGE, byteArray)
            put(MyDatabase.MyDBContract.DrawEntry.COLUMN_TEXT_POSITION, yCoordinate)
        }

        val selection = "${MyDatabase.MyDBContract.DrawEntry.COLUMN_BOOK_ID} = ? AND " +
                "${MyDatabase.MyDBContract.DrawEntry.COLUMN_PAGE_ID} = ?"
        val selectionArgs = arrayOf(bookId, pageId.toString())

        val rowsAffected = myDatabase.db.update(
            MyDatabase.MyDBContract.DrawEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs
        )

        if (rowsAffected > 0) {

            binding.seekBar.visibility = View.GONE
            binding.colorBtn1.visibility = View.GONE
            binding.colorBtn2.visibility = View.GONE
            binding.colorBtn3.visibility = View.GONE
            binding.colorBtn4.visibility = View.GONE
            binding.colorBtn5.visibility = View.GONE
            binding.unpinBtn.visibility = View.GONE
            binding.pinBtn.visibility = View.GONE
            binding.saveBtn.visibility = View.GONE

            Log.d(
                "DB",
                "Image data updated successfully. Book ID: $bookId, Page ID: $pageId, Y Coordinate: $yCoordinate"
            )
        } else {
            Log.d("DB", "Failed to update image data.")
        }
    }

    // TextBox의 크기만큼 비트맵을 생성하고, 그 위에 TextBox의 배경과 텍스트를 그려 저장
    private fun captureFragmentContent(): Pair<Bitmap, Float> {
        val textBoxWidth = binding.textBox.width
        val textBoxHeight = binding.textBox.height

        val bitmap = Bitmap.createBitmap(textBoxWidth, textBoxHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val background = binding.textBox.background
        if (background is ColorDrawable) {
            val backgroundColor = background.color
            canvas.drawColor(backgroundColor)
        }

        val textPaint = binding.textBox.paint

        val textLayout = StaticLayout.Builder
            .obtain(binding.textBox.text, 0, binding.textBox.length(), textPaint, textBoxWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()

        val textHeight = textLayout.height.toFloat()
        val lineHeight = textLayout.getLineBottom(0) - textLayout.getLineTop(0)
        val textY = (textBoxHeight - textHeight) / 2 + (lineHeight / 3) // textbox에 뜨는 text 위치 조정

        canvas.save()
        canvas.translate(0f, textY)
        textLayout.draw(canvas)
        canvas.restore()

        return Pair(bitmap, yCoordinate)
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

                    val centerY = view.height / 2 // TextView의 세로 중앙 좌표
                    val screenHeight = binding.root.height // 화면의 세로 크기
                    val adjustedY = view.y + centerY - screenHeight / 2

                    yCoordinate = adjustedY.toFloat()
                    Log.d("TextBoxFragment", "TextView position: y=$yCoordinate")
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
