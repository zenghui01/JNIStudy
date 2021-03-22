package com.testndk.jnistudy.ui.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.testndk.jnistudy.R;
import com.testndk.jnistudy.callback.OnProgressListener;
import com.testndk.jnistudy.ui.composition.VideoComposition;
import com.testndk.jnistudy.utils.ExpandKt;
import com.testndk.jnistudy.utils.LogUtils;
import com.testndk.jnistudy.utils.UriUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VideoCompositionActivity extends BaseActivity implements SurfaceHolder.Callback {

    FrameLayout flParent;
    TextView tvNotice;
    private final static int REQUEST_CODE_SELECT = 112;
    private List<String> files = new ArrayList<>();
    VideoComposition composition;
    private File outPath;
    private SurfaceHolder surfaceHolder;

    @Override
    public int initLayout() {
        return R.layout.activity_video_composition;
    }

    @Override
    public void initView() {
        super.initView();
        composition = new VideoComposition();
        flParent = findViewById(R.id.flParent);
        tvNotice = findViewById(R.id.tvNotice);
        findViewById(R.id.btnSelect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_CODE_SELECT);
            }
        });
        outPath = new File(Environment.getExternalStorageDirectory(), "outPath.mp4");
        findViewById(R.id.btnCom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (files.size() <= 0) {
                    ExpandKt.toast("当前视频量不够无法合成");
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            appStr("输出地址:" + outPath.getAbsolutePath());
                            appStr("开始合成");
                            composition.appendVideo(files, outPath.getAbsolutePath(), new OnProgressListener() {
                                @Override
                                public void onProgress(String msg) {
                                    appStr(msg);
                                }
                            });
                            appStr("合成结束");
                            files.clear();
                            if (outPath.exists()) {
                                toPlay();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });
    }

    private void toPlay() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (flParent.getChildCount() > 0) {
                    play(surfaceHolder);
                } else {
                    SurfaceView view = new SurfaceView(VideoCompositionActivity.this);
                    flParent.addView(view);
                    SurfaceHolder surfaceHolder = view.getHolder();
                    if (surfaceHolder != null) {
                        surfaceHolder.removeCallback(VideoCompositionActivity.this);
                        surfaceHolder.addCallback(VideoCompositionActivity.this);
                    }
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT && resultCode == RESULT_OK && null != data) {
            Uri uri = data.getData();
            LogUtils.eLog(uri, uri);
            String path = UriUtil.getPath(this, uri);
            files.add(path);
            appStr(String.format(Locale.CHINA, "视频%d:%s", files.size(), path));
        }
    }

    public void appStr(String str) {
        if (tvNotice != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvNotice.append(str);
                    tvNotice.append("\n");
                }
            });
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
        play(holder);

    }

    private void play(SurfaceHolder holder) {
        if (outPath != null) {
            appStr("输出文件路径:" + outPath.getAbsolutePath());
            if (!outPath.exists()) {
                appStr("文件不存在");
                return;
            }
            appStr("开始播放");
            MediaPlayer.create(this, Uri.parse(outPath.getAbsolutePath()), holder).start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
