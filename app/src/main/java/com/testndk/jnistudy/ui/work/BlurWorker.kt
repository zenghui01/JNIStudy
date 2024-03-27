package com.testndk.jnistudy.ui.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

/**
* 默认 work id
*/
private const val DEFAULT_WORKER_ID = 10086

class BlurWorker(appContext: Context, workerParams: WorkerParameters) :
   CoroutineWorker(appContext, workerParams) {
   override suspend fun doWork(): Result {
       fakeBlur(inputData.getInt(TEST_WORKER_KEY_ID, DEFAULT_WORKER_ID))
       Result.failure()
       Result.retry()
       return Result.success()
   }

   private suspend fun fakeBlur(workId: Int) {
       Log.d(TAG, "开始执行，任务 ID: $workId")
       delay(1000)
       Log.d(TAG, "fakeBlur: 成功")
   }

}
