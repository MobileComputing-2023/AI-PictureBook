package com.example.myapplication

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.ActivityTermspopBinding

class TermsPopFragment : DialogFragment() {
    private lateinit var binding: ActivityTermspopBinding
    private var alertDialog: AlertDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = ActivityTermspopBinding.inflate(requireActivity().layoutInflater)
        val view = binding.root

        builder.setView(view)
        return builder.create()
}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityTermspopBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!requireActivity().isFinishing) {
            showPopupDialog(binding)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // 다이얼로그를 닫기 위해 onDestroy에서 호출
        dismiss()
        alertDialog?.dismiss()
    }

    private fun showPopupDialog(binding: ActivityTermspopBinding) {
        // 팝업창 생성
        val builder = AlertDialog.Builder(requireActivity())

        //버튼 외 화면, backpress 눌러도 화면 꺼지지 않음
        builder.setCancelable(false)

        // LinearLayout을 생성하고 binding의 루트 뷰를 추가
        val container = LinearLayout(requireActivity())

        // Remove the view from its current parent
        val parent = binding.root.parent as? ViewGroup
        parent?.removeView(binding.root)

        // 클릭, 포커스 막음
        binding.root.isClickable = false
        binding.root.isFocusable = false

        container.addView(binding.root)

        binding.closeBtn.setOnClickListener {
            // 팝업창 닫기
            dismiss()
            alertDialog?.dismiss()
        }

        // 팝업창 생성 및 표시
        if (!requireActivity().isFinishing) {
            alertDialog = builder.create()
            alertDialog?.setView(container)
            alertDialog?.show()
        }
    }
}