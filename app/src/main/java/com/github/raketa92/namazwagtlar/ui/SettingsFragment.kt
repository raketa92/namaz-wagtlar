package com.github.raketa92.namazwagtlar.ui

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import androidx.work.*
import com.github.raketa92.namazwagtlar.R
import com.github.raketa92.namazwagtlar.utils.RemindWorker
import kotlinx.coroutines.InternalCoroutinesApi
import java.util.*
import java.util.concurrent.TimeUnit

class SettingsFragment : PreferenceFragmentCompat() {
    private val PREF_ONE_TIME_WORK = "one_time_work"
    private val PREF_ONE_MIN_WORK = "one_min_work"
    private val PREF_CUSTOM_MIN_WORK = "one_custom_work"

    private val ONE_TIME_WORKER_TAG = "one_time_worker"
    private val ONE_TIME_WORKER_ONE_MIN_TAG = "one_time_worker_one_min"
    private val ONE_TIME_WORKER_CUSTOM_MIN_TAG = "one_time_worker_custom_min"

    private val DAILY_WORKER_TAG = "daily_worker"
    private val DAILY_WORKER_ONE_MIN_TAG = "daily_worker_one_min"
    private val DAILY_WORKER_CUSTOM_MIN_TAG = "daily_worker_custom_min"

    private val PREF_DARK_MODE_ENABLE = "enable_dark_mode"
    private val PREF_REMINDER_ENABLE = "enable_reminder"
    private val PREF_CUSTOM_REMINDER_ENABLE = "enable_custom_reminder"
    private val PREF_REMINDER_TIP = "reminder_tip"
    private val PREF_ONE_MIN_REMINDER_ENABLE = "enable_one_min_reminder"
    private val PREF_CUSTOM_REMINDER_VALUE = "custom_reminder"

    private var is_dark_mode_enabled: Boolean? = null
    private var is_reminder_enabled: Boolean? = null
    private var is_one_min_reminder_enabled: Boolean? = null
    private var is_custom_reminder_enabled: Boolean? = null
    private var custom_reminder_value: String? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadPreferences()
        super.onViewCreated(view, savedInstanceState)
    }

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDetach() {
        super.onDetach()
        loadPreferences()
        manageTheme()
        manageWorkers()
    }

    private fun manageTheme() {
        val dark_mode = findPreference<SwitchPreferenceCompat>(PREF_DARK_MODE_ENABLE)
        dark_mode!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue == true) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putBoolean(PREF_DARK_MODE_ENABLE, true).apply()
                } else {
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putBoolean(PREF_DARK_MODE_ENABLE, false).apply()
                }
                true
            }
        if (is_dark_mode_enabled as Boolean) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun showReminderTip() {
        val reminderPreference = findPreference<SwitchPreferenceCompat>(PREF_REMINDER_ENABLE)
        reminderPreference!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                findPreference<Preference>(PREF_REMINDER_TIP)?.isVisible = newValue == true
                if (newValue == false) {
                    findPreference<SwitchPreferenceCompat>(PREF_CUSTOM_REMINDER_ENABLE)?.isChecked = false
                    findPreference<SwitchPreferenceCompat>(PREF_ONE_MIN_REMINDER_ENABLE)?.isChecked = false
                }
                true
            }
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        is_reminder_enabled = sp.getBoolean(PREF_REMINDER_ENABLE, false)
        if (is_reminder_enabled!!) {
            findPreference<Preference>(PREF_REMINDER_TIP)?.isVisible = true
        }
    }

    private fun loadPreferences() {
        resetReminderData()
        showReminderTip()
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        is_dark_mode_enabled = sp.getBoolean(PREF_DARK_MODE_ENABLE, false)
        is_reminder_enabled = sp.getBoolean(PREF_REMINDER_ENABLE, false)
        is_one_min_reminder_enabled = sp.getBoolean(PREF_ONE_MIN_REMINDER_ENABLE, false)
        custom_reminder_value = sp.getString(PREF_CUSTOM_REMINDER_VALUE, "")
    }

    private fun resetReminderData() {
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        is_reminder_enabled = sp.getBoolean(PREF_REMINDER_ENABLE, false)

        if (!is_reminder_enabled!!) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_ONE_MIN_REMINDER_ENABLE, false).apply()
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREF_CUSTOM_REMINDER_ENABLE, false).apply()
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PREF_CUSTOM_REMINDER_VALUE, "10").apply()
        }
    }

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun manageWorkers() {
        resetReminderData()
        is_reminder_enabled = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean(PREF_REMINDER_ENABLE, false)
        is_one_min_reminder_enabled =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean(PREF_ONE_MIN_REMINDER_ENABLE, false)
        is_custom_reminder_enabled =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean(PREF_CUSTOM_REMINDER_ENABLE, false)
        custom_reminder_value = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getString(PREF_CUSTOM_REMINDER_VALUE, "10")
        val customReminder = custom_reminder_value?.toInt()!!
        Log.d("SETTINGS", "is_reminder_enabled: $is_reminder_enabled")
        Log.d("SETTINGS", "is_one_min_reminder_enabled: $is_one_min_reminder_enabled")
        Log.d("SETTINGS", "is_custom_reminder_enabled: $is_custom_reminder_enabled")
        if (!is_reminder_enabled!!) {
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag(ONE_TIME_WORKER_ONE_MIN_TAG)
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag(ONE_TIME_WORKER_CUSTOM_MIN_TAG)
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag(DAILY_WORKER_ONE_MIN_TAG)
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag(DAILY_WORKER_CUSTOM_MIN_TAG)
            Log.d("SETTINGS", "All workers disabled")
            return
        }
        if (!is_one_min_reminder_enabled!!) {
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag(ONE_TIME_WORKER_ONE_MIN_TAG)
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag(DAILY_WORKER_ONE_MIN_TAG)
            Log.d("SETTINGS", "ONE_TIME_WORKER_ONE_MIN_TAG disabled")
            Log.d("SETTINGS", "DAILY_WORKER_ONE_MIN_TAG disabled")
        }
        if (!is_custom_reminder_enabled!!) {
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag(ONE_TIME_WORKER_CUSTOM_MIN_TAG)
            WorkManager.getInstance(requireContext()).cancelAllWorkByTag(DAILY_WORKER_CUSTOM_MIN_TAG)
            Log.d("SETTINGS", "ONE_TIME_WORKER_CUSTOM_MIN_TAG disabled")
            Log.d("SETTINGS", "DAILY_WORKER_CUSTOM_MIN_TAG disabled")
        }

        if (is_reminder_enabled!!) {
            if (is_one_min_reminder_enabled as Boolean) startOneTimeWork(1, ONE_TIME_WORKER_ONE_MIN_TAG)
            if (is_custom_reminder_enabled as Boolean) startOneTimeWork(customReminder, ONE_TIME_WORKER_CUSTOM_MIN_TAG)
        }

        if (is_reminder_enabled!!) {
            if (is_one_min_reminder_enabled as Boolean) startSchedulerWorker(1, DAILY_WORKER_ONE_MIN_TAG)
            if (is_custom_reminder_enabled as Boolean) startSchedulerWorker(customReminder, DAILY_WORKER_CUSTOM_MIN_TAG)
        }

    }

    @InternalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startSchedulerWorker(remindBefore: Int, tag: String) {
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(tag)

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val cal = (24 - Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) * 60
        val diffMidNight = cal + 20 // start daily worker after midnight + 20 mins
//        val diffMidNight = Calendar.getInstance().get(Calendar.SECOND) + 5

        val data = workDataOf("delay" to remindBefore)
        Log.d("RemindWorkerMain:", "diffMidNight: $diffMidNight")

        val dailyRequest = PeriodicWorkRequest.Builder(RemindWorker::class.java, 1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInputData(data)
            .addTag(tag)
            .setInitialDelay(diffMidNight.toLong(), TimeUnit.MINUTES)
//            .setInitialDelay(diffMidNight.toLong(), TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                "daily_time_work",
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyRequest
            )
        Toast.makeText(requireContext(), "Daily reminders set. Delay: $remindBefore", Toast.LENGTH_LONG).show()
    }

    private fun startOneTimeWork(remindBefore: Int, tag: String) {
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(tag)

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

//        val now = Calendar.getInstance().get(Calendar.SECOND)
//        val diff = (Calendar.getInstance().get(Calendar.SECOND) + 5) - now // starts one time work after 5 secs
        val diff =
            Calendar.getInstance().get(Calendar.SECOND) + 5 // starts one time work after 5 secs

        val data = workDataOf("delay" to remindBefore)

        val oneTimeRequest = OneTimeWorkRequestBuilder<RemindWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .addTag(tag)
            .setInitialDelay(diff.toLong(), TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(oneTimeRequest)
        Toast.makeText(requireContext(), "$tag. Delay: $remindBefore", Toast.LENGTH_LONG).show()

        //???
        val pref_one_time_work = context?.getSharedPreferences(
            PREF_ONE_TIME_WORK,
            AppCompatActivity.MODE_PRIVATE
        )
        pref_one_time_work?.edit()?.putBoolean(PREF_ONE_TIME_WORK, true)?.apply()
    }

}