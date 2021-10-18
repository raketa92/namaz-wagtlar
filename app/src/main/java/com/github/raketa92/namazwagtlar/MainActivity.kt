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
    private val PREF_ONE_TIME_WORK = "one_time_work"

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

        val pref_one_time_work = getSharedPreferences(PREF_ONE_TIME_WORK, MODE_PRIVATE)
        val isOneTimeWorkStarted = pref_one_time_work?.getBoolean(PREF_ONE_TIME_WORK, false)
        if (!isOneTimeWorkStarted!!) startOneTimeWork()

        if (!RemindWorker.isWorkScheduled("daily_time_scheduler", this))
            startSchedulerWorker()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startSchedulerWorker() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val cal = (24 - Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) * 60
        val diffMidNight = cal + 20

        Log.d("RemindWorkerMain:", "diffMidNight: $diffMidNight")

        val dailyRequest = PeriodicWorkRequest.Builder(RemindWorker::class.java, 1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag("daily_time_scheduler")
            .setInitialDelay(diffMidNight.toLong(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "daily_time_work",
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyRequest
            )
        Toast.makeText(this, "Daily reminders set", Toast.LENGTH_LONG).show()
    }

    private fun startOneTimeWork() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val now = Calendar.getInstance().get(Calendar.SECOND)
        val diff = (Calendar.getInstance().get(Calendar.SECOND) + 5) - now

        val oneTimeRequest = OneTimeWorkRequestBuilder<RemindWorker>()
            .setConstraints(constraints)
            .addTag("one_time_scheduler")
            .setInitialDelay(diff.toLong(), TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(oneTimeRequest)
        Toast.makeText(this, "One time reminders set", Toast.LENGTH_LONG).show()

        val pref_one_time_work = getSharedPreferences(PREF_ONE_TIME_WORK, MODE_PRIVATE)
        pref_one_time_work.edit().putBoolean(PREF_ONE_TIME_WORK, true).apply()
    }

}