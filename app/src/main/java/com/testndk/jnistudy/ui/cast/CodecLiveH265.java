package com.testndk.jnistudy.ui.cast;

import android.hardware.display.DisplayManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.view.Surface;

import com.testndk.jnistudy.utils.FileUtils;
import com.testndk.jnistudy.utils.LogUtils;

import java.nio.ByteBuffer;

public class CodecLiveH265 extends CodecLive {
    //i帧在h265是19
    private int NAL_I = 19;
    //vps在h265中是32
    private int NAL_VPS = 32;
    private byte[] vpsBytes;

    public CodecLiveH265(SocketLiveService socketLive, MediaProjection mediaProjection) {
        super(socketLive, mediaProjection);
    }


    @Override
    public void startLive() {
        super.startLive();
        try {
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height);
            onProgress("创建MediaFormat");
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            onProgress("设置采集编码颜色");
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            onProgress("设置码率");
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
            onProgress("设置帧数");
            //设置i帧间隔
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            onProgress("设置关键帧");
            //创建h265编码器
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);
            onProgress("创建video/hevc(H265) MediaCodec");
            //设置编码器参数
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            onProgress("MediaCodec设置MediaFormat参数");
            //创建虚拟场地
            Surface surface = mediaCodec.createInputSurface();
            onProgress("MediaCodec创建输入surface");
            //创建虚拟预览
            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "-h265push", width, height,
                    1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    surface, null, null);
            onProgress("MediaCodec创建虚拟预览");
            start();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.eLog(e.fillInStackTrace());
            onProgress("H265参数设置异常");
        }
    }

    @Override
    public void run() {
        super.run();
        onProgress("开始编码数据");
        mediaCodec.start();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (isPlay) {
            int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
            if (outputBufferId >= 0) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
                dealFrame(outputBuffer, bufferInfo);
                mediaCodec.releaseOutputBuffer(outputBufferId, false);
            }
        }
        onProgress("停止当前推送和录屏");
        mediaProjection.stop();
        mediaCodec.stop();
        mediaCodec.release();
    }

    //vps sps pps
    //
    //0000 0001 40           01            0c
    //		  0010 1000
    //
    //		  0禁止位
    //		   010 100  帧类型
    //		   		  0 视频层ID
    //
    //vps sps pps 帧类型是：32
    //I帧类型： 19
    //
    //
    //
    //7E 1111110
    //
    //与运算的运算规则是0&0=0;  0&1=0;   1&0=0;    1&1=1;即：两位同时为“1”，结果才为“1”，否则为0。
    //
    //在计算h265帧类型时需要&7E并且向左平移一位
    //
    //&7E的原因是去除前面禁止位
    //
    //左移一位是为了去除后面的视频层ID
    //I420:YYYYYYYY UUVV
    //YV12:YYYYYYYY VVUU
    //NV12:YYYYYYYY UVUV
    //NV21:YYYYYYYY VUVU
    private void dealFrame(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {
        //偏移分隔符00 00 00 01
        int offset = 4;
        //移除视频层id,避免被影响
        int type = (outputBuffer.get(offset) & 0x7E) >> 1;
        if (type == NAL_VPS) {
//            onProgress("获取Vps数据,并保存");
            //因为在录屏是只会出现一次,vps sps pps数据,为了网络传输时,可能出现的丢数据问题.
            //将vps sps pps数据保存一份,每次出现I帧的时候在I帧前面添加该数据避免对数据问题.

            vpsBytes = new byte[bufferInfo.size];
            outputBuffer.get(vpsBytes);
            onProgress(FileUtils.writeContent(vpsBytes, false));
        } else if (type == NAL_I) {
//            onProgress("获取I数据,并在I帧数据前塞入vps数据.防止网络传输时,丢失vps等数据的情况");
            byte[] h265Byte = new byte[bufferInfo.size];
            outputBuffer.get(h265Byte);
            //将vps sps pps数据和h265数据进行组合
            byte[] newBuf = new byte[vpsBytes.length + h265Byte.length];
            //拷贝vps sps pps数据在前面
            System.arraycopy(vpsBytes, 0, newBuf, 0, vpsBytes.length);
            //拷贝I帧数据
            System.arraycopy(h265Byte, 0, newBuf, vpsBytes.length, h265Byte.length);
            //通过socket发送数据
            this.socketLive.sendData(newBuf);
        } else {
//            onProgress("P帧或者B数据直接发送");
            //剩下的就只剩p帧和b帧的数据,无需处理直接发送即可
            byte[] pOrbData = new byte[bufferInfo.size];
            outputBuffer.get(pOrbData);
            this.socketLive.sendData(pOrbData);
        }
    }
}
