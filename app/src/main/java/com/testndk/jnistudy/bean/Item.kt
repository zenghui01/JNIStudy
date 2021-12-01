package com.testndk.jnistudy.bean


data class Item(var sid: String, var text: String, var thumbnail: String) {
    override fun toString(): String {
        return "sid $sid，text $text，thumbnail $thumbnail"
    }
}