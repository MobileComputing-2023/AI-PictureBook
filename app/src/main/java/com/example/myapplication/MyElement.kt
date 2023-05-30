package com.example.myapplication

data class MyElement(
    val pageId: Int,
    val bookId: Int,
    val pageNumber: Int,
    val imageUrl: String,
    val drawingData : ByteArray,
    val text: String
)
