package com.testndk.jnistudy.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

public class ScreenUtil {

    private static int sScreenWidth = 0, sScreenHeight = 0;
    private static float sDensity;
    private static float sScaleDensity;
    private static float sScale;
    private static float sXdpi;
    private static float sYdpi;
    private static int sDensityDpi;

    public static void init(Context application) {
        if (null == application) {
            return;
        }
        DisplayMetrics dm = application.getResources().getDisplayMetrics();
        //横屏进入时取反
        if (dm.widthPixels > dm.heightPixels) {
            sScreenWidth = dm.heightPixels;
            sScreenHeight = dm.widthPixels;
        } else {
            sScreenWidth = dm.widthPixels;
            sScreenHeight = dm.heightPixels;
        }

        sDensity = dm.density;
        sScaleDensity = dm.scaledDensity;
        sXdpi = dm.xdpi;
        sYdpi = dm.ydpi;
        sDensityDpi = dm.densityDpi;

        //以1080*1920为标准，取长宽的最小缩放比
        sScale = Math.min((float) sScreenHeight / 1920, (float) sScreenWidth / 1080);
    }

    public static int getScreenWidth() {
        return sScreenWidth;
    }

    public static int getScreenHeight() {
        return sScreenHeight;
    }

    public static float getScale() {
        return sScale;
    }

    // 根据手机的分辨率将dp的单位转成px(像素)
    public static int dp2px(float dpValue) {
        return (int) (dpValue * sDensity + 0.5f);
    }

    // 根据手机的分辨率将px(像素)的单位转成dp
    public static int px2dp(float pxValue) {
        return (int) (pxValue / sDensity + 0.5f);
    }

    // 将px值转换为sp值
    public static int px2sp(float pxValue) {
        return (int) (pxValue / sScaleDensity + 0.5f);
    }

    // 将sp值转换为px值
    public static int sp2px(float spValue) {
        return (int) (spValue * sScaleDensity + 0.5f);
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = context.getResources().getDimensionPixelSize(x);
        } catch (Exception E) {
            E.printStackTrace();
        }
        return sbar;
    }

    public static int getNavBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * 每英尺像素点
     */
    public static int getDensityDpi() {
        return sDensityDpi;
    }

    /**
     * 9.0缺口屏适配
     */
    public static void notchAdapter(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && activity != null) {
            try {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            } catch (Throwable e) {
            }
        }

    }

    /**
     * 设置屏幕为全屏
     *
     * @param activity activity
     */
    public static void setFullScreen(@NonNull final Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
}
