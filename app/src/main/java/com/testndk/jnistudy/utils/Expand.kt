//package com.testndk.jnistudy.utils
//
//import android.text.TextUtils
//import android.util.Log
//import android.widget.Toast
//import com.alibaba.fastjson.JSON
//import com.testndk.jnistudy.MyApplication
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import java.lang.StringBuilder
//
//fun isEmpty(s: String): Boolean {
//    return TextUtils.isEmpty(s)
//}
//
//fun isEquals(s: String, s1: String): Boolean {
//    return TextUtils.equals(s, s1);
//}
//
//fun toast(s: String) {
//    GlobalScope.launch(Dispatchers.Main) {
//        Toast.makeText(MyApplication.INSTANCE, s, Toast.LENGTH_SHORT).show()
//    }
//}
//
//fun loge(vararg s: Any) {
//    val builder = StringBuilder()
//    for (any in s) {
//        builder.append(JSON.toJSONString(s))
//    }
//    Log.e("error_log", builder.toString())
//}