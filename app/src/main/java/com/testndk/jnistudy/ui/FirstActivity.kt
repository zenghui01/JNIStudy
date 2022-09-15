package com.testndk.jnistudy.ui

import android.Manifest
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.tbruyelle.rxpermissions2.RxPermissions
import com.testndk.jnistudy.BuildConfig
import com.testndk.jnistudy.R
import com.testndk.jnistudy.aspect.annotation.TestAnnotation
import com.testndk.jnistudy.ui.activity.BaseActivity
import com.testndk.jnistudy.ui.activity.BitmapOptionActivity
import com.testndk.jnistudy.ui.activity.BsdiffActivity
import com.testndk.jnistudy.ui.activity.CameraActivity
import com.testndk.jnistudy.ui.activity.CastClientActivity
import com.testndk.jnistudy.ui.activity.CastServiceActivity
import com.testndk.jnistudy.ui.activity.EditWeightActivity
import com.testndk.jnistudy.ui.activity.FFmpegActivity
import com.testndk.jnistudy.ui.activity.FmodActivity
import com.testndk.jnistudy.ui.activity.GifActivity
import com.testndk.jnistudy.ui.activity.KtorActivity
import com.testndk.jnistudy.ui.activity.MainActivity
import com.testndk.jnistudy.ui.activity.PathGroupActivity
import com.testndk.jnistudy.ui.activity.RTMPActivity
import com.testndk.jnistudy.ui.activity.TestAspectActivity
import com.testndk.jnistudy.ui.activity.TestKotlinApiActivity
import com.testndk.jnistudy.ui.activity.VideoCompositionActivity
import com.testndk.jnistudy.ui.activity.VideoMixingActivity
import com.testndk.jnistudy.ui.opengl.OpenGLActivity
import com.testndk.jnistudy.utils.isEquals

class FirstActivity : BaseActivity() {
    val permissions: RxPermissions by lazy { RxPermissions(this) }
    override fun initLayout(): Int {
        return R.layout.activity_first;
    }

    override fun initView() {
        super.initView()
        findViewById<TextView>(R.id.tvUrl).text = "当前环境 ${BuildConfig.app_url}"
        initPermission()
    }

    private fun initPermission() {
        if ((!permissions.isGranted(Manifest.permission.CAMERA) || !permissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                    || !permissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) || !permissions.isGranted(
                Manifest.permission.RECORD_AUDIO
            ))
        ) {
            mDisposable.add(permissions.requestEach(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
            ).subscribe {
                if (isEquals(it.name, Manifest.permission.CAMERA)) {

                }
            })
        }
    }

    private fun Class<*>.start() {
        this@FirstActivity.startActivity(Intent(this@FirstActivity, this));
    }

    fun onClickAndFix(view: View) {
        MainActivity::class.java.start()
    }

    fun onClickBitmapOption(view: View) {
        BitmapOptionActivity::class.java.start()
    }

    fun onClickBsdiff(view: View) {

    }


    fun onClickFFmpeg(view: View) {
        FFmpegActivity::class.java.start()
    }

    @TestAnnotation
    fun onClickAspectFirst(view: View) {
        TestAspectActivity::class.java.start()
    }

    fun onClickEdit(view: View) {
        EditWeightActivity::class.java.start()
    }

    fun onClickCamera(view: View) {
        if ((!permissions.isGranted(Manifest.permission.CAMERA) || !permissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                    || !permissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        ) {
            mDisposable.add(permissions.request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).subscribe { granted ->
                if (granted) {
                    CameraActivity::class.java.start()
                }
            })
        } else {
            CameraActivity::class.java.start()
        }
    }

    fun onClickLoadGif(view: View) {
        GifActivity::class.java.start()
    }

    fun onClickMergeApk(view: View) {
        BsdiffActivity::class.java.start()
    }

    fun onClickFmod(view: View) {
        FmodActivity::class.java.start()
    }

    fun onClickRTMP(view: View) {
        RTMPActivity::class.java.start()
    }

    fun onClickCastService(view: View) {
        CastServiceActivity::class.java.start()
    }

    fun onclickCastClient(view: View) {
        CastClientActivity::class.java.start()
    }

    fun onclickMediaExtra(view: View) {
        VideoCompositionActivity::class.java.start()
    }

    fun onclickVideoMixing(view: View) {
        VideoMixingActivity::class.java.start()
    }

    fun onClickKtor(view: View) {
        KtorActivity::class.java.start()
    }

    fun onClickWorkManager(view: View) {
        TestKotlinApiActivity::class.java.start()
    }

    fun onClickDataStore(view: View) {
        TestMovePointActivity::class.java.start()
    }

    fun onClickPath(view: View) {
        PathGroupActivity::class.java.start()
    }

    fun onClickOpenGL(view: View) {
        OpenGLActivity::class.java.start()
    }

}