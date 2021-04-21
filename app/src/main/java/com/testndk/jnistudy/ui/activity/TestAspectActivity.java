package com.testndk.jnistudy.ui.activity;

import android.Manifest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.testndk.jnistudy.R;
import com.testndk.jnistudy.aspect.Permission;
import com.testndk.jnistudy.aspect.PermissionCancel;
import com.testndk.jnistudy.aspect.PermissionDenied;
import com.testndk.jnistudy.utils.LogUtils;

import java.util.ArrayList;

public class TestAspectActivity extends BaseActivity {


    @Override
    public int initLayout() {

        return R.layout.activity_aspect;
    }

    @Override
    public void initView() {
        super.initView();
//        ssImg = findViewById(R.id.ssImg);
//        ssImg.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
    }

    int a = 11;
    private static Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public void onTestHandler(View view) {
        LogUtils.eLog("点击了");
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.eLog("跑了");
//                Looper.prepare();
                LogUtils.eLog(Thread.currentThread());
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        LogUtils.eLog("handleMessage", msg, a = msg.arg1);
                    }

                    @Override
                    public void dispatchMessage(Message msg) {
                        super.dispatchMessage(msg);
                        LogUtils.eLog("11:", msg, a = msg.arg1);
                    }
                };
                LogUtils.eLog("Handler");

                LogUtils.eLog("Looper");
                Message message = new Message();
                message.arg1 = 1;
                message.arg2 = 2;
                message.obj = "你好";
                handler.sendMessage(message);
                LogUtils.eLog("发送结束了");
//                Looper.loop();
            }
        }).start();
    }

    @Permission(value = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
            , requestCode = 200)
    public void onClickAspect(View view) {
        testRequest();
    }


    @Permission(value = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
            , requestCode = 200)
    public void testRequest() {
        Toast.makeText(this, "权限申请成功...", Toast.LENGTH_SHORT).show();
    }

    @PermissionCancel
    public void testCancel() {
        Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied
    public void testDenied() {
        Toast.makeText(this, "权限被拒绝（用户勾选了，不再提醒）", Toast.LENGTH_SHORT).show();
    }

}
