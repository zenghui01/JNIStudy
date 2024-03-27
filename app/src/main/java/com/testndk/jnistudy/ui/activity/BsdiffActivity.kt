package com.testndk.jnistudy.ui.activity

import android.view.View
import com.testndk.jnistudy.R
import com.testndk.jnistudy.ui.BaseActivity
import com.testndk.jnistudy.ui.bsdiff.BsdiffUtilsJava

class BsdiffActivity : BaseActivity() {
   override fun initLayout() = R.layout.activity_bsdiff

   fun onClickMerge(view: View?) {
       BsdiffUtilsJava.mergeApk(this)
   }
}
