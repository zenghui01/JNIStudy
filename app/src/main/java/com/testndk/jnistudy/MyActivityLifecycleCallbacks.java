package com.testndk.jnistudy;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private int mActivityCount = 0;

    //是否在前台
    private boolean mIsForeground = false;
    private static ArrayList<Activity> mActivityList = new ArrayList<>();

    private static volatile MyActivityLifecycleCallbacks instance;

    private OnAppStatusChangeListener statusChangeListener;

    public static MyActivityLifecycleCallbacks getInstance() {
        if (instance == null) {
            synchronized (MyActivityLifecycleCallbacks.class) {
                if (instance == null) {
                    instance = new MyActivityLifecycleCallbacks();
                }
            }
        }

        return instance;
    }

    private MyActivityLifecycleCallbacks() {
    }

    public MyActivityLifecycleCallbacks setStatusChangeListener(OnAppStatusChangeListener statusChangeListener) {
        this.statusChangeListener = statusChangeListener;
        return instance;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        synchronized (this) {
            mActivityList.add(activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mActivityCount++;
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        mIsForeground = true;
        if (statusChangeListener != null) {
            statusChangeListener.onStatusChange(mIsForeground);
        }
        //打开APP取消所有通知
        NotificationManager systemService = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        systemService.cancelAll();
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        mActivityCount--;
        if (mActivityCount < 0) {
            mActivityCount = 0;
        }

        if (mActivityCount == 0) {
            mIsForeground = false;
            if (statusChangeListener != null) {
                statusChangeListener.onStatusChange(mIsForeground);
            }
        }
    }

    public boolean getAppIsForeground() {
        return mIsForeground;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        synchronized (this) {
            mActivityList.remove(activity);
        }
    }

    /**
     * finish 所有集合中的除了参数之外的所有Activity
     *
     * @param clazz 需要保留的Activity
     */
    public void clearActivities(Class clazz) {
        synchronized (this) {
            for (Activity activity : mActivityList) {
                if (activity == null) {
                    continue;
                }
                if (clazz != null && TextUtils.equals(clazz.getSimpleName(), activity.getClass().getSimpleName())) {
                    continue;
                }
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        }
    }

    public Activity getTopActivity() {
        if (mActivityList != null && mActivityList.size() > 0) {
            return mActivityList.get(mActivityList.size() - 1);
        }

        return null;
    }

    public ArrayList<Activity> getActivityList() {
        return mActivityList;
    }

    public void killAllProcess(Context context) {
        int myPid = android.os.Process.myPid();
        killProcess(context, myPid);
        android.os.Process.killProcess(myPid);
    }

    private void killProcess(Context context, int myPid) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infoList = null;
        try {
            infoList = am.getRunningAppProcesses();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (infoList == null) {
            return;
        }

        for (ActivityManager.RunningAppProcessInfo info : infoList) {
            //进程的重要程度(越低越重要)
            if (info.pid != myPid) {
                android.os.Process.killProcess(info.pid);
            }
        }
    }

    public interface OnAppStatusChangeListener {
        void onStatusChange(boolean isResume);
    }
}
