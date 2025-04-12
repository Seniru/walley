package com.seniru.walley

import WalleyNotificationManager
import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.seniru.walley.persistence.SharedMemory

class Reminder : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Reminder", "onReceive")
        val prefs = SharedMemory.getInstance(context)
        if (prefs.getIsDailyReminderEnabled()) {
            WalleyNotificationManager.createNotification(
                context,
                intent,
                "Daily reminder",
                "You forgot to log today's transactions"
            )
        } else {
            Log.i("Reminder", "Daily reminder preferences turned off")
        }
        schedule(context)
    }

    companion object {

        @RequiresApi(Build.VERSION_CODES.S)
        fun requestRequiredPermissions(context: Context) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
        }

        @RequiresApi(Build.VERSION_CODES.S)
        fun checkPermissions(context: Context): Boolean {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }

        @RequiresApi(Build.VERSION_CODES.S)
        fun schedule(context: Context) {
            Log.i("Reminder", "schedule")
            val intent = Intent(context, Reminder::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerTime = System.currentTimeMillis() + 60 * 60 * 24 * 1000

            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                Log.w("Reminder", "Cannot schedule exact alarms")
               requestRequiredPermissions(context)
            }
        }

    }
}