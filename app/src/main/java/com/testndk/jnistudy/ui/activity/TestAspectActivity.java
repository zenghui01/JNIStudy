package com.testndk.jnistudy.ui.activity;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.testndk.jnistudy.R;
import com.testndk.jnistudy.annotation.TestAnnotation;
import com.testndk.jnistudy.test.ImageBrowserDialog;
import com.testndk.jnistudy.utils.LogUtils;

public class TestAspectActivity extends BaseActivity {
    SubsamplingScaleImageView ssImg;

    @Override
    public int initLayout() {
        return R.layout.activity_aspect;
    }

    @Override
    public void initView() {
        super.initView();
        ssImg = findViewById(R.id.ssImg);
        ssImg.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        Glide.with(TestAspectActivity.this).asBitmap().load("https://cdn.diandi.club/image/159056144500020_i.png").into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                ssImg.setImage(ImageSource.cachedBitmap(resource));
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });

    }

    @TestAnnotation
    public void onClickAspect(View view) {
        LogUtils.eLog("sssss");
        ImageBrowserDialog.newInstance().show(getSupportFragmentManager(), "ss");

    }
}
