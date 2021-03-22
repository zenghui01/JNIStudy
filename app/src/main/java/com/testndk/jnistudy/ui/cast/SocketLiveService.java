package com.testndk.jnistudy.ui.cast;

import android.media.projection.MediaProjection;

import com.testndk.jnistudy.callback.OnProgressListener;
import com.testndk.jnistudy.utils.LogUtils;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class SocketLiveService {
    private WebSocket webSocket;
    private OnProgressListener progressListener;
    CodecLive mCodeLive;
    boolean isStart = false;


    public enum PushType {
        H264, H265
    }

    public void setProgressListener(OnProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public void start(PushType type, MediaProjection mediaProjection) {
        onProgress("Socket服务器:端口 13001");
        if (!isStart) {
            isStart = true;
            webSocketServer.start();
        }
        if (type == PushType.H264) {
            onProgress("创建H264编码器");
            mCodeLive = new CodecLiveH264(this, mediaProjection);
        } else {
            onProgress("创建H265编码器");
            mCodeLive = new CodecLiveH265(this, mediaProjection);
        }
        if (progressListener != null) {
            mCodeLive.listener = progressListener;
        }
        mCodeLive.startLive();
    }

    public void changePushType() {
        if (mCodeLive != null) {
            LogUtils.eLog("停止中");
            mCodeLive.setPlay(false);
        }
    }

    public void close() {
        mCodeLive.setPlay(false);
    }

    public void release() {
        try {
            isStart = false;
            webSocket.close();
            webSocketServer.stop();
            onProgress("关闭socket");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onProgress(String msg) {
        if (progressListener == null) {
            return;
        }
        progressListener.onProgress(msg + "\n");
    }

    private WebSocketServer webSocketServer = new WebSocketServer(new InetSocketAddress(13001)) {
        @Override
        public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
            SocketLiveService.this.webSocket = webSocket;
            onProgress("WebSocket onOpen");
        }

        @Override
        public void onClose(WebSocket webSocket, int i, String s, boolean b) {
            onProgress("WebSocket onClose");
        }

        @Override
        public void onMessage(WebSocket webSocket, String s) {
            onProgress("WebSocket onMessage");
        }

        @Override
        public void onError(WebSocket webSocket, Exception e) {
            onProgress("WebSocket onError," + e.getMessage());
        }

        @Override
        public void onStart() {
            onProgress("WebSocket onStart");
        }
    };

    public void sendData(byte[] bytes) {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.send(bytes);
        }
    }

}
