package com.example.myapplication

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ImageDetailFragment : DialogFragment() {

    private lateinit var imageView: ImageView
    private lateinit var imageUrl: String

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

        val downloadButton = view.findViewById<AppCompatButton>(R.id.download)
        downloadButton.setOnClickListener {
            Log.d("ImageDetailFragment", "Download button clicked")
            openImageFullscreen()
        }

        val closeButton = view.findViewById<AppCompatButton>(R.id.close)
        closeButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun openImageFullscreen() {
        val intent = Intent(activity, ImageFullscreenActivity::class.java)
        intent.putExtra("imageUrl", imageUrl)
        startActivity(intent)
        dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageUrl = arguments?.getString("imageUrl") ?: ""

        imageUrl.let {
            Glide.with(requireContext())
                .load(it)
                .apply(RequestOptions.overrideOf(500, 500))
                .into(imageView)
        }
    }
}
