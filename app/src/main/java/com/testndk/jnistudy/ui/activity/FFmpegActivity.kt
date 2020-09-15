package com.testndk.jnistudy.ui.activity

import android.os.Environment
import android.text.TextUtils
import android.widget.TextView
import com.testndk.jnistudy.R
import com.testndk.jnistudy.ui.ffmpeg.FFmpegPlayer
import com.testndk.jnistudy.utils.toast
import java.io.File

class FFmpegActivity : BaseActivity() {

    override fun initLayout(): Int {
        return R.layout.activity_ffmpeg
    }

    override fun initView() {
        super.initView()
        val fFmpegPlayer = FFmpegPlayer()
        val filePath = File(
            Environment.getExternalStorageDirectory().toString() + File.separator + "demo.mp4"
        ).absolutePath
        if (TextUtils.isEmpty(filePath)) {
            toast("视频文件异常")
            return
        }
        findViewById<TextView>(R.id.tvVersion).text = filePath
        fFmpegPlayer.setDataSource(filePath)
        fFmpegPlayer.setPrepareListener {
            toast("加载成功")
        }
        fFmpegPlayer.setErrorListener { errorCode, errorMsg ->
            toast(errorMsg)
        }
        fFmpegPlayer.prepare()
    }
}