package com.testndk.jnistudy.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.testndk.jnistudy.R
import com.testndk.jnistudy.ui.activity.*
import java.util.function.Consumer

class FirstActivity : BaseActivity() {

    override fun initLayout(): Int {
        return R.layout.activity_first;
    }

    override fun initView() {
        super.initView()
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

    fun onClickEdit(view: View) {
        start(EditWeightActivity::class.java)
    }

    fun onClickCamera(view: View) {
        val permissions = RxPermissions(this)
        if ((!permissions.isGranted(Manifest.permission.CAMERA) || !permissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                    || !permissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        ) {
            mDisposable.add(permissions.request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).subscribe { granted ->
                if (granted) {
                    start(CameraActivity::class.java)
                }
            })
        } else {
            start(CameraActivity::class.java)
        }
    }

    fun onClickLoadGif(view: View) {
        start(GifActivity::class.java)
    }
}