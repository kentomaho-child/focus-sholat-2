package com.focussholat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focussholat.service.PrayerTimeWorker
import com.focussholat.util.NotificationHelper

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                NotificationHelper.createChannels(context)
                PrayerTimeWorker.schedule(context)
                PrayerTimeWorker.scheduleImmediate(context)
            }
        }
    }
}
