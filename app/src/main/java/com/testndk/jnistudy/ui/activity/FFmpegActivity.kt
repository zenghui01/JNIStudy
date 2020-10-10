package com.testndk.jnistudy.ui.activity

import android.os.Environment
import android.text.TextUtils
import android.view.SurfaceView
import android.widget.SeekBar
import android.widget.TextView
import com.testndk.jnistudy.R
import com.testndk.jnistudy.ui.ffmpeg.FFmpegPlayer
import com.testndk.jnistudy.utils.toast
import java.io.File

class FFmpegActivity : BaseActivity(), SeekBar.OnSeekBarChangeListener {
    private var isSeek = false;
    lateinit var surface: SurfaceView
    lateinit var seek_bar: SeekBar
    lateinit var tvDuration: TextView
    lateinit var mPlayer: FFmpegPlayer
    var mDuration = 0
    private var isTouch = false

    override fun initLayout(): Int {
        return R.layout.activity_ffmpeg
    }

    override fun initView() {
        super.initView()
        surface = findViewById(R.id.surface)
        seek_bar = findViewById(R.id.seek_bar)
        tvDuration = findViewById(R.id.tvDuration)
        seek_bar.setOnSeekBarChangeListener(this)

        mPlayer = FFmpegPlayer()
        val filePath = File(
            Environment.getExternalStorageDirectory().toString() + File.separator + "demo.mp4"
        ).absolutePath
        if (TextUtils.isEmpty(filePath)) {
            toast("视频文件异常")
            return
        }
        mPlayer.setSurface(surface)
        mPlayer.setDataSource(filePath)
        mPlayer.setPrepareListener {
            mDuration = it
            runOnUiThread {

                tvDuration.text = "00:00/00:${getTime(it)}"
            }
            mPlayer.start()
        }
        mPlayer.setErrorListener { errorCode, errorMsg ->
            toast(errorMsg)
        }
        mPlayer.setProgressListener {
            runOnUiThread {
                if (!isTouch) {
                    tvDuration.text = "${getTime(it)}:${getTime(mDuration)}"
                    seek_bar.progress = 100 * it / mDuration
                }
            }
        }
        mPlayer.prepare()
    }

    private fun getTime(duration: Int): String {
        return getMinutes(duration) + ":" + getSeconds(duration)
    }

    private fun getMinutes(duration: Int): String {
        val minutes = duration / 60
        return if (minutes <= 9) {
            "0$minutes"
        } else "" + minutes
    }

    private fun getSeconds(duration: Int): String {
        val seconds = duration % 60
        return if (seconds <= 9) {
            "0$seconds"
        } else "" + seconds
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            //progress 是进度条的进度 （0 - 100）
            tvDuration.text = getMinutes(progress * mDuration / 100) + ":" + getSeconds(
                progress * mDuration / 100
            ) + "/" + getMinutes(mDuration) + ":" + getSeconds(
                mDuration
            )
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        isTouch = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        isTouch = false
        isSeek = true;
        seekBar?.let {
            mPlayer.onSeek(it.progress * mDuration / 100)
        }
    }
}