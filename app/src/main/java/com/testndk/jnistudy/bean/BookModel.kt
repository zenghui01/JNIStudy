package com.testndk.jnistudy.bean

data class BookModel(
    var name: String = "",
    var price: Float = 0f,
    var type: Type = Type.ENGLISH
)
enum class Type {
    MATH,
    CHINESE,
    ENGLISH
}