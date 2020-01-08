package com.testndk.jnistudy.ui.andfix;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class DexManager {
    private static final DexManager ourInstance = new DexManager();

    public static DexManager getInstance() {
        return ourInstance;
    }

    private DexManager() {
    }

    private Context mContext;

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void loadDex(File dex) {
        try {
            /**
             * loadDex(dex文件,输出目录,模式)
             */
            Enumeration<URL> systemResources = DexClassLoader.getSystemResources(dex.getAbsolutePath());
            DexFile dexFile = DexFile.loadDex(dex.getAbsolutePath(), new File(mContext.getCacheDir(), "opt").getAbsolutePath(), Context.MODE_PRIVATE);
            //获取到类名集合
            Enumeration<String> entries = dexFile.entries();
            //判断是否是最后一个元素
            while (entries.hasMoreElements()) {
                //类名
                String className = entries.nextElement();
                /**
                 * 为什么此时不用反射(Class.forName) ? 反射不适用的情况 ?
                 *
                 * 反射只能加载安装了App的Class,而外部sdcard是不能用反射加载.不适用于加载dex文件的时候.
                 */
//                Class<?> aClass = Class.forName(className);
                Class aClass = dexFile.loadClass(className, mContext.getClassLoader());
                if (null != aClass) {
                    fixClass(aClass);
                } else {
                    Toast.makeText(mContext, "未找到", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void fixClass(Class fixClass) {
        Method[] methods = fixClass.getMethods();
        for (Method fixMethod : methods) {
            Annotation[] annotation = fixMethod.getDeclaredAnnotations();
            if (annotation == null) {
                continue;
            }
            for (int i = 0; i < annotation.length; i++) {
                Log.e("sssss", annotation[i].annotationType().getName());
                if (annotation[i] instanceof MethodReplace) {
                    MethodReplace replace = (MethodReplace) annotation[i];
                    String targetFixClassName = replace.className();
                    String targetFixMethodName = replace.methodName();
                    if (TextUtils.isEmpty(targetFixClassName) || TextUtils.isEmpty(targetFixMethodName)) {
                        Log.e("sssss", "222");
                        return;
                    }
                    try {
                        Class<?> bugClass = Class.forName(targetFixClassName);
                        Method bugMethod = bugClass.getMethod(targetFixMethodName, fixMethod.getParameterTypes());
                        //替换artMethod
                        replace(fixMethod, bugMethod);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private native void replace(Method fixMethod, Method bugMethod);
}
