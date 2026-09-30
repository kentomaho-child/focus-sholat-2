package com.focussholat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focussholat.util.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PRAYER_REMINDER = "com.focussholat.ACTION_PRAYER_REMINDER"
        const val ACTION_PRAYER_START = "com.focussholat.ACTION_PRAYER_START"
        const val ACTION_PRAYER_END = "com.focussholat.ACTION_PRAYER_END"
        const val ACTION_EMERGENCY_OVERRIDE_END = "com.focussholat.ACTION_EMERGENCY_OVERRIDE_END"

        const val EXTRA_PRAYER_KEY = "prayer_key"
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_OVERRIDE_ID = "override_id"
        const val EXTRA_PACKAGE_NAME = "package_name"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PRAYER_REMINDER -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Sholat"
                // Read reminder minutes from preferences synchronously here or use a default
                val reminderMinutes = 10 // default, could read from SharedPreferences sync
                NotificationHelper.showPrayerReminderNotification(
                    context, prayerName, reminderMinutes
                )
            }
            ACTION_PRAYER_START -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Sholat"
                NotificationHelper.showPrayerStartNotification(context, prayerName)
            }
            ACTION_EMERGENCY_OVERRIDE_END -> {
                // Override ended - notification cleanup
                NotificationHelper.cancelNotification(
                    context,
                    NotificationHelper.NOTIF_ID_OVERRIDE
                )
            }
        }
    }
}
