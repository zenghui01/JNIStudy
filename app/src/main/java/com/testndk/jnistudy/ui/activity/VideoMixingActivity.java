//package com.testndk.jnistudy.ui.activity;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.text.TextUtils;
//import android.view.SurfaceView;
//import android.view.View;
//import android.widget.SeekBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//
//import com.testndk.jnistudy.R;
//import com.testndk.jnistudy.callback.OnProgressListener;
//import com.testndk.jnistudy.listener.CustomSeekBarChangeListener;
//import com.testndk.jnistudy.ui.composition.VideoComposition;
//import com.testndk.jnistudy.utils.ExpandKt;
//import com.testndk.jnistudy.utils.UriUtil;
//
//import java.io.File;
//
//public class VideoMixingActivity extends BaseActivity {
//    TextView tvVideo;
//    TextView tvMusic;
//    TextView tvSeek;
//    TextView tvMSeek;
//    TextView tvNotice;
//    SeekBar seekVideo;
//    SeekBar seekMusic;
//
//    private final static int REQUEST_CODE_SELECT = 112;
//    public static final int IMPORT_REQUEST_CODE = 10005;
//    VideoComposition composition;
//    private String mVideoPath;
//    private String mAudioPath;
//
//    @Override
//    public int initLayout() {
//        return R.layout.activity_video_mixing;
//    }
//
//    @Override
//    public void initView() {
//        super.initView();
//        composition = new VideoComposition();
//        tvVideo = findViewById(R.id.tvVideo);
//        tvMusic = findViewById(R.id.tvMusic);
//        tvSeek = findViewById(R.id.tvSeek);
//        tvMSeek = findViewById(R.id.tvMSeek);
//        tvNotice = findViewById(R.id.tvNotice);
//        seekVideo = findViewById(R.id.seekVideo);
//        seekMusic = findViewById(R.id.seekMusic);
//        seekVideo.setOnSeekBarChangeListener(new CustomSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                super.onProgressChanged(seekBar, progress, fromUser);
//                tvSeek.setText("" + progress);
//            }
//        });
//        seekMusic.setOnSeekBarChangeListener(new CustomSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                super.onProgressChanged(seekBar, progress, fromUser);
//                tvMSeek.setText("" + progress);
//            }
//        });
//        findViewById(R.id.btnSelect).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//                i.setType("video/*");//设置类型，我这里是任意类型，可以过滤文件类型
//                startActivityForResult(i, REQUEST_CODE_SELECT);
//            }
//        });
//        findViewById(R.id.btnMusic).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");//设置类型，我这里是任意类型，可以过滤文件类型
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent, IMPORT_REQUEST_CODE);
//            }
//        });
//        findViewById(R.id.btMixing).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (TextUtils.isEmpty(mAudioPath) || TextUtils.isEmpty(mAudioPath)) {
//                    ExpandKt.toast("路径异常");
//                    return;
//                }
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
////                            final String videoPath  = new File(Environment.getExternalStorageDirectory(), "input2.mp4").getAbsolutePath();
//                            final String outPathPcm = new File(Environment.getExternalStorageDirectory(), "outPut.mp3").getAbsolutePath();
//                            composition.mixAudioTrack(VideoMixingActivity.this, mVideoPath, mAudioPath,
//                                    outPathPcm, 0, 30 * 1000 * 1000, 100, 100);
//                        } catch (Exception e) {
//                            ExpandKt.toast("合成失败");
//                        }
//                    }
//                }).start();
//            }
//        });
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (data == null) {
//            return;
//        }
//        if (requestCode == REQUEST_CODE_SELECT) {
//            Uri data1 = data.getData();
//            String path = UriUtil.getPath(this, data1);
//            tvVideo.setText(path);
//            mVideoPath = path;
//        } else if (requestCode == IMPORT_REQUEST_CODE) {
//            Uri data1 = data.getData();
//            String path = UriUtil.getPath(this, data1);
//            tvMusic.setText(path);
//            mAudioPath = path;
//        }
//    }
//
//    private void appendTxt(String txt) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                tvNotice.append(txt);
//                tvNotice.append("\n");
//            }
//        });
//    }
//}
