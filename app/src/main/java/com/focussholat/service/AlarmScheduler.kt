package com.focussholat.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.focussholat.receiver.AlarmReceiver

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedulePrayerReminder(prayerKey: String, prayerName: String, triggerAtMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_PRAYER_REMINDER
            putExtra(AlarmReceiver.EXTRA_PRAYER_KEY, prayerKey)
            putExtra(AlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
        }
        val requestCode = "reminder_$prayerKey".hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun schedulePrayerStart(prayerKey: String, prayerName: String, triggerAtMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_PRAYER_START
            putExtra(AlarmReceiver.EXTRA_PRAYER_KEY, prayerKey)
            putExtra(AlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
        }
        val requestCode = "start_$prayerKey".hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun scheduleEmergencyOverrideEnd(overrideId: Long, packageName: String, endTimeMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_EMERGENCY_OVERRIDE_END
            putExtra(AlarmReceiver.EXTRA_OVERRIDE_ID, overrideId)
            putExtra(AlarmReceiver.EXTRA_PACKAGE_NAME, packageName)
        }
        val requestCode = "override_$overrideId".hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                endTimeMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, endTimeMillis, pendingIntent)
        }
    }

    fun cancelPrayerAlarms(prayerKey: String) {
        listOf("reminder_$prayerKey", "start_$prayerKey").forEach { tag ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, tag.hashCode(), intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}
