package com.testndk.jnistudy.ui.activity

import android.content.Intent
import android.graphics.Color
import android.provider.MediaStore
import android.text.TextUtils
import android.view.SurfaceView
import android.view.View
import android.widget.*
import com.testndk.jnistudy.R
import com.testndk.jnistudy.ui.ffmpeg.FFmpegPlayer
import com.testndk.jnistudy.utils.LogUtils
import com.testndk.jnistudy.utils.UriUtil
import com.testndk.jnistudy.utils.toast
import java.util.*

class FFmpegActivity : BaseActivity(), SeekBar.OnSeekBarChangeListener {
    private var isSeek = false;
    lateinit var flParent: FrameLayout
    lateinit var seek_bar: SeekBar
    lateinit var tvDuration: TextView
    lateinit var mPlayer: FFmpegPlayer
    lateinit var ivPlay: ImageView
    lateinit var ivReplay: ImageView
    lateinit var llProgress: LinearLayout
    lateinit var tvNotice: TextView
    lateinit var tvSelect: Button
    lateinit var surfaceView: SurfaceView
    var mDuration = 0
    private var isTouch = false
    private val REQUEST_CODE_SELECT = 112

    override fun initLayout(): Int {
        return R.layout.activity_ffmpeg
    }

    override fun initView() {
        super.initView()
        surfaceView = SurfaceView(this);
        flParent = findViewById(R.id.flParent)
        seek_bar = findViewById(R.id.seek_bar)
        tvDuration = findViewById(R.id.tvDuration)
        ivPlay = findViewById(R.id.ivPlay)
        ivReplay = findViewById(R.id.ivReplay)
        llProgress = findViewById(R.id.llProgress)
        tvNotice = findViewById(R.id.tvNotice)
        tvSelect = findViewById(R.id.tvSelect)
        flParent.addView(surfaceView)
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
        tvSelect.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i, REQUEST_CODE_SELECT)
        }
        mPlayer = FFmpegPlayer()
//        val filePath = File(
//            Environment.getExternalStorageDirectory().toString() + File.separator + "demo.mp4"
//        ).absolutePath
//        val filePath = "rtmp://47.96.225.33/myapp/";

    }

    fun playVideo(filePath: String) {
        mPlayer.run {
            setSurface(surfaceView)
            setDataSource(filePath)
            setPrepareListener {
                mDuration = it
                runOnUiThread {
                    if (mDuration == 0) {
                        llProgress.visibility = View.INVISIBLE
                        ivReplay.visibility = View.GONE
                        tvNotice.text = "正在直播"
                    } else {
                        ivReplay.visibility = View.VISIBLE
                        llProgress.visibility = View.VISIBLE
                        tvNotice.text = "本地视频/网络视频"
                        tvDuration.text = "00:00/00:${getTime(it)}"
                    }
                    tvNotice.visibility = View.VISIBLE
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (requestCode == REQUEST_CODE_SELECT && resultCode == RESULT_OK && null != data) {
                val uri = data.data
                LogUtils.eLog(uri, uri)
                val path = UriUtil.getPath(this, uri)
                playVideo(path)
            }
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