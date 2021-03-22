package com.testndk.jnistudy.ui.composition;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.testndk.jnistudy.callback.OnProgressListener;
import com.testndk.jnistudy.utils.FileUtils;
import com.testndk.jnistudy.utils.LogUtils;
import com.testndk.jnistudy.utils.PcmToWavUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class VideoComposition {

    public void mixingVideo(String videoPath, String musicPath, OnProgressListener listener) throws Exception {
        int videoTrackIndex = -1;
        int audioTrackIndex = -1;
        int musicTrackIndex = -1;
        long videoDuration;
        //初始化视频提取器
        MediaExtractor extractor = new MediaExtractor();
        //设置提取路径
        extractor.setDataSource(videoPath);
        for (int index = 0; index < extractor.getTrackCount(); index++) {
            MediaFormat format = extractor.getTrackFormat(index);
            LogUtils.eLog("videoFormat", format);
            listener.onProgress(format.toString());
            //获取当前轨道类型
            String mime = format.getString(MediaFormat.KEY_MIME);
            //获取当前轨道时长
            videoDuration = format.getLong(MediaFormat.KEY_DURATION);
            if (mime.contains("video")) {
                videoTrackIndex = index;
            } else if (mime.contains("audio")) {
                audioTrackIndex = index;
            }
        }

        MediaExtractor audioExtractor = new MediaExtractor();
        audioExtractor.setDataSource(musicPath);
        for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
            MediaFormat format = audioExtractor.getTrackFormat(i);
            LogUtils.eLog("musicFormat", format);
            listener.onProgress(format.toString());
            //获取当前轨道类型
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.contains("audio")) {
                musicTrackIndex = i;
            }
        }
    }

    private static float normalizeVolume(int volume) {
        return volume / 100f * 1;
    }

    //     vol1  vol2  0-100  0静音  120
    public static void mixPcm(String pcm1Path, String pcm2Path, String toPath
            , int volume1, int volume2) throws IOException {
        float vol1 = normalizeVolume(volume1);
        float vol2 = normalizeVolume(volume2);
//一次读取多一点 2k
        byte[] buffer1 = new byte[2048];
        byte[] buffer2 = new byte[4096];
//        待输出数据
        byte[] buffer3 = new byte[2048];

        FileInputStream is1 = new FileInputStream(pcm1Path);
        FileInputStream is2 = new FileInputStream(pcm2Path);

//输出PCM 的
        FileOutputStream fileOutputStream = new FileOutputStream(toPath);
        short temp2, temp1;//   两个short变量相加 会大于short   声音
        int temp;
        boolean end1 = false, end2 = false;
        while (!end1 || !end2) {

            if (!end1) {
//
                end1 = (is1.read(buffer1) == -1);
//            音乐的pcm数据  写入到 buffer3
                System.arraycopy(buffer1, 0, buffer3, 0, buffer1.length);

            }
            LogUtils.eLog("长度:" + buffer1.length);
            if (!end2) {
                end2 = (is2.read(buffer2) == -1);
                LogUtils.eLog("长度2:" + buffer2.length);
                int voice = 0;//声音的值  跳过下一个声音的值    一个声音 2 个字节
                for (int i = 0; i < buffer1.length; i += 2) {
//                    或运算
                    temp1 = (short) ((buffer1[i] & 0xff) | (buffer1[i + 1] & 0xff) << 8);
                    temp2 = (short) ((buffer2[i * 2] & 0xff) | (buffer2[i * 2 + 1] & 0xff) << 8);
                    temp = (int) (temp1 * vol1 + temp2 * vol2);//音乐和 视频声音 各占一半
                    if (temp > 32767) {
                        temp = 32767;
                    } else if (temp < -32768) {
                        temp = -32768;
                    }
                    buffer3[i] = (byte) (temp & 0xFF);
                    buffer3[i + 1] = (byte) ((temp >>> 8) & 0xFF);
                }
                fileOutputStream.write(buffer3);
            }
        }
        is1.close();
        is2.close();
        fileOutputStream.close();
    }

    public void mixAudioTrack(Context context,
                              final String videoInput,
                              final String audioInput,
                              final String output,
                              final Integer startTimeUs, final Integer endTimeUs,
                              int videoVolume,//视频声音大小
                              int aacVolume//音频声音大小
    ) throws Exception {

        final File videoPcmFile = new File(Environment.getExternalStorageDirectory(), "video.pcm");
        final File musicPcmFile = new File(Environment.getExternalStorageDirectory(), "music.pcm");
        LogUtils.eLog("视频音频开始提取");
        decodeToPCM(videoInput, videoPcmFile.getAbsolutePath(), startTimeUs, endTimeUs);
        LogUtils.eLog("视频音频提取结束");
        decodeToPCM(audioInput, musicPcmFile.getAbsolutePath(), startTimeUs, endTimeUs);
        LogUtils.eLog("背景音频提取结束");
        final File mixPcmFile = new File(Environment.getExternalStorageDirectory(), "mix.pcm");
        mixPcm(videoPcmFile.getAbsolutePath(), musicPcmFile.getAbsolutePath(), mixPcmFile.getAbsolutePath(), videoVolume, aacVolume);
        new PcmToWavUtil(44100, AudioFormat.CHANNEL_IN_STEREO,
                2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(mixPcmFile.getAbsolutePath()
                , output);
    }

    //    MP3 截取并且输出  pcm
    @SuppressLint("WrongConstant")
    public void decodeToPCM(String musicPath, String outPath, int startTime, int endTime) throws Exception {
        if (endTime < startTime) {
            return;
        }
//    MP3  （zip  rar    ） ----> aac   封装个事 1   编码格式
//        jie  MediaExtractor = 360 解压 工具
        MediaExtractor mediaExtractor = new MediaExtractor();

        mediaExtractor.setDataSource(musicPath);
        int audioTrack = selectTrack(mediaExtractor);

        mediaExtractor.selectTrack(audioTrack);
// 视频 和音频
        mediaExtractor.seekTo(startTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
// 轨道信息  都记录 编码器
        MediaFormat oriAudioFormat = mediaExtractor.getTrackFormat(audioTrack);
        int maxBufferSize = 100 * 1000;
        if (oriAudioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            maxBufferSize = oriAudioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
//        h264   H265  音频
        MediaCodec mediaCodec = MediaCodec.createDecoderByType(oriAudioFormat.getString((MediaFormat.KEY_MIME)));
//        设置解码器信息    直接从 音频文件
        mediaCodec.configure(oriAudioFormat, null, null, 0);
        File pcmFile = new File(outPath);
        FileChannel writeChannel = new FileOutputStream(pcmFile).getChannel();
        mediaCodec.start();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int outputBufferIndex = -1;
        while (true) {
            int decodeInputIndex = mediaCodec.dequeueInputBuffer(100000);
            if (decodeInputIndex >= 0) {
                long sampleTimeUs = mediaExtractor.getSampleTime();

                if (sampleTimeUs == -1) {
                    break;
                } else if (sampleTimeUs < startTime) {
//                    丢掉 不用了
                    mediaExtractor.advance();
                    continue;
                } else if (sampleTimeUs > endTime) {
                    break;
                }
//                获取到压缩数据
                info.size = mediaExtractor.readSampleData(buffer, 0);
                info.presentationTimeUs = sampleTimeUs;
                info.flags = mediaExtractor.getSampleFlags();

//                下面放数据  到dsp解码
                byte[] content = new byte[buffer.remaining()];
                buffer.get(content);
//                输出文件  方便查看
//                FileUtils.writeContent(content, true, outPath.substring(0, outPath.indexOf(".")) + ".txt");
//                解码
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(decodeInputIndex);
                inputBuffer.put(content);
                mediaCodec.queueInputBuffer(decodeInputIndex, 0, info.size, info.presentationTimeUs, info.flags);
//                释放上一帧的压缩数据
                mediaExtractor.advance();
            }

            outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 100_000);
            while (outputBufferIndex >= 0) {
                ByteBuffer decodeOutputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                writeChannel.write(decodeOutputBuffer);//MP3  1   pcm2
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 100_000);
            }
        }
        writeChannel.close();
        mediaExtractor.release();
        mediaCodec.stop();
        mediaCodec.release();
//        转换MP3    pcm数据转换成mp3封装格式
//
//        File wavFile = new File(Environment.getExternalStorageDirectory(),"output.mp3" );
//        new PcmToWavUtil(44100,  AudioFormat.CHANNEL_IN_STEREO,
//                2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(pcmFile.getAbsolutePath()
//                , wavFile.getAbsolutePath());
        Log.i("David", "mixAudioTrack: 转换完毕");
    }

    private int selectTrack(MediaExtractor mediaExtractor) {
//获取每条轨道
        int numTracks = mediaExtractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
//            数据      MediaFormat
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }

    //视频拼接
    public boolean appendVideo(List<String> paths, String outputPath, OnProgressListener listener) throws Exception {
        MediaMuxer mediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        //初始化多媒体提取器
        listener.onProgress("初始化MediaMuxer媒体封装器");
        return appendVoid(paths, mediaMuxer, listener);
    }

    private boolean appendVoid(List<String> paths, MediaMuxer mediaMuxer, OnProgressListener listener) throws Exception {
        if (mediaMuxer == null)
            return false;
        long fileStartTime = 0;
        long fileDuration = 0;

        int videoTrackIndex = -1;
        int audioTrackIndex = -1;

        int sourceVideoTrack1 = -1;
        int sourceAudioTrack1 = -1;

        MediaCodec.BufferInfo info;

        int sampleSize;
        for (String path : paths) {
            //初始化多媒体提取器
            listener.onProgress("初始化MediaExtractor媒体提取器");
            MediaExtractor extractor = new MediaExtractor();
            //设置提取路径
            extractor.setDataSource(path);
            listener.onProgress("设置MediaExtractor路径");
            for (int index = 0; index < extractor.getTrackCount(); index++) {
                //获取通道的信息
                listener.onProgress("设置获取媒体文件通道信息");
                MediaFormat format = extractor.getTrackFormat(index);

                //获取通道类型
                listener.onProgress("设置获取媒体文件通道类型");
                String mime = format.getString(MediaFormat.KEY_MIME);
                //获取通道时长
                fileDuration = format.getLong(MediaFormat.KEY_DURATION);
                if (mime.startsWith("video/")) {
                    sourceVideoTrack1 = index;
                    if (videoTrackIndex == -1) {
                        listener.onProgress("MediaMuxer 添加视轨");
                        videoTrackIndex = mediaMuxer.addTrack(format);
                    }
                } else if (mime.startsWith("audio/")) {
                    sourceAudioTrack1 = index;
                    if (audioTrackIndex == -1) {
                        listener.onProgress("MediaMuxer 添加音轨");
                        audioTrackIndex = mediaMuxer.addTrack(format);
                    }
                }
            }
            if (fileStartTime == 0) {
                listener.onProgress("MediaMuxer 启动合成");
                mediaMuxer.start();
            }
            //多媒体提取去锁住当前通道
            listener.onProgress("设置MediaExtractor锁定当前轨道");
            extractor.selectTrack(sourceVideoTrack1);
            listener.onProgress("设置MediaCodec.BufferInfo信息");
            info = new MediaCodec.BufferInfo();
            //设置视频pts pts影响视频播放顺序
            listener.onProgress("设置pts");
//            info.presentationTimeUs = fileStartTime;
            ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
            while ((sampleSize = extractor.readSampleData(buffer, 0)) > 0) {
                byte[] data = new byte[buffer.remaining()];

                buffer.get(data);
//            FileUtils.writeBytes(data);
//            FileUtils.writeContent(data, false);
                info.offset = 0;
                info.size = sampleSize;
                info.flags = extractor.getSampleFlags();
                info.presentationTimeUs = extractor.getSampleTime() + fileStartTime;
                mediaMuxer.writeSampleData(videoTrackIndex, buffer, info);
                extractor.advance();
            }
            listener.onProgress(path + " 文件视频轨添加结束");
            //2.write first audio track into muxer.
            listener.onProgress("解锁视频轨");
            extractor.unselectTrack(sourceVideoTrack1);
            listener.onProgress("重新锁定音频轨");
            extractor.selectTrack(sourceAudioTrack1);
            info = new MediaCodec.BufferInfo();
//            info.presentationTimeUs = fileStartTime;
            buffer = ByteBuffer.allocate(500 * 1024);
            listener.onProgress("设置buffer信息");
            while ((sampleSize = extractor.readSampleData(buffer, 0)) > 0) {
                info.offset = 0;
                info.size = sampleSize;
                info.flags = extractor.getSampleFlags();
//            byte[] data = new byte[buffer.remaining()];
//            buffer.get(data);
//            FileUtils.writeBytes(data);
//            FileUtils.writeContent(data);
                info.presentationTimeUs = extractor.getSampleTime() + fileStartTime;
                mediaMuxer.writeSampleData(audioTrackIndex, buffer, info);
                extractor.advance();
            }
            listener.onProgress(path + " 文件音频轨添加结束");
            listener.onProgress(path + " 文件添加结束");
//            videoExtractor1.release();
            fileStartTime += fileDuration;
            listener.onProgress("记录当前文件合成后,pts.并且开始合成下一个视频");
        }
//
//
//        MediaExtractor videoExtractor2 = new MediaExtractor();
//        videoExtractor2.setDataSource(inputPath2);
//
//        int sourceVideoTrack2 = -1;
//        int sourceAudioTrack2 = -1;
//        for (int index = 0; index < videoExtractor2.getTrackCount(); index++) {
//            MediaFormat format = videoExtractor2.getTrackFormat(index);
//            String mime = format.getString(MediaFormat.KEY_MIME);
//            if (mime.startsWith("video/")) {
//                sourceVideoTrack2 = index;
//            } else if (mime.startsWith("audio/")) {
//                sourceAudioTrack2 = index;
//            }
//        }
//
//
//        //3.write second video track into muxer.
//        videoExtractor2.selectTrack(sourceVideoTrack2);
//        info = new MediaCodec.BufferInfo();
//        info.presentationTimeUs = file1_duration;
//        buffer = ByteBuffer.allocate(500 * 1024);
//        sampleSize = 0;
//        while ((sampleSize = videoExtractor2.readSampleData(buffer, 0)) > 0) {
//            info.offset = 0;
//            info.size = sampleSize;
//            info.flags = videoExtractor2.getSampleFlags();
//            info.presentationTimeUs = videoExtractor2.getSampleTime() + file1_duration;
//            mediaMuxer.writeSampleData(videoTrackIndex, buffer, info);
//            videoExtractor2.advance();
//        }
//
//        //4.write second audio track into muxer.
//        videoExtractor2.unselectTrack(sourceVideoTrack2);
//        videoExtractor2.selectTrack(sourceAudioTrack2);
//        info = new MediaCodec.BufferInfo();
//        info.presentationTimeUs = file1_duration;
//        buffer = ByteBuffer.allocate(500 * 1024);
//        sampleSize = 0;
//        while ((sampleSize = videoExtractor2.readSampleData(buffer, 0)) > 0) {
//            info.offset = 0;
//            info.size = sampleSize;
//            info.flags = videoExtractor2.getSampleFlags();
//            info.presentationTimeUs = videoExtractor2.getSampleTime() + file1_duration;
//            mediaMuxer.writeSampleData(audioTrackIndex, buffer, info);
//            videoExtractor2.advance();
//        }
//        videoExtractor2.release();
        mediaMuxer.stop();
        mediaMuxer.release();
        return true;
    }

}
