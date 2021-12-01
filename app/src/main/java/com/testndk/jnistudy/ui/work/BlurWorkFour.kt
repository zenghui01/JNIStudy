package com.testndk.jnistudy.ui.work

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class BlurWorkFour(appContext: Context, workerParams: WorkerParameters) :
    BaseWorker(appContext, workerParams) {
    override suspend fun executeTask(workerId: Int): String {
        delay(1000)
        return "BlurWorkFour 执行完毕,workerId: $workerId \n"
    }
}