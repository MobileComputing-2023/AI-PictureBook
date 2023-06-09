package com.example.myapplication

import MyDatabase
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.ActivityPopupBinding


class PopupFragment : DialogFragment() {
    private lateinit var binding: ActivityPopupBinding
    private lateinit var bookId: String
    private lateinit var title: String
    private lateinit var myDatabase: MyDatabase
    private var alertDialog: AlertDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = ActivityPopupBinding.inflate(requireActivity().layoutInflater)
        val view = binding.root

        builder.setView(view)

        myDatabase = MyDatabase.getInstance(requireContext()) // myDatabase 초기화

        return builder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityPopupBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ActivityPopupBinding.inflate(layoutInflater)

        bookId = arguments?.getString("bookId") ?: ""
        title = arguments?.getString("title") ?: ""

        myDatabase = MyDatabase.getInstance(requireContext())

        if (!requireActivity().isFinishing) {
            showPopupDialog(bookId, title)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 다이얼로그를 닫기 위해 onDestroy에서 호출
        alertDialog?.dismiss()
    }

    private fun showPopupDialog(bookId: String, title: String) {
        Log.d("PopupFragment", "BookId: $bookId")
        Log.d("PopupFragment", "Title: $title")

        // 팝업창 생성
        val builder = AlertDialog.Builder(requireActivity())

        //버튼 외 화면, backpress 눌러도 화면 꺼지지않음
        builder.setCancelable(false)

        // LinearLayout을 생성하고 binding의 루트 뷰를 추가
        val container = LinearLayout(requireContext())

        val parent = binding.root.parent as? ViewGroup
        parent?.removeView(binding.root)

        // Disable clicks and focus for the root view
        binding.root.isClickable = false
        binding.root.isFocusable = false
        val image=myDatabase.getImageForPage(bookId, 0)
        binding.cover.setImageBitmap(image)
        binding.titleText.text = "『$title』의\n 마지막 페이지입니다."

        container.addView(binding.root)

        binding.saveBtn.setOnClickListener {
            // 저장 버튼 클릭 시 동작을 구현

            // 팝업창 닫기
            dismiss()

            // Toast 메시지 표시
            Toast.makeText(requireContext(), "동화가 생성되었습니다.", Toast.LENGTH_SHORT).show()

            // 동화읽는 화면으로 가기
            val intent = Intent(requireContext(), ReadActivity::class.java)
            intent.putExtra("bookId", bookId) // Pass the bookId as an extra
            requireActivity().startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
        }

        binding.deleteBtn.setOnClickListener {
            // DB 삭제
            myDatabase.deleteBook(bookId)

            // 팝업창 닫기
            dismiss()
            // Toast 메시지 표시
            Toast.makeText(requireContext(), "동화가 저장되지 않았습니다.\n메인화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show()

            // 메인 화면으로 돌아가기
            val intent = Intent(requireContext(), MainActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
        }

        // 팝업창 생성 및 표시
        if (!requireActivity().isFinishing) {
            alertDialog = builder.create()
            alertDialog?.setView(container)
            alertDialog?.show()
        }
    }
}
