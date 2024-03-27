package com.testndk.jnistudy.ui.activity;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.testndk.jnistudy.R;
import com.testndk.jnistudy.ui.BaseActivity;
import com.testndk.jnistudy.ui.cast.SocketLiveClient;
import com.testndk.jnistudy.ui.cast.SocketLiveService;
import com.testndk.jnistudy.utils.KeyboardUtil;
import com.testndk.jnistudy.utils.LogUtils;

import java.nio.ByteBuffer;

public class CastClientActivity extends BaseActivity implements SocketLiveClient.SocketCallback, SurfaceHolder.Callback {
   FrameLayout flParent;
   FrameLayout flParents;
   LinearLayout llParent;
   int width = 720;
   int height = 1280;

   private MediaCodec mediaCodec;
   private SocketLiveClient liveClient;
   private Surface mSurface;
   private TextView tvNotice;

   private SocketLiveService.PushType mPushType = null;

   @Override
   public int initLayout() {
       return R.layout.layout_cast_client;
   }

   @Override
   public void initView() {
       super.initView();
       llParent = findViewById(R.id.llParent);
       flParent = findViewById(R.id.flParent);
       flParents = findViewById(R.id.flParents);
       tvNotice = findViewById(R.id.tvNotice);
       EditText edtIp = findViewById(R.id.edtIp);
       Button btnLink = findViewById(R.id.btnLink);
       btnLink.setOnClickListener(v -> {
           flParents.setVisibility(View.VISIBLE);
           llParent.setVisibility(View.GONE);
           KeyboardUtil.hideKeyboard(edtIp);
           initSocket(edtIp.getText().toString());
       });

   }

   private void initDecoder() {
       try {
           String mediaType;
           if (mPushType == SocketLiveService.PushType.H264) {
               mediaType = MediaFormat.MIMETYPE_VIDEO_AVC;
           } else {
               mediaType = MediaFormat.MIMETYPE_VIDEO_HEVC;
           }
           appendNotice("创建MediaFormat");
           MediaFormat mediaFormat = MediaFormat.createVideoFormat(mediaType, width, height);

           mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

           mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height);

           mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
           //设置i帧间隔
           mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
           //创建h265编码器
           mediaCodec = MediaCodec.createDecoderByType(mediaType);
           //设置编码器参数
           mediaCodec.configure(mediaFormat, mSurface, null, 0);

           mediaCodec.start();
           canDecode = true;
           LogUtils.eLog("编码器设置成功");
       } catch (Exception e) {
           e.printStackTrace();
           LogUtils.eLog("编码器参数报错");
       }
   }

   private void initSocket(String path) {
       liveClient = new SocketLiveClient(path, this);
       liveClient.setListener(msg -> {
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   if (msg.contains("onOpen")) {
                       LogUtils.eLog("创建预览");
                       SurfaceView surfaceView = new SurfaceView(CastClientActivity.this);
                       flParent.addView(surfaceView);
                       SurfaceHolder surfaceHolder = surfaceView.getHolder();
                       if (surfaceHolder != null) {
                           surfaceHolder.removeCallback(CastClientActivity.this);
                           surfaceHolder.addCallback(CastClientActivity.this);
                       }
                   } else if (msg.contains("onClose")) {
                       flParents.setVisibility(View.GONE);
                       llParent.setVisibility(View.VISIBLE);
                   } else if (msg.contains("onError")) {
                       LogUtils.eLog("过程", msg);
                   }
               }
           });
       });
       liveClient.start();
   }

   int NAL_SPS = 7;
   int NAL_VPS = 32;
   private boolean canDecode = false;

   @Override
   public void callBack(byte[] data) {
       int index = 4; // 00 00 00 01
       if (data[2] == 0X1) { // 00 00 01
           index = 3;
       }
       int i = data[index] & 0x1F;
       //移除视频层id,避免被影响
       int type = (data[4] & 0x7E) >> 1;
       if (i == NAL_SPS) {
           if (mPushType != SocketLiveService.PushType.H264) {
               mPushType = SocketLiveService.PushType.H264;
               if (mediaCodec != null) {
                   canDecode = false;
                   mediaCodec.flush();
                   mediaCodec.reset();
                   appendNotice("释放原有解码器");
               }
               appendNotice("重新初始化H264解码器");
               initDecoder();
           }
           decodeData(data);
       } else if (type == NAL_VPS) {
           if (mPushType != SocketLiveService.PushType.H265) {
               mPushType = SocketLiveService.PushType.H265;
               if (mediaCodec != null) {
                   canDecode = false;
                   mediaCodec.flush();
                   mediaCodec.reset();
                   appendNotice("释放原有解码器");
               }
               appendNotice("重新初始化H265解码器");
               initDecoder();
           }
           decodeData(data);
       } else {
           decodeData(data);
       }
   }

   public void appendNotice(String msg) {
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
               tvNotice.append(msg);
               tvNotice.append("\n");
           }
       });
   }

   private void decodeData(byte[] data) {
       if (mediaCodec == null || !canDecode) {
           return;
       }
       int index = mediaCodec.dequeueInputBuffer(100000);
       if (index >= 0) {
           ByteBuffer inputBuffer = mediaCodec.getInputBuffer(index);
           inputBuffer.clear();
           inputBuffer.put(data, 0, data.length);
           mediaCodec.queueInputBuffer(index, 0, data.length, System.currentTimeMillis(), 0);
       }

       MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
       //获取输出
       int outBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
       while (outBufferIndex >= 0) {
           mediaCodec.releaseOutputBuffer(outBufferIndex, true);
           outBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
       }
   }


   @Override
   public void surfaceCreated(SurfaceHolder holder) {
       LogUtils.eLog("创建预览回调");
       mSurface = holder.getSurface();
   }

   @Override
   public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

   }

   @Override
   public void surfaceDestroyed(SurfaceHolder holder) {

   }
}
