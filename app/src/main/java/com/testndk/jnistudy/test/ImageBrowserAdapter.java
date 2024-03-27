//package com.testndk.jnistudy.test;
//
//import android.graphics.Bitmap;
//import android.graphics.PointF;
//import android.graphics.drawable.Drawable;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.view.GestureDetector;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.VideoView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.RequestBuilder;
//import com.bumptech.glide.request.RequestOptions;
//import com.bumptech.glide.request.target.CustomTarget;
//import com.bumptech.glide.request.transition.Transition;
//import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
//import com.chad.library.adapter.base.BaseViewHolder;
//import com.davemorrissey.labs.subscaleview.ImageSource;
//import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
//import com.github.chrisbanes.photoview.PhotoView;
//import com.testndk.jnistudy.R;
//import com.testndk.jnistudy.utils.LogUtils;
//
//import java.util.ArrayList;
//
//public class ImageBrowserAdapter<T extends BasePreBrowserBean> extends BaseMultiItemQuickAdapter<T, BaseViewHolder> {
//
//    public ImageBrowserAdapter() {
//        super(new ArrayList<>());
//        addItemType(BasePreBrowserBean.TYPE_IMG, R.layout.item_browser_img);
//        addItemType(BasePreBrowserBean.TYPE_IMG_GIF, R.layout.item_browser_img_gif);
//        addItemType(BasePreBrowserBean.TYPE_IMG_VIDEO, R.layout.item_browser_video);
//    }
//
//    GestureDetector mGesture;
//
//    @Override
//    protected void convert(BaseViewHolder helper, T item) {
//        switch (helper.getItemViewType()) {
//            case BasePreBrowserBean.TYPE_IMG:
//                SubsamplingScaleImageView2 ssImg = helper.getView(R.id.ssImg);
////                ssImg.req
//                ssImg.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
////                if (mGesture == null) {
////                    mGesture = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
////                        @Override
////                        public boolean onSingleTapConfirmed(MotionEvent e) {
////                            return super.onSingleTapConfirmed(e);
////                        }
////                    });
////                }
////                ssImg.setOnTouchListener(new View.OnTouchListener() {
////                    @Override
////                    public boolean onTouch(View v, MotionEvent event) {
////                        return mGesture.onTouchEvent(event);
////                    }
////                });
////                RequestBuilder<Bitmap> load = Glide.with(mContext).asBitmap().load(item.getBrowserThumbUrl());
//                Glide.with(mContext).asBitmap().load(item.getBrowserUrl()).into(new CustomTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                        LogUtils.eLog(item.getBrowserThumbUrl(), item.getBrowserUrl());
//                        float width = resource.getWidth() * 1f;
//                        float i = 1080 / width;
//                        ssImg.setImage(ImageSource.cachedBitmap(resource));
//                    }
//
//                    @Override
//                    public void onLoadCleared(@Nullable Drawable placeholder) {
//
//                    }
//                });
//                break;
//            case BasePreBrowserBean.TYPE_IMG_GIF:
//                PhotoView2 phtImg = helper.getView(R.id.phtImg);
////                RequestBuilder<Drawable> apply = Glide.with(mContext).load(item.getBrowserThumbUrl()).apply(new RequestOptions());
//                Glide.with(mContext).load(item.getBrowserUrl()).into(phtImg);
//                break;
//            case BasePreBrowserBean.TYPE_IMG_VIDEO:
//                VideoView vdView = helper.getView(R.id.vdView);
//                vdView.setVideoURI(Uri.parse(item.getBrowserUrl()));
////                vdView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
////                    @Override
////                    public void onPrepared(MediaPlayer mp) {
////                        mp.start();
////                    }
////                });
//                break;
//        }
//    }
//}
