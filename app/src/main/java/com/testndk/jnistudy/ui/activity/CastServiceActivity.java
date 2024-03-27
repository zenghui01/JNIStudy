package com.testndk.jnistudy.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.testndk.jnistudy.R;
import com.testndk.jnistudy.callback.OnProgressListener;
import com.testndk.jnistudy.ui.BaseActivity;
import com.testndk.jnistudy.ui.cast.SocketLiveService;
import com.testndk.jnistudy.utils.ExpandKt;

public class CastServiceActivity extends BaseActivity {
   MediaProjectionManager mediaProjectionManager;

   SocketLiveService socketLive;

   private Handler handler = new Handler();

   TextView tvNotice;
   TextView tvTitle;
   Button btnH264;
   Button btnH265;

   private SocketLiveService.PushType mPushType;
   private MediaProjection mediaProjection;

   @Override
   public int initLayout() {
       return R.layout.layout_cast_service;
   }

   @Override
   public void initView() {
       super.initView();

   }

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       socketLive = new SocketLiveService();
       socketLive.setProgressListener(listener);
       tvNotice = findViewById(R.id.tvNotice);
       tvTitle = findViewById(R.id.tvTitle);
       btnH264 = findViewById(R.id.btnH264);
       btnH265 = findViewById(R.id.btnH265);
       btnH264.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               tvTitle.setText("当前是:264");
               startPlay(SocketLiveService.PushType.H264);
           }
       });
       btnH265.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               tvTitle.setText("当前是:265");
               startPlay(SocketLiveService.PushType.H265);
           }
       });

       tvNotice.append("当前投屏服务器IP:");
       tvNotice.append(getWifiIp());
       tvNotice.append("\n");
   }

   private void startPlay(SocketLiveService.PushType pushType) {
       mPushType = pushType;
       if (mediaProjection == null) {
           checkPermission();
           mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
           Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
           startActivityForResult(captureIntent, 1);
       } else {
           ExpandKt.toast("停止当前录屏");
           socketLive.changePushType();
       }
   }

   private String getWifiIp() {
       WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
       if (wifiManager.isWifiEnabled()
               && wifiManager.getWifiState() == wifiManager.WIFI_STATE_ENABLED) {
           WifiInfo wifiInfo = wifiManager.getConnectionInfo();
           if (wifiInfo != null) {
               int ipAddress = wifiInfo.getIpAddress();
               if (ipAddress == 0)
                   return "";
               return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff)
                       + "." + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
           }
       }
       return "";
   }


   public boolean checkPermission() {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
               Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
           requestPermissions(new String[]{
                   Manifest.permission.READ_EXTERNAL_STORAGE,
                   Manifest.permission.WRITE_EXTERNAL_STORAGE
           }, 1);
       }
       return false;
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode, resultCode, data);
       if (resultCode != RESULT_OK || requestCode != 1) return;
       mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
       if (mediaProjection == null) {
           tvNotice.append("onActivityResult 异常");
           return;
       }
       mediaProjection.registerCallback(callback, handler);
       tvNotice.append("onActivityResult 成功 \n");
       socketLive.start(mPushType, mediaProjection);
   }

   MediaProjection.Callback callback = new MediaProjection.Callback() {
       @Override
       public void onStop() {
           super.onStop();
           ExpandKt.toast("停止咯");
           checkPermission();
           mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
           Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
           startActivityForResult(captureIntent, 1);
       }
   };

   OnProgressListener listener = new OnProgressListener() {
       @Override
       public void onProgress(String msg) {
           handler.post(() -> {
               if (tvNotice == null) {
                   return;
               }
               tvNotice.append(msg);
           });
       }
   };

   @Override
   protected void onDestroy() {
       super.onDestroy();
       socketLive.release();
   }
}
