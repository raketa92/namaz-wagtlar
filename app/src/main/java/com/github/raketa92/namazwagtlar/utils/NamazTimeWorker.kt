package com.github.raketa92.namazwagtlar.utils

import android.content.Context
import android.util.Log
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.InternalCoroutinesApi
import java.util.*
import java.util.concurrent.ExecutionException

class NamazTimeWorker(
    val context: Context,
    workerParameters: WorkerParameters,
) : Worker(context, workerParameters) {

    @InternalCoroutinesApi
    override fun doWork(): Result {
        Log.d(TAG, "Worker started")
        val title = inputData.getString("title")
        val time = inputData.getString("time")
        NotificationHelper(context).createNotification(title!!, "Next namaz starts at $time")
        return Result.success()
    }

    companion object {
        private const val TAG = "NamazTimeWorker"
        fun isWorkScheduled(tag: String, context: Context): Boolean {
            val instance = WorkManager.getInstance(context)
            val statuses = instance.getWorkInfosByTag(tag)
            var running = false
            var workInfoList = Collections.emptyList<WorkInfo>()

            try {
                workInfoList = statuses.get()
            } catch (e: ExecutionException) {
                Log.d(TAG, "ExecutionException in isWorkScheduled: $e")
            } catch (e: InterruptedException) {
                Log.d(TAG, "InterruptedException in isWorkScheduled: $e")
            }

            for (workInfo in workInfoList) {
                val state = workInfo.state
                Log.d(TAG, "States: $state")
                if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED)
                    running = true
//                running = (state == WorkInfo.State.RUNNING) or (state == WorkInfo.State.ENQUEUED)
            }
            Log.d(TAG, "IsScheduled: $running")
            return running
        }
    }

}