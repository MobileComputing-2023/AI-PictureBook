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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.ActivityReadallpopBinding

class ReadAllPopFragment : DialogFragment() {
    private lateinit var binding: ActivityReadallpopBinding
    private lateinit var title: String
    private lateinit var myDatabase: MyDatabase
    private var alertDialog: AlertDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = ActivityReadallpopBinding.inflate(requireActivity().layoutInflater)
        val view = binding.root

        builder.setView(view)

        myDatabase = MyDatabase.getInstance(requireContext()) // myDatabase 초기화

        return builder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityReadallpopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = arguments?.getString("title") ?: ""
        myDatabase = MyDatabase.getInstance(requireContext())

        if (!requireActivity().isFinishing) {
            showPopupDialog(title)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 다이얼로그를 닫기 위해 onDestroy에서 호출합니다.
        alertDialog?.dismiss()
    }

    private fun showPopupDialog(title: String) {
        Log.d("ReadAllPopFragment", "Title: $title")

        // 팝업창 생성
        val builder = AlertDialog.Builder(requireContext())

        //버튼 외 화면, backpress 눌러도 화면 꺼지지않음
        builder.setCancelable(false)

        // LinearLayout을 생성하고 binding의 루트 뷰를 추가합니다.
        val container = LinearLayout(requireContext())

        // Remove the view from its current parent
        val parent = binding.root.parent as? ViewGroup
        parent?.removeView(binding.root)

        // Disable clicks and focus for the root view
        binding.root.isClickable = false
        binding.root.isFocusable = false

        binding.titleText.text = "『$title』을\n마지막 페이지까지 다 읽었습니다!"

        // Add the view to the new parent
        container.addView(binding.root)

        binding.toListBtn.setOnClickListener {
            // 팝업창 닫기
            dismiss()

            // 동화리스트 화면으로 가기
            val intent = Intent(requireContext(), ListActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.fromright_toleft, R.anim.none)
        }

        binding.toMainBtn.setOnClickListener {
            // 팝업창 닫기
            dismiss()

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
