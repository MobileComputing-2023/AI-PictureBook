package com.example.myapplication

import MyDatabase
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

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = (holder as MyViewHolder).binding
        val database = MyDatabase.getInstance(binding.root.context)

        binding.bookButton.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
        val title = database.getTitle(dataSet[position].bookId.toString())
        binding.bookTitle.text = title ?: ""

    }
}