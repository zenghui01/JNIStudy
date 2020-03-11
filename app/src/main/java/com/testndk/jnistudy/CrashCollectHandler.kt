package com.testndk.jnistudy

import android.content.Context
import android.os.Looper
import kotlin.system.exitProcess

class CrashCollectHandler : Thread.UncaughtExceptionHandler {
    var mContext: Context? = null
    var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { CrashCollectHandler() }
    }

    fun init(pContext: Context) {
        this.mContext = pContext
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    //当UncaughtException发生时会转入该函数来处理
    override fun uncaughtException(t: Thread?, e: Throwable?) {
//        if (!handleException(e) && mDefaultHandler != null) {
//            //如果用户没有处理则让系统默认的异常处理器来处理
//            mDefaultHandler?.uncaughtException(t, e)
//        } else {
//            //退出程序
//            try {
//                //给Toast留出时间
//                Thread.sleep(2000)
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//            MyActivityLifecycleCallbacks.getInstance().clearActivities(null)
//            android.os.Process.killProcess(android.os.Process.myPid())
//            exitProcess(0)
//        }
        try {
            //给Toast留出时间
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        MyActivityLifecycleCallbacks.getInstance().clearActivities(null)
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)

    }

    fun handleException(ex: Throwable?): Boolean {
        if (ex == null) {
            return false
        }
        Thread {
            Looper.prepare()
            Looper.loop()
        }.start()
        //收集设备参数信息
        //collectDeviceInfo(mContext);
        //保存日志文件
        //saveCrashInfo2File(ex);
        // 注：收集设备信息和保存日志文件的代码就没必要在这里贴出来了
        //文中只是提供思路，并不一定必须收集信息和保存日志
        //因为现在大部分的项目里都集成了第三方的崩溃分析日志，如`Bugly` 或 `啄木鸟等`
        //如果自己定制化处理的话，反而比较麻烦和消耗精力，毕竟开发资源是有限的
        return true
    }
}