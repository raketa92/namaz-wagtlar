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
import androidx.preference.PreferenceManager
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
    private val PREF_REMINDER = "enable_reminder"
    private val PREF_ONE_MIN_REMINDER = "enable_one_min_reminder"
    private val PREF_CUSTOM_REMINDER = "custom_reminder"

    private var is_reminder_enabled: Boolean? = null
    private var is_one_min_reminder_enabled: Boolean? = null
    private var custom_reminder_value: String? = null

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

//        val pref_one_time_work = getSharedPreferences(PREF_ONE_TIME_WORK, MODE_PRIVATE)
//        val isOneTimeWorkStarted = pref_one_time_work?.getBoolean(PREF_ONE_TIME_WORK, false)
//        is_reminder_enabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_REMINDER, false)
//        is_one_min_reminder_enabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_ONE_MIN_REMINDER, false)
//        custom_reminder_value = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_CUSTOM_REMINDER, "10")
//
//        if (!isOneTimeWorkStarted!! and is_reminder_enabled!!) startOneTimeWork()
//
//        if (!RemindWorker.isWorkScheduled("daily_time_scheduler", this) and is_reminder_enabled!!)
//            startSchedulerWorker()
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
        val diffMidNight = cal + 20 // start daily worker after midnight + 20 mins
//        val diffMidNight = Calendar.getInstance().get(Calendar.SECOND) + 5

        Log.d("RemindWorkerMain:", "diffMidNight: $diffMidNight")

        val dailyRequest = PeriodicWorkRequest.Builder(RemindWorker::class.java, 1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag("daily_time_scheduler")
            .setInitialDelay(diffMidNight.toLong(), TimeUnit.MINUTES)
//            .setInitialDelay(diffMidNight.toLong(), TimeUnit.SECONDS)
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

//        val now = Calendar.getInstance().get(Calendar.SECOND)
//        val diff = (Calendar.getInstance().get(Calendar.SECOND) + 5) - now // starts one time work after 5 secs
        val diff = Calendar.getInstance().get(Calendar.SECOND) + 5 // starts one time work after 5 secs

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