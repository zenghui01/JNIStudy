package com.testndk.jnistudy.bean


data class KtorTestModel(var code: Int, var message: String, var result: List<Item>) {
    override fun toString(): String {
        return "状态码 $code，msg $message，结果 $result "
    }
}