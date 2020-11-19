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
    pthread_mutex_destroy(&seek_mutex);
    DELETE(data_source)
}


void FFmpegPlayer::player_prepare() {
    //1.分配一个影音格式上下文。
    avContext = avformat_alloc_context();
    //影音字典
    AVDictionary *dictionary = 0;
    //设置超时（5秒）
    av_dict_set(&dictionary, "timeout", "5000000", 0);//单位微秒
    //2.打开一个视频流
    /**
     *1.AVFormatContext
     *2.url:文件输入地址
     *3.fmt:文件封装格式,0表示自动识别
     *4.字典:参数
     */
    int retCode = avformat_open_input(&avContext, data_source, 0, &dictionary);
    //释放已用完字典
    av_dict_free(&dictionary);
    LOGE("打开视频流地址")
    if (retCode) {
        avformat_close_input(&avContext);
        avContext = 0;
        //如果retcode不等于0返回报错
        //源码提示:@return 0 on success, a negative AVERROR on failure.
        onError(retCode, av_err2str(retCode));
        return;
    }
    retCode = avformat_find_stream_info(avContext, 0);
    LOGE("获取流信息")
    if (retCode < 0) {
        //如果返回值小于0返回报错
        //源码提示:@return >=0 if OK, AVERROR_xxx on error
        LOGE("sss 报错了");
        onError(retCode, av_err2str(retCode));
        return;
    }
    LOGE("sssll");
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
        jlong file_duration = avContext->duration / AV_TIME_BASE;
        if (codecParameters->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_channel = new AudioChannel(stream_index, avCodecContext, stream->time_base,
                                             file_duration);
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
            auto *param = new VideoChannel::VideoParam();
            video_channel = new VideoChannel(stream_index, avCodecContext, stream->time_base,
                                             file_duration);
            param->renderCallback = renderCallback;
            param->fps = fps;
            param->frame_delay = codecParameters->video_delay / fps;
            video_channel->setVideoParam(param);
        }
    }
    if (!audio_channel && !video_channel) {
        LOGE("异常:last");
        onError(0, "编码器获取流失败");
        return;
    }

    if (loadSuccessCallback) {
        LOGE("调用Prepare方法");
        loadSuccessCallback->prepare(THREAD_CHILD);
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
        if (getDuration() > 0) {
            hasPlayComplete = 0;
            if (hasReadEnd) {
                //已经读完了,要考虑是否播放完
                int playing = 0;
                if (video_channel) {
                    if (video_channel->isPlaying) {
                        playing = 1;
                    }
                }
                if (audio_channel) {
                    if (audio_channel->isPlaying) {
                        playing = 1;
                    }
                }
                if (!playing) {
                    LOGE("文件全部读取完成");
                    break;
                }
                continue;
            }
        }
        AVPacket *packet = av_packet_alloc();
        pthread_mutex_lock(&seek_mutex);
        int ret = av_read_frame(avContext, packet);
        if (ret == AVERROR_EOF) {
            hasReadEnd = 1;
        }
        pthread_mutex_unlock(&seek_mutex);
        if (!ret) {
            if (video_channel && video_channel->streamIndex == packet->stream_index) {
                video_channel->packets.push(packet);
            } else if (audio_channel && audio_channel->streamIndex == packet->stream_index) {
                audio_channel->packets.push(packet);
            } else {
                break;
            }
        }
    }
    isPlaying = 0;
    if (hasReadEnd) {
        hasPlayComplete = 1;
        //释放视频与音频中的资源
//    video_channel->stop();
//    audio_channel->stop();
        //回调java层完成
        if (completeCallback) {
            completeCallback->onComplete(THREAD_CHILD);
        }
    } else {
        if (completeCallback) {
            completeCallback->onPlayPause(THREAD_CHILD);
        }
    }

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
//    if (!avContext) {
//        onError(-100, "视频加载失败");
//        return;
//    }
    if (isPlaying) {
        return;
    }
    //播放完的话,重新从头开始播放
    if (hasPlayComplete) {
        onSeek(0);
    }
    hasPlayComplete = 0;
    hasReadEnd = 0;
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
    LOGE("准备解析avpackte");
    //开始播放需要对视频avparket进行解析,属于耗时操作所以放在子线程中
    pthread_create(&pid_start, 0, task_start, this);
}

//线程函数必须返回0
void *task_stop(void *args) {
    auto *player = static_cast<FFmpegPlayer *>(args);
    pthread_join(player->pid_prepare, 0);
    pthread_join(player->pid_start, 0);
    //AVFormatContext
    if (player->avContext) {
        avformat_close_input(&player->avContext);
        avformat_free_context(player->avContext);
        player->avContext = 0;
    }
    //音频解码通道
    if (player->audio_channel) {
        player->audio_channel = 0;
    }
    //视频解码通道
    if (player->video_channel) {
        player->video_channel = 0;
    }
    return 0;
}

/**
 * stop逻辑
 * 1.当时视频预加载时,直接stop,反复执行可能导致异常问题出现,所以stop之前最好是等待prepare执行完
 * 2.当时视频播放时,直接stop,反复执行可能导致audio_channel和video_channel未释放,最好是等待start线程执行执行完后进行释放
 * 由于两步都是耗时操作,为了避免anr,放到线程中执行
 */
void FFmpegPlayer::stop() {
    isPlaying = 0;
    if (audio_channel) {
        audio_channel->stop();
    }
    if (video_channel) {
        video_channel->stop();
    }
    pthread_create(&pid_stop, 0, task_stop, this);
    //置空加载成功回调
    if (loadSuccessCallback) {
        DELETE(loadSuccessCallback)
    }
    //置空错误回调
    if (errorCallback) {
        DELETE(errorCallback)
    }
    //置空进度回调
    if (progressCallback) {
        DELETE(progressCallback)
    }
    //置空渲染回调
    if (renderCallback) {
        renderCallback = 0;
    }
    //置空完成回调
    if (completeCallback) {
        DELETE(completeCallback)
    }
}

void FFmpegPlayer::release() {
}


void FFmpegPlayer::onError(jint code, const char *errorMsg) {
    LOGE("准备调用onError方法");
    if (errorCallback) {
        errorCallback->onError(THREAD_CHILD, code, errorMsg);
    }
}

void FFmpegPlayer::setFFmpegCallback(JavaCallback *callback) {
    LOGE("设置Prepare监听");
    this->loadSuccessCallback = callback;
}

void FFmpegPlayer::setFFmpegErrorCallback(JavaErrorCallback *errorCallback) {
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
        pthread_mutex_unlock(&seek_mutex);
        onError(ret, "seek error");
        return;
    }
    //重置已经播放结束状态
    hasPlayComplete = 0;
    //重置已经读取完的状态
    hasReadEnd = 0;
    if (audio_channel) {
        audio_channel->packets.setWorking(0);
        audio_channel->frames.setWorking(0);

        audio_channel->packets.clear();
        audio_channel->frames.clear();

        audio_channel->packets.setWorking(1);
        audio_channel->frames.setWorking(1);
    }

    if (video_channel) {
        video_channel->packets.setWorking(0);
        video_channel->frames.setWorking(0);

        video_channel->packets.clear();
        video_channel->frames.clear();

        video_channel->packets.setWorking(1);
        video_channel->frames.setWorking(1);
    }

    pthread_mutex_unlock(&seek_mutex);
}

void FFmpegPlayer::setProgressCallback(JavaProgressCallback *progressCallback) {
    this->progressCallback = progressCallback;
}

int FFmpegPlayer::getDuration() {
    if (avContext) {
        return avContext->duration / AV_TIME_BASE;//获取到秒
    } else {
        return -1;
    }
}

void FFmpegPlayer::setCompleteCallback(JavaCompleteCallback *completeCallback) {
    this->completeCallback = completeCallback;
}

void FFmpegPlayer::pause() {
    isPlaying = 0;
    if (audio_channel) {
        audio_channel->isPlaying = 0;
        audio_channel->packets.setWorking(0);
        audio_channel->frames.setWorking(0);
    }

    if (video_channel) {
        video_channel->isPlaying = 0;
        video_channel->packets.setWorking(0);
        video_channel->frames.setWorking(0);
    }
}
