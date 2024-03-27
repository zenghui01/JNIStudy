//package com.testndk.jnistudy.ui.cast;
//
//import android.hardware.display.DisplayManager;
//import android.media.MediaCodec;
//import android.media.MediaCodecInfo;
//import android.media.MediaFormat;
//import android.media.projection.MediaProjection;
//import android.view.Surface;
//
//import com.testndk.jnistudy.utils.FileUtils;
//import com.testndk.jnistudy.utils.LogUtils;
//
//import java.nio.ByteBuffer;
//
//public class CodecLiveH264 extends CodecLive {
//    //i帧在h264是5
//    private int NAL_I = 5;
//    //sps在h264中是7
//    private int NAL_SPS = 7;
//    //保存的sps数据
//    private byte[] vpsBytes;
//
//    public CodecLiveH264(SocketLiveService socketLive, MediaProjection mediaProjection) {
//        super(socketLive, mediaProjection);
//    }
//
//    @Override
//    public void startLive() {
//        super.startLive();
//        try {
//            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
//            onProgress("创建MediaFormat");
//            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//            onProgress("设置采集编码颜色");
//            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
//            onProgress("设置码流");
//            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
//            onProgress("设置帧数");
//            //设置i帧间隔
//            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
//            onProgress("设置关键帧");
//            //创建h265编码器
//            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
//            onProgress("创建video/avc(H264) MediaCodec");
//            //设置编码器参数
//            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            onProgress("MediaCodec设置MediaFormat参数");
//            //创建虚拟场地
//            onProgress("MediaCodec创建虚拟surface");
//            Surface surface = mediaCodec.createInputSurface();
//            //创建虚拟预览
//            onProgress("MediaCodec创建虚拟预览");
//            virtualDisplay = mediaProjection.createVirtualDisplay(
//                    "-h264push", width, height,
//                    1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
//                    surface, null, null);
//            start();
//        } catch (Exception e) {
//            e.printStackTrace();
//            onProgress("H265参数设置异常" + e.getMessage());
//        }
//    }
//
//    @Override
//    public void run() {
//        super.run();
//        onProgress("开始编码数据");
//        mediaCodec.start();
//        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//        while (isPlay) {
//            int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
//            if (outputBufferId >= 0) {
//                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
//                dealFrame(outputBuffer, bufferInfo);
//                mediaCodec.releaseOutputBuffer(outputBufferId, false);
//            }
//        }
//        onProgress("停止当前推送和录屏");
//        mediaProjection.stop();
//        mediaCodec.stop();
//        mediaCodec.release();
//    }
//
//    private void dealFrame(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {
//        int index = 4; // 00 00 00 01
//        if (outputBuffer.get(2) == 0X1) { // 00 00 01
//            index = 3;
//        }
//        int naluType = outputBuffer.get(index) & 0x1F;
//        if (naluType == NAL_SPS) {
//            vpsBytes = new byte[bufferInfo.size];
//            outputBuffer.get(vpsBytes);
//            onProgress(FileUtils.writeContent(vpsBytes,false));
//        } else if (naluType == NAL_I) {
//            byte[] h264Byte = new byte[bufferInfo.size];
//            outputBuffer.get(h264Byte);
//            byte[] keyframe = new byte[bufferInfo.size + vpsBytes.length];
//            System.arraycopy(vpsBytes, 0, keyframe, 0, vpsBytes.length);
//            System.arraycopy(h264Byte, 0, keyframe, vpsBytes.length, h264Byte.length);
//            this.socketLive.sendData(keyframe);
//        } else {
//            byte[] pOrbData = new byte[bufferInfo.size];
//            outputBuffer.get(pOrbData);
//            this.socketLive.sendData(pOrbData);
//        }
//    }
//
//
//}
