package com.testndk.jnistudy.ui.rtmp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.testndk.jnistudy.utils.ExpandKt;
import com.testndk.jnistudy.utils.LogUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioChannel {
    private Pusher mPusher;
    private AudioRecord audioRecord;
    private int channels = 2;
    private boolean isPlay;
    private ExecutorService executorService;
    private int inputSimples;
    private int sampleRateInHz = 44100;

    public AudioChannel(Pusher pusher) {
        mPusher = pusher;
        executorService = Executors.newSingleThreadExecutor();
        int channelConfig;
        if (channels == 2) {
            channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        } else {
            channelConfig = AudioFormat.CHANNEL_IN_MONO;
        }
        /**
         * 编码器初始化
         */
        mPusher.initAudioEncoderNative(sampleRateInHz, channels);

        /**
         * 获取编码器编译样本数
         */
        inputSimples = mPusher.getInputSimplesNative() * 2;//为什么要*2?,因为我们编码是16个字节  16bit = 2 字节;
        LogUtils.eLog("编码器编译样本数", inputSimples);
        //录制期间将音频数据写入缓冲区的总大小
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, AudioFormat.ENCODING_PCM_16BIT) * 2;
        /**
         * 参数1:输入源
         * 参数2:采样率，以赫兹表示。目前，只有44100Hz是可以在所有设备上使用的速率，但是其他速率（例如22050，* 16000和11025）可以在某些设备上使用
         * 参数3:描述音频通道的配置。
         * 参数4:返回音频数据的格式。
         * 参数5:录制期间将音频数据写入缓冲区的总大小.
         */
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, Math.max(inputSimples, minBufferSize));
    }

    public void startLive() {
        if (inputSimples < 0) {
            ExpandKt.toast("初始化异常");
            return;
        }
        isPlay = true;
        executorService.submit(new AudioTask());
    }

    public void stopLive() {
        isPlay = false;
    }

    public void release() {
    }

    private class AudioTask implements Runnable {
        @Override
        public void run() {
            LogUtils.eLog("开始录制音频", isPlay);
            audioRecord.startRecording();//开始录制
            //每次读多少由编译器编译样本数来决定
            byte[] data = new byte[inputSimples];
            while (isPlay) {
                int len = audioRecord.read(data, 0, data.length);
                if (len > 0) {
                    //采集成功
                    //交由native层进行编码
                    mPusher.pushAudioData(data);
                }
            }
            audioRecord.stop();//停止录制
        }
    }
}
