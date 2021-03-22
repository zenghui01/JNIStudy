package com.testndk.jnistudy.ui.cast;

import com.testndk.jnistudy.callback.OnProgressListener;
import com.testndk.jnistudy.utils.ExpandKt;
import com.testndk.jnistudy.utils.LogUtils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class SocketLiveClient {
    private SocketCallback socketCallback;
    MyWebSocketClient myWebSocketClient;
    private String path;
    private OnProgressListener listener;

    public void setListener(OnProgressListener listener) {
        this.listener = listener;
    }

    public SocketLiveClient(String path, SocketCallback socketCallback) {
        this.socketCallback = socketCallback;
        this.path = path;
    }

    public void start() {
        LogUtils.eLog("start");
        try {
            URI url = new URI("ws://" + path + ":13001");
            myWebSocketClient = new MyWebSocketClient(url);
            myWebSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
            ExpandKt.toast(e.getMessage());
        }
    }


    private class MyWebSocketClient extends WebSocketClient {

        public MyWebSocketClient(URI serverURI) {
            super(serverURI);
            LogUtils.eLog("MyWebSocketClient");
        }

        @Override
        public void connect() {
            super.connect();
            onProgress("Socket connect");
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            onProgress("Socket onOpen");
        }

        @Override
        public void onMessage(String s) {
            onProgress("Socket onMessage:" + s);
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            onProgress("Socket onMessage bytes " + bytes.remaining());
            byte[] buf = new byte[bytes.remaining()];
            bytes.get(buf);
            socketCallback.callBack(buf);
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            onProgress("Socket onClose " + s);
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
            onProgress("Socket onError " + e.getMessage());
        }
    }

    public interface SocketCallback {
        void callBack(byte[] data);
    }

    private void onProgress(String msg) {
        if (listener == null) {
            return;
        }
        listener.onProgress(msg + "\n");
    }
}
