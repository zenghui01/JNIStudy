package com.testndk.jnistudy.ui.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.testndk.jnistudy.R;
import com.testndk.jnistudy.ui.activity.BaseActivity;
import com.testndk.jnistudy.ui.gif.GIfNativeDecoder;

import java.io.File;

public class GifActivity extends BaseActivity {
    ImageView ivGif;
    Bitmap bitmap;

    @Override
    public int initLayout() {
        return R.layout.activity_gif;
    }

    @Override
    public void initView() {
        super.initView();
        ivGif = findViewById(R.id.ivGif);
    }

    public void loadGif(View view) {
        new NativeTask().execute();
    }

    class NativeTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private GIfNativeDecoder gIfNativeDecoder;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(GifActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("加载中");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            File file = new File(Environment.getExternalStorageDirectory(), "demo.gif");
            if (!file.exists()) {
                Toast.makeText(GifActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
                return null;
            }
            gIfNativeDecoder = GIfNativeDecoder.loadFile(file.getPath());
            int width = gIfNativeDecoder.getWidth(gIfNativeDecoder.getGifPoint());
            int height = gIfNativeDecoder.getHeight(gIfNativeDecoder.getGifPoint());
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            int delay = gIfNativeDecoder.updateFrame(bitmap, gIfNativeDecoder.getGifPoint());
            sendHandler(gIfNativeDecoder, delay);
        }
    }

    public void sendHandler(final GIfNativeDecoder gIfNativeDecoder, int delay) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ivGif.setImageBitmap(bitmap);
                int delay = gIfNativeDecoder.updateFrame(bitmap, gIfNativeDecoder.getGifPoint());
                sendHandler(gIfNativeDecoder, delay);
            }
        }, delay);
    }

    Handler handler = new Handler();
}
