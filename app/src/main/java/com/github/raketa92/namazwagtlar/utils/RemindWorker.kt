package com.github.raketa92.namazwagtlar.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.github.raketa92.namazwagtlar.models.Time
import com.github.raketa92.namazwagtlar.repository.NamazRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.InternalCoroutinesApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

@HiltWorker
class RemindWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters,
    val namazRepo: NamazRepo,
) : Worker(context, workerParameters) {

    @RequiresApi(Build.VERSION_CODES.O)
    @InternalCoroutinesApi
    override fun doWork(): Result {
        Log.d(TAG, "Worker started")
        val delay = inputData.getInt("delay", 15)
        scheduleNotification(delay)
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNotification(delay: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("namaz_times")

        val times = getNextNamazTimes()
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val requests = mutableListOf<WorkRequest>()
//        var delay = 15
        var index = 0
        val now = LocalDateTime.now()
        times.forEach forEach@{ time ->
            val zeros = justifyTimeFormat(time)
            val namazTime =
                LocalDateTime.of(now.year, now.month, now.dayOfMonth, time.hour, time.minute)
            val diff = ChronoUnit.MINUTES.between(now, namazTime)
            val notifTime = diff - delay
            Log.d(TAG, "NOTTIME: $notifTime")
            if (diff < 0) {
                Log.d(TAG, "NOTTIME SKIPPING: $notifTime")
                return@forEach
            }
            val req = OneTimeWorkRequestBuilder<NamazTimeWorker>()
                .setInputData(
                    workDataOf
                        (
                        "title" to time.title,
                        "time" to zeros.first + time.hour.toString() + ":" + zeros.second + time.minute.toString()
                    )
                )
                .setInitialDelay(notifTime, TimeUnit.MINUTES)
                .addTag("namaz_times")
                .setConstraints(constraints)
                .build()
            requests.add(index, req)
            index++
        }

        WorkManager.getInstance(context).enqueue(requests)
    }

    private fun justifyTimeFormat(time: Time): Pair<String, String> {
        var zeroOfHour = ""
        var zeroOfMinute = ""
        if (time.hour.toString().length == 1) zeroOfHour = "0"
        if (time.hour.toString().length == 0) zeroOfHour = "00"
        if (time.minute.toString().length == 1) zeroOfMinute = "0"
        if (time.minute.toString().length == 0) zeroOfMinute = "00"
        return Pair(zeroOfHour, zeroOfMinute)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNextNamazTimes(): List<Time> {
        //TODO: change DB structure: remove years
        val now = LocalDate.now()
        val todayTime = LocalDateTime.of(2021, now.month, now.dayOfMonth, 0, 0, 0,)
        val today = todayTime.toLocalDate()
        val todayTimes = namazRepo.getByDateList(today)
        val todayHours = getTodayHours(todayTimes)

        Log.d(TAG, "today: $today")
        Log.d(TAG, "Times: ${todayTimes}")
        Log.d(TAG, "Today hours: ${todayHours}")

        return todayHours
    }

    companion object {
        private const val TAG = "RemindWorker"

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
                Log.d(TAG, "workInfo: $workInfo")
                if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED)
                    running = true
            }
            Log.d(TAG, "IsScheduled: $running")
            return running
        }
    }
}