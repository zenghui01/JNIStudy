package com.testndk.jnistudy.ui.activity

import android.view.View
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.testndk.jnistudy.R
import com.testndk.jnistudy.ui.work.BlurWorkFive
import com.testndk.jnistudy.ui.work.BlurWorkFour
import com.testndk.jnistudy.ui.work.BlurWorkOne
import com.testndk.jnistudy.ui.work.BlurWorkThree
import com.testndk.jnistudy.ui.work.BlurWorkTwo
import com.testndk.jnistudy.ui.work.TEST_WORKER_KEY_CONTENT
import com.testndk.jnistudy.ui.work.TEST_WORKER_KEY_ID


class WorkManagerActivity : BaseActivity() {
    override fun initLayout() = R.layout.activity_workmanager

    /**
     * 向WorkRequest添加约束。
     * 参数：
     * 约束——工作的约束
     * 返回：
     * 当前的WorkRequest.Builder
     */
    fun onClickStartTask(v: View) {
        val data =
            workDataOf(TEST_WORKER_KEY_CONTENT to "hello", TEST_WORKER_KEY_ID to 110)
//        val constraints = Constraints.Builder()
//            // 设备电池是否应处于可接受的水平以使WorkRequest运行
//            .setRequiresBatteryNotLow(true)
//            // 网络连接状态下执行
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//        val request = OneTimeWorkRequest.Builder(BlurWorker::class.java)
//            .setInputData(data)
//            .setConstraints(constraints)
//            .build()
//        WorkManager.getInstance(this).run {
//            enqueue(request)
//            getWorkInfoByIdLiveData(request.id).observe(this@WorkManagerActivity) { workInfo ->
//                when (workInfo.state) {
//
//                }
//            }
//        }

        val reqOne =
            OneTimeWorkRequest.Builder(BlurWorkOne::class.java).setInputData(data).build()
        val reqTwo =
            OneTimeWorkRequest.Builder(BlurWorkTwo::class.java).setInputData(data).build()
        WorkManager.getInstance(this).beginWith(reqOne).then(reqOne).enqueue()

        val reqThree =
            OneTimeWorkRequest.Builder(BlurWorkThree::class.java).setInputData(data)
                .build()
        val reqFour =
            OneTimeWorkRequest.Builder(BlurWorkFour::class.java).setInputData(data)
                .build()
        val reqFive =
            OneTimeWorkRequest.Builder(BlurWorkFive::class.java)
                .build()

        val combineReq = WorkManager.getInstance(this).beginWith(reqOne).then(reqTwo)
        //
        val combineReq2 = WorkManager.getInstance(this).beginWith(reqThree).then(reqFour)
        WorkContinuation.combine(arrayListOf(combineReq, combineReq2)).then(reqFive).enqueue()
    }
}