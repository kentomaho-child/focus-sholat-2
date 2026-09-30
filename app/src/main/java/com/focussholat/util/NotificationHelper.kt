package com.focussholat.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.focussholat.MainActivity
import com.focussholat.R

object NotificationHelper {

    const val CHANNEL_ID_BLOCKING = "blocking_service"
    const val CHANNEL_ID_PRAYER = "prayer_reminder"
    const val CHANNEL_ID_OVERRIDE = "emergency_override"

    const val NOTIF_ID_BLOCKING_SERVICE = 1001
    const val NOTIF_ID_PRAYER_REMINDER = 1002
    const val NOTIF_ID_OVERRIDE = 1003

    fun createChannels(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Blocking service channel (silent, persistent)
        val blockingChannel = NotificationChannel(
            CHANNEL_ID_BLOCKING,
            "FocusSholat Aktif",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifikasi layanan pemblokiran aplikasi"
            setShowBadge(false)
        }

        // Prayer reminder channel
        val prayerChannel = NotificationChannel(
            CHANNEL_ID_PRAYER,
            "Pengingat Waktu Sholat",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pengingat sebelum waktu sholat tiba"
            enableVibration(true)
        }

        // Emergency override channel
        val overrideChannel = NotificationChannel(
            CHANNEL_ID_OVERRIDE,
            "Override Darurat",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Status override darurat aktif"
        }

        notificationManager.createNotificationChannels(
            listOf(blockingChannel, prayerChannel, overrideChannel)
        )
    }

    fun buildBlockingServiceNotification(context: Context): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_BLOCKING)
            .setContentTitle("FocusSholat Aktif")
            .setContentText("Memantau penggunaan aplikasi...")
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun showPrayerReminderNotification(
        context: Context,
        prayerName: String,
        minutesBefore: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PRAYER)
            .setContentTitle("⏰ Waktu Sholat Segera Tiba")
            .setContentText("Sholat $prayerName dalam $minutesBefore menit. Bersiaplah!")
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 100, 500))
            .build()

        notificationManager.notify(NOTIF_ID_PRAYER_REMINDER, notification)
    }

    fun showPrayerStartNotification(context: Context, prayerName: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PRAYER)
            .setContentTitle("🕌 Waktu Sholat $prayerName")
            .setContentText("Aplikasi akan diblokir selama waktu sholat")
            .setSmallIcon(R.drawable.ic_mosque)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIF_ID_PRAYER_REMINDER + 10, notification)
    }

    fun showOverrideActiveNotification(
        context: Context,
        appName: String,
        remainingMinutes: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_OVERRIDE)
            .setContentTitle("Override Darurat Aktif")
            .setContentText("$appName diizinkan selama $remainingMinutes menit lagi")
            .setSmallIcon(R.drawable.ic_warning)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIF_ID_OVERRIDE, notification)
    }

    fun cancelNotification(context: Context, notifId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notifId)
    }
}
