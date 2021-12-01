package com.testndk.jnistudy.utils;

import android.util.Log;

public class LogUtils {
    public static void eLog(Object... obj) {
        StringBuilder builder = new StringBuilder();
        for (Object ob : obj) {
            builder.append(ob).append("    ");
        }
        Log.e("error_log", builder.toString());
    }

    public static void d(Object... obj) {
        StringBuilder builder = new StringBuilder();
        for (Object ob : obj) {
            builder.append(ob).append("    ");
        }
        Log.d("error_log", builder.toString());
    }
}
