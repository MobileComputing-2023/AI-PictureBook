package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemBookButtonBinding

class BookListAdapter(private val bookTitles: Array<String>) : RecyclerView.Adapter<BookListAdapter.BookListViewHolder>() {
    class BookListViewHolder(val binding: ItemBookButtonBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun getItemCount() = bookTitles.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookListViewHolder {
        return BookListViewHolder(ItemBookButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BookListViewHolder, position: Int) {
        val binding = (holder as BookListViewHolder).binding
        binding.bookButton.text = bookTitles[position]
    }
}
