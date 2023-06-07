package com.example.myapplication

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.ByteArrayOutputStream

class ImageDetailFragment : DialogFragment() {

    private var nextPromptIndex: Int = 0

    private lateinit var imageView: ImageView
    private lateinit var bookId: String
    private lateinit var title: String
    private var lastPageId: Int = 0

    private lateinit var summary: String
    private lateinit var originalsummary: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_image_detail, null)

        imageView = view.findViewById(R.id.generatedImageView)

        builder.setView(view)
        return builder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_detail, container, false)
        imageView = view.findViewById(R.id.generatedImageView)

        // Arguments로 전달된 데이터 가져오기
        val imageUrl = arguments?.getString("imageUrl")

        // 이미지 로드
        imageUrl?.let {
            Glide.with(requireContext())
                .load(it)
                .apply(RequestOptions.overrideOf(500, 500))
                .into(imageView)
        }

        val intent = requireActivity().intent
        bookId = intent.getStringExtra("bookId") ?: ""
        title = intent.getStringExtra("title") ?: ""
        lastPageId = intent.getIntExtra("lastPageId", 0)
        originalsummary = intent.getStringExtra("originalsummary")?:""
        summary = intent.getStringExtra("summary")?:""

        nextPromptIndex = arguments?.getInt("nextPromptIndex") ?: 0

        val downloadButton = view.findViewById<AppCompatButton>(R.id.download)
        downloadButton.setOnClickListener {

            Log.d("title_1", title)
            Log.d("bookId_1", bookId)

            Log.d("ImageDetailFragment", "Download button clicked")

            // 이미지를 Bitmap으로 변환
            val bitmap = imageView.drawable.toBitmap()

            // Bitmap을 ByteArray로 압축
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            Log.d("현재 상황", nextPromptIndex.toString())
            Log.d("현재 상황", lastPageId.toString())

            // DB에 이미지 저장
            saveImageToDB(bookId, nextPromptIndex, byteArray)

            if (nextPromptIndex == lastPageId) {
                // 마지막 페이지일 경우 팝업 액티비티 호출
                Log.d("마지막 페이지", nextPromptIndex.toString())
                Log.d("마지막 페이지", lastPageId.toString())
                showPopupActivity()
            } else {
                // 다음 페이지로 이동하여 CreateActivity 호출
                nextPromptIndex += 1
                val intent = Intent(requireContext(), CreateActivity::class.java).apply {
                    putExtra("bookId", bookId)
                    putExtra("title", title)
                    putExtra("lastPageId", lastPageId)
                    putExtra("originalsummary", originalsummary)
                    putExtra("summary", summary)
                    putExtra("nextPromptIndex", nextPromptIndex)
                }
                Log.d("마지막 페이지 아님", nextPromptIndex.toString())
                Log.d("마지막 페이지 아님", lastPageId.toString())
                startActivity(intent)
            }
        }

        val closeButton = view.findViewById<AppCompatButton>(R.id.close)
        closeButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun showPopupActivity() {

        val intent = Intent(requireContext(), AI_PopupActivity::class.java).apply {
            putExtra("bookId", bookId)
            putExtra("title", title)
        }

        Log.d("title_2", title)
        Log.d("bookId_2", bookId)

        startActivity(intent)
    }


    private fun saveImageToDB(bookId: String, pageId: Int, byteArray: ByteArray) {
        val dbHelper = MyDatabase.MyDbHelper(requireContext())
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(MyDatabase.MyDBContract.DrawEntry.COLUMN_IMAGE, byteArray)
        }

        val selection = "${MyDatabase.MyDBContract.DrawEntry.COLUMN_BOOK_ID} = ? AND " +
                "${MyDatabase.MyDBContract.DrawEntry.COLUMN_PAGE_ID} = ?"
        val selectionArgs = arrayOf(bookId, pageId.toString())

        val rowsAffected = db.update(
            MyDatabase.MyDBContract.DrawEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs
        )

        if (rowsAffected > 0) {
            Log.d(
                "DB",
                "Image data updated successfully. Book ID: $bookId, Page ID: $pageId Bitmap: $byteArray"
            )
        } else {
            Log.d("DB", "Failed to update image data.")
            val intent: Intent = Intent(requireContext(), ErrorActivity::class.java)
            startActivity(intent)
        }
    }
}
