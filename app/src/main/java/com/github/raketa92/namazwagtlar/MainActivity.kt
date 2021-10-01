package com.github.raketa92.namazwagtlar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.github.raketa92.namazwagtlar.models.Time
import com.github.raketa92.namazwagtlar.utils.AlarmReciever
import com.github.raketa92.namazwagtlar.utils.RemindWorker
import com.github.raketa92.namazwagtlar.utils.getNextNamazTime
import com.github.raketa92.namazwagtlar.viewmodel.NamazViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var calendarForAlarm: Calendar
    private lateinit var nextNamazTime: Time

    @InternalCoroutinesApi
    private val namazViewModel: NamazViewModel by viewModels()

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        drawerLayout = drawer_layout
        nav_view.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)


        if (!RemindWorker.isWorkScheduled("daily_time_scheduler", this))
            startSchedulerWorker()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNextNamazTimeForToday() {
        val today = LocalDate.parse(LocalDateTime.now().toString())
        val todayTimes = namazViewModel.getByDate(today).value
        nextNamazTime = getNextNamazTime(todayTimes!!)
    }

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAlarm() {
        setNextNamazTimeForToday()
        setCalendar()
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReciever::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, AlarmReciever.REQUEST_CODE, intent, 0)
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendarForAlarm.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendarForAlarm.timeInMillis, pendingIntent)
        Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show()
    }

    private fun setCalendar() {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val seconds = now.get(Calendar.SECOND)
        Log.d("RemindWorker:", "$hour:$minute:$seconds")
        val remindBefore = 10

        calendarForAlarm = Calendar.getInstance()
//        calendarForAlarm.set(Calendar.HOUR_OF_DAY, nextNamazTime.hour)
//        calendarForAlarm.set(Calendar.MINUTE, nextNamazTime.minute - remindBefore)
        calendarForAlarm.set(Calendar.HOUR_OF_DAY, hour)
        calendarForAlarm.set(Calendar.MINUTE, minute)
        calendarForAlarm.set(Calendar.SECOND, seconds + 5)
        calendarForAlarm.set(Calendar.MILLISECOND, 0)
    }

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startSchedulerWorker() {
        setCalendar()
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val nowInMillis = Calendar.getInstance().timeInMillis
        val diff = calendarForAlarm.timeInMillis - nowInMillis

//        val myData = workDataOf("title" to "1 title", "message" to "msg 1")
        val midnight = Calendar.getInstance()
//        midnight.set(Calendar.HOUR_OF_DAY, 0)
//        midnight.set(Calendar.MINUTE, 0)

        val dailyRequest = PeriodicWorkRequest.Builder(RemindWorker::class.java, 1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag("daily_time_scheduler")
            .setInitialDelay(diff, TimeUnit.MILLISECONDS)
//            .setInputData(myData)
//            .setInitialDelay(midnight.timeInMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "daily_time_scheduler",
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyRequest
            )
        Toast.makeText(this, "Reminders set", Toast.LENGTH_LONG).show()
    }

}