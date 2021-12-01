package com.testndk.jnistudy.ui.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.testndk.jnistudy.R;
import com.testndk.jnistudy.ui.activity.BaseActivity;
import com.testndk.jnistudy.ui.gif.GIfNativeDecoder;
import com.testndk.jnistudy.utils.ExpandKt;

import java.io.File;
import java.io.InputStream;

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
        Glide.with(this).download(
                "https://n.sinaimg.cn/tech/transform/481/w221h260/20200902/a072-iypetiv5742891.gif"
        ).into(new CustomTarget<File>() {
            @Override
            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                new NativeTask().execute(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    class NativeTask extends AsyncTask<File, File, String> {
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
        protected String doInBackground(File... files) {
            File file = files[0];
            if (!file.exists()) {
                return "";
            }
            gIfNativeDecoder = GIfNativeDecoder.loadFile(file.getPath());
            int width = gIfNativeDecoder.getWidth(gIfNativeDecoder.getGifPoint());
            int height = gIfNativeDecoder.getHeight(gIfNativeDecoder.getGifPoint());
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            return file.getPath();
        }


        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            if (TextUtils.isEmpty(aVoid)) {
                ExpandKt.toast("图片加载失败");
                return;
            }
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
