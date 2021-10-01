package com.github.raketa92.namazwagtlar.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.raketa92.namazwagtlar.MainActivity
import com.github.raketa92.namazwagtlar.R

class AlarmReciever: BroadcastReceiver() {
    companion object {
        private val CHANNEL_ID = "namazwagtlar"
        private val NOTIFICATION_ID = 1
        val REQUEST_CODE = 0
        private val CHANNEL_NAME = "namazwagtlar_channel"
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        createNotificationChannel(context)
        notifyNotification(context, intent)
    }

    private fun notifyNotification(context: Context?, intent: Intent?) {
        val destinationIntent = Intent(context, MainActivity::class.java)
        intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE, destinationIntent, 0)

        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Namaz wagtlar")
            .setContentText("Next namaz starts in 10 minutes")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(NOTIFICATION_ID, builder)
    }

    private fun createNotificationChannel(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            if (context != null) {
                NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
            }
        }
    }
}