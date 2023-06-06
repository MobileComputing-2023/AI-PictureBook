package com.example.myapplication

import MyDatabase
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemBookButtonBinding

class MyAdapter(private var dataSet: MutableList<MyElement>): RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: ItemBookButtonBinding) : RecyclerView.ViewHolder(binding.root)
    override fun getItemCount() = dataSet.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemBookButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    fun setList(newList : MutableList<MyElement>) {
        this.dataSet = newList
    }

    fun getElement(pos : Int): MyElement {
        return dataSet[pos]
    }

    private lateinit var itemClickListener: OnItemClickListener
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    /// private var selectedItemPosition = RecyclerView.NO_POSITION
    private var deletePosition: Int = -1
    fun setDeletePosition(position: Int) {
        deletePosition = position
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = (holder as MyViewHolder).binding
        val database = MyDatabase.getInstance(binding.root.context)

        val coverImg = database.getImageForPage(dataSet[position].bookId.toString(), 0)
        binding.bookButton.setImageBitmap(coverImg)

        // 삭제하기 선택 후 버튼 클릭 시 테두리 색상 변경
        if (deletePosition == position) {
            binding.bookButton.setBackgroundResource(R.drawable.selected_book_button_border)
        } else {
            binding.bookButton.setBackgroundResource(R.drawable.default_book_button_border)
        }

        binding.bookButton.setOnClickListener {
            itemClickListener.onClick(it, position)
        }

        val title = database.getTitle(dataSet[position].bookId.toString())
        binding.bookTitle.text = title ?: ""

    }
}
