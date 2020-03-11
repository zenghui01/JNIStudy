package com.testndk.jnistudy.ui.bsdiff;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.testndk.jnistudy.utils.ExpandKt;
import com.testndk.jnistudy.utils.RxSchedulers;

import java.io.File;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.disposables.Disposable;

public class BsdiffUtilsJava {
    static {
        System.loadLibrary("bzip_lib");
    }

    public static void mergeApk(Context mContext) {
        Disposable disposable = Flowable.create((FlowableOnSubscribe<File>) emitter -> {
            String oldApk = mContext.getApplicationInfo().sourceDir;
            File newApk = new File(Environment.getExternalStorageDirectory(), "newApk.apk");
            ExpandKt.loge("创建成功", "ssss");
            if (!newApk.exists()) {
                boolean newFile = newApk.createNewFile();
                ExpandKt.loge("文件不存在");
                if (newFile) {
                    ExpandKt.loge("创建成功", newFile);
                }
            } else {
                boolean delete = newApk.delete();
                if (delete) {
                    newApk.createNewFile();
                }
            }
            File bsdiffFile = new File(Environment.getExternalStorageDirectory(), "path.diff");
            if (!bsdiffFile.exists()) {
                ExpandKt.toast("增量文件不存在");
                ExpandKt.loge(bsdiffFile.getAbsolutePath());
            } else {
                mergeApkNative(oldApk, newApk.getAbsolutePath(), bsdiffFile.getAbsolutePath());
                ExpandKt.loge("合并结束");
            }
            emitter.onNext(newApk);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER).compose(RxSchedulers.ioToMain()).subscribe(file -> {
            ExpandKt.loge(file.getAbsolutePath());
        });
    }

    public static native void mergeApkNative(String oldApk, String newApk, String path);
}
