//
// Created by zenghui on 2020/9/9.
//


#include "FFmpegPlayer.h"

void *task_prepare(void *args) {
    auto *player = static_cast<FFmpegPlayer *>(args);
    player->player_prepare();
    return 0;
}

void *task_start(void *args) {
    auto *player = static_cast<FFmpegPlayer *>(args);
    player->player_start();
    return 0;
}

FFmpegPlayer::FFmpegPlayer(const char *data_source_) {
    //为什么不能使用这种方式赋值? this->data_source=data_source_;
    //由于data_source_有可能被释放掉,导致data_source成为悬空指针,出现异常
    //所以需要对data_source_ 的值copy到data_source中

    //+1的原因
    //由于在c中字符串后会跟\0结尾
    //例如:"hello" 在c中表现为"hello\0"故需要+1
    this->data_source = new char[strlen(data_source_) + 1];
    //将值copy到data_source中
    strcpy(this->data_source, data_source_);

}

FFmpegPlayer::~FFmpegPlayer() {
    if (data_source) {
        delete data_source;
        data_source = 0;
    }
}


void FFmpegPlayer::player_prepare() {
    //1.分配一个影音格式上下文。
    avContext = avformat_alloc_context();
    //影音字典
    AVDictionary *avDictionary = 0;
    //字典中设置值
    av_dict_set(&avDictionary, "timeout", "5000000", 0);
    //2.打开一个视频流
    /**
     *1.AVFormatContext
     *2.url:文件输入地址
     *3.fmt:文件封装格式,0表示自动识别
     *4.字典:参数
     */
    int retCode = avformat_open_input(&avContext, this->data_source, 0, &avDictionary);
    //释放已用完字典
    av_dict_free(&avDictionary);
    if (retCode) {
        //如果retcode不等于0返回报错
        //源码提示:@return 0 on success, a negative AVERROR on failure.
        onError(retCode, av_err2str(retCode));
        return;
    }
    retCode = avformat_find_stream_info(avContext, 0);
    if (retCode < 0) {
        //如果返回值小于0返回报错
        //源码提示:@return >=0 if OK, AVERROR_xxx on error
        onError(retCode, av_err2str(retCode));
        return;
    }
    //轮询avFormatContext中的流个数
    for (int stream_index = 0; stream_index < avContext->nb_streams; ++stream_index) {
        //获取avFormatContext中的流
        AVStream *stream = avContext->streams[stream_index];
        //获取流中的解码器参数
        AVCodecParameters *codecParameters = stream->codecpar;
        //获取流解码器
        AVCodec *codec = avcodec_find_decoder(codecParameters->codec_id);
        if (!codec) {
            //如果codec为空返回报错
            //源码提示:@return A decoder if one was found, NULL otherwise.
            const char *errorMsg = "";
            onError(2, (char *) "解码器查找失败");
            return;
        }
        AVCodecContext *avCodecContext = avcodec_alloc_context3(codec);
        if (!avCodecContext) {
            onError(1, "解码器分配内存失败");
            return;
        }
        retCode = avcodec_parameters_to_context(avCodecContext, codecParameters);
        if (retCode < 0) {
            //如果返回值小于0返回报错
            //源码提示:@return >=0 if OK, AVERROR_xxx on error
            onError(retCode, av_err2str(retCode));
            return;
        }
        retCode = avcodec_open2(avCodecContext, codec, 0);
        if (retCode) {
            //如果retcode不等于0返回报错
            //源码提示:@return 0 on success, a negative AVERROR on failure.
            onError(retCode, av_err2str(retCode));
            return;
        }
        /**
       * 10, 从编码器参数中获取流类型 codec_type
       */
        if (codecParameters->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_channel = new AudioChannel(stream_index, avCodecContext, stream->time_base);
        } else if (codecParameters->codec_type == AVMEDIA_TYPE_VIDEO) {
            //            stream->attached_pic;// 封面图像数据
            if (stream->disposition & AV_DISPOSITION_ATTACHED_PIC) {
                //如果这个标记是 附加图
                //过滤当前的封面视频流
                continue;
            }
            //从流中获取平均帧率
            AVRational frame_rate = stream->avg_frame_rate;
            //根据帧率获取到fps
            double fps = av_q2d(frame_rate);
            video_channel = new VideoChannel(stream_index, avCodecContext, stream->time_base);
            video_channel->setFPS(fps);
            video_channel->setRenderCallback(renderCallback);
        }
    }
    if (!audio_channel && !video_channel) {
        LOGE("异常:last");
        onError(0, "编码器获取流失败");
        return;
    }

    if (callback) {
        LOGE("调用Prepare方法");
        callback->prepare(THREAD_CHILD);
    }

}

void FFmpegPlayer::player_start() {
    while (isPlaying) {
        //对视频包队列进行管理,避免packets无限增加导致奔溃
        if (video_channel && video_channel->packets.size() > 100) {
            //如果队列视频包超过100休眠10ms
            av_usleep(10 * 1000);//microseconds
            continue;
        }
        //对音频包队列进行管理,避免packets无限增加导致奔溃
        if (audio_channel && audio_channel->packets.size() > 100) {
            //如果队列视频包超过100休眠10ms
            av_usleep(10 * 1000);//microseconds
            continue;
        }
        AVPacket *packet = av_packet_alloc();
        pthread_mutex_lock(&seek_mutex);
        int ret = av_read_frame(avContext, packet);
        pthread_mutex_unlock(&seek_mutex);
        if (!ret) {
            if (video_channel && video_channel->streamIndex == packet->stream_index) {
                video_channel->packets.push(packet);
            } else if (audio_channel && audio_channel->streamIndex == packet->stream_index) {
                audio_channel->packets.push(packet);
            } else if (ret == AVERROR_EOF) {
                //已经读完了,要考虑是否播放完

            } else {
                break;
            }
        }
    }
    isPlaying = 0;
    video_channel->stop();
    audio_channel->stop();
}

void FFmpegPlayer::prepare() {
    //解封装?用ffmpeg直接解析data_source
    //可以直接解析么?
    //要么是文件
    //要么是直播流
    //都需要耗时,所以需要异步
    pthread_create(&pid_prepare, 0, task_prepare, this);
}


void FFmpegPlayer::start() {
    isPlaying = 1;
    if (video_channel) {
        LOGE("video channel 不为空启动packet转换");
        video_channel->setAudioChannel(audio_channel);
        video_channel->start();
        video_channel->setProgressCallback(progressCallback);
    }
    if (audio_channel) {
        audio_channel->start();
        audio_channel->setProgressCallback(progressCallback);
    }
    //开始播放需要对视频avparket进行解析,属于耗时操作所以放在子线程中
    pthread_create(&pid_start, 0, task_start, this);
}

void FFmpegPlayer::stop() {

}

void FFmpegPlayer::release() {
}


void FFmpegPlayer::onError(jint code, const char *errorMsg) {
    LOGE("准备调用onError方法");
    if (errorCallback) {
        errorCallback->onError(THREAD_CHILD, code, errorMsg);
    }
}

void FFmpegPlayer::setFFmpegCallback(JavaFFmpegCallback *callback) {
    LOGE("设置Prepare监听");
    this->callback = callback;
}

void FFmpegPlayer::setFFmpegErrorCallback(JavaFFmpegErrorCallback *errorCallback) {
    LOGE("设置加载错误监听");
    this->errorCallback = errorCallback;
}

void FFmpegPlayer::setRenderCallback(RenderCallback renderCallback) {
    this->renderCallback = renderCallback;
}

void FFmpegPlayer::onSeek(int duration) {
    if (!avContext) {

        return;
    }
    if (duration < 0 || duration > this->getDuration()) {
        return;
    }
    if (!audio_channel && !video_channel) {
        return;
    }
    LOGE("没有异常%d", duration);
    pthread_mutex_lock(&seek_mutex);
    int ret = av_seek_frame(avContext, -1, duration * AV_TIME_BASE,
                            AVSEEK_FLAG_BACKWARD);
    if (ret < 0) {
        LOGE("拖拽调整进度失败");
        pthread_mutex_unlock(&seek_mutex);
        return;
    }
    if (audio_channel) {
//        avcodec_flush_buffers(audio_channel->codecContext);
        audio_channel->packets.setWorking(0);
        audio_channel->frames.setWorking(0);

        audio_channel->packets.clear();
        audio_channel->frames.clear();

        audio_channel->packets.setWorking(1);
        audio_channel->frames.setWorking(1);
    }

    if (video_channel) {
//        avcodec_flush_buffers(video_channel->codecContext);
        video_channel->packets.setWorking(0);
        video_channel->frames.setWorking(0);

        video_channel->packets.clear();
        video_channel->frames.clear();

        video_channel->packets.setWorking(1);
        video_channel->frames.setWorking(1);
    }

    pthread_mutex_unlock(&seek_mutex);
}

void FFmpegPlayer::setProgressCallback(JavaFFmpegProgressCallback *progressCallback) {
    this->progressCallback = progressCallback;
}

int FFmpegPlayer::getDuration() {
    if (avContext) {
        return avContext->duration / AV_TIME_BASE;//获取到秒
    } else {
        return -1;
    }
}
