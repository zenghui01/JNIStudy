//package com.testndk.jnistudy.ui.work
//
//import android.content.Context
//import android.util.Log
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import kotlinx.coroutines.delay
//
//class BlurWorkFive(appContext: Context, workerParams: WorkerParameters) :
//    CoroutineWorker(appContext, workerParams) {
//
//    override suspend fun doWork(): Result {
//        Log.d("BaseWorker",inputData.getStringArray(TEST_WORKER_KEY_CONTENT).contentToString())
//        return Result.success()
//    }
//}