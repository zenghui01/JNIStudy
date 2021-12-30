package com.testndk.jnistudy.ui.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

/**
 * worker id
 */
const val TEST_WORKER_KEY_ID = "test_worker_key_id"

/**
 * worker id
 */
const val TEST_WORKER_KEY_CONTENT = "test_worker_key_content"

/**
 * 默认 work id
 */
private const val DEFAULT_WORKER_ID = 10086

const val TAG = "BaseWorker"


abstract class BaseWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val workerId = inputData.getInt(TEST_WORKER_KEY_ID, DEFAULT_WORKER_ID)
        val workContent = inputData.getString(TEST_WORKER_KEY_CONTENT)
        val result = applyFilter(workerId, workContent)
        Log.d(TAG, "workContent $result \n")
        return Result.success(workDataOf(TEST_WORKER_KEY_CONTENT to result))
    }

    private suspend fun applyFilter(workerId: Int, string: String?): String {
        return "$string ${executeTask(workerId)} ${Thread.currentThread().name}"
    }

    abstract suspend fun executeTask(workerId: Int): String
}