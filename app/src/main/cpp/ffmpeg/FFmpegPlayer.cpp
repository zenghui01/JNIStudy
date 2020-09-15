//
// Created by zenghui on 2020/9/9.
//

#include "FFmpegPlayer.h"

void *task_prepare(void *args) {
    auto *player = static_cast<FFmpegPlayer *>(args);
    player->player_prepare();
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

void FFmpegPlayer::prepare() {
    //解封装?用ffmpeg直接解析data_source
    //可以直接解析么?
    //要么是文件
    //要么是直播流
    //都需要耗时,所以需要异步
    pthread_create(&pid_prepare, 0, task_prepare, this);
}


void FFmpegPlayer::player_prepare() {
    //1.分配一个影音格式上下文。
    AVFormatContext *avContext = avformat_alloc_context();
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
    LOGE("sss1");
    if (retCode) {
        //如果retcode不等于0返回报错
        //源码提示:@return 0 on success, a negative AVERROR on failure.
        onError(retCode, av_err2str(retCode));
        return;
    }
    LOGE("sss2");
    retCode = avformat_find_stream_info(avContext, 0);
    if (retCode < 0) {
        //如果返回值小于0返回报错
        //源码提示:@return >=0 if OK, AVERROR_xxx on error
        onError(retCode, av_err2str(retCode));
        return;
    }
    LOGE("sss3");
    //轮询avFormatContext中的流个数
    for (int i = 0; i < avContext->nb_streams; ++i) {
        //获取avFormatContext中的流
        AVStream *stream = avContext->streams[i];
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
            audio_channel = new AudioChannel();
        } else if (codecParameters->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_channel = new VideoChannel();
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

void FFmpegPlayer::onError(jint code, const char *errorMsg) {
    LOGE("准备调用onError方法");
    if (errorCallback) {
        errorCallback->onError(code, errorMsg);
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
