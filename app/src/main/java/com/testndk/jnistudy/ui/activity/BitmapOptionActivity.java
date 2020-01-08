package com.testndk.jnistudy.ui.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import com.testndk.jnistudy.R;

public class BitmapOptionActivity extends BaseActivity {

    @Override
    public int initLayout() {
        return R.layout.activity_bitmap_option;
    }

    @Override
    public void initView() {
        ImageView viewById = findViewById(R.id.ivBitmap);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDensity = 2160;
        options.inScaled = true;
        options.inTargetDensity = 1080;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_img, options);

        viewById.setImageBitmap(bitmap);
    }
}
