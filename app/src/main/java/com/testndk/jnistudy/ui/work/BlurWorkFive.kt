package com.testndk.jnistudy.ui.work

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class BlurWorkFive(appContext: Context, workerParams: WorkerParameters) :
    BaseWorker(appContext, workerParams) {

    override suspend fun executeTask(workerId: Int): String {
        delay(1000)
        return "BlurWorkFive 执行完毕,workerId: $workerId \n"
    }
}