package com.testndk.jnistudy.ui.fmod;

import android.content.Context;

import org.fmod.FMOD;

public class FmodUtils {

    public static final int MODEL_NORMAL = 0;
    public static final int MODEL_LUOLI = 1;
    public static final int MODEL_DASHU = 2;
    public static final int MODEL_JINGSONG = 3;
    public static final int MODEL_GAOGUAI = 4;
    public static final int MODEL_KONGLING = 5;

    static {
        System.loadLibrary("");
    }

    public static void init(Context context) {
        FMOD.init(context);
    }

    public void playVoice(int model, String path) {
        if (FMOD.checkInit()) {
            nativePlayVoice(model, path);
        } else {

        }
    }

    public native void nativePlayVoice(int model, String path);

    public static void close() {
        FMOD.close();
    }
}
