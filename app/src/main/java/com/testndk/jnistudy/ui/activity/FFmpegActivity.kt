package com.testndk.jnistudy.ui.activity

import android.graphics.Color
import android.os.Environment
import android.text.TextUtils
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
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
    lateinit var ivPlay: ImageView
    lateinit var ivReplay: ImageView
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
        ivPlay = findViewById(R.id.ivPlay)
        ivReplay = findViewById(R.id.ivReplay)
        ivReplay.apply {
            tag = 0
            setOnClickListener {
                if (tag == 0) {
                    tag = 1
                    setColorFilter(Color.RED)
                } else {
                    tag = 0
                    setColorFilter(Color.BLACK)
                }
            }
        }
        seek_bar.setOnSeekBarChangeListener(this)
        ivPlay.setOnClickListener {
            if (ivPlay.isSelected) {
                mPlayer.pause()
            } else {
                ivPlay.isSelected = true

                mPlayer.start()
            }

        }
        mPlayer = FFmpegPlayer()
//        val filePath = File(
//            Environment.getExternalStorageDirectory().toString() + File.separator + "demo.mp4"
//        ).absolutePath
        val filePath = "rtmp://150.158.152.202/myapp/";
        if (TextUtils.isEmpty(filePath)) {
            toast("视频文件异常")
            return
        }
        mPlayer.run {
            setSurface(surface)
            setDataSource(filePath)
            setPrepareListener {
                mDuration = it
                runOnUiThread {
                    tvDuration.text = "00:00/00:${getTime(it)}"
                    ivPlay.isSelected = false
                }
            }
            setErrorListener { errorCode, errorMsg ->
                toast(errorMsg)
            }
            setCompleteListener(object : FFmpegPlayer.OnCompleteListener {
                override fun onComplete() {
                    runOnUiThread {
                        ivPlay.isSelected = false;
                    }
                }

                override fun onPause() {
                    runOnUiThread {
                        ivPlay.isSelected = false;
                    }
                }

            })
            setProgressListener {
                runOnUiThread {
                    if (!isTouch && mDuration != 0) {
                        tvDuration.text = "${getTime(it)}:${getTime(mDuration)}"
                        seek_bar.progress = 100 * it / mDuration
                    }
                }
            }
            prepare()
        }
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