package com.testndk.jnistudy.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.testndk.jnistudy.R
import com.testndk.jnistudy.ui.activity.BaseActivity
import com.testndk.jnistudy.ui.activity.BitmapOptionActivity
import com.testndk.jnistudy.ui.activity.MainActivity

class FirstActivity : BaseActivity() {

    override fun initLayout(): Int {
        return R.layout.activity_first;
    }


    private fun start(clazz: Class<*>) {
        val intent = Intent(this, clazz)
        this.startActivity(intent);
    }

    fun onClickAndFix(view: View) {
        start(MainActivity::class.java)
    }

    fun onClickBitmapOption(view: View) {
        start(BitmapOptionActivity::class.java)
    }

    fun onClickBsdiff(view: View) {

    }

    fun onClickFFmpeg(view: View) {

    }
}