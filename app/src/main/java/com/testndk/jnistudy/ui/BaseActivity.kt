package com.testndk.jnistudy.ui

import android.app.Activity
import android.os.Bundle

abstract class BaseActivity : Activity() {
   abstract fun initLayout(): Int

   open fun initView() {
   }

   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       setContentView(initLayout())
       initView()
   }
}