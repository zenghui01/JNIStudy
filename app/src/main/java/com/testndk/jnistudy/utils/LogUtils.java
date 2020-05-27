package com.testndk.jnistudy.utils;

import android.util.Log;

public class LogUtils {
    public static void eLog(Object... obj) {
        StringBuilder builder = new StringBuilder();
        for (Object ob : obj) {
            builder.append(ob.toString()).append("    ");
        }
        Log.e("error_log", builder.toString());
    }
}
