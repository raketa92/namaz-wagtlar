package com.github.raketa92.namazwagtlar

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.github.raketa92.namazwagtlar.utils.RemindWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.InternalCoroutinesApi
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var calendarForAlarm: Calendar

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

    private fun setCalendar() {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val seconds = now.get(Calendar.SECOND)
        Log.d("RemindWorker:", "$hour:$minute:$seconds")

        calendarForAlarm = Calendar.getInstance()
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

        val dailyRequest = PeriodicWorkRequest.Builder(RemindWorker::class.java, 1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag("daily_time_scheduler")
            .setInitialDelay(diff, TimeUnit.MILLISECONDS)
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