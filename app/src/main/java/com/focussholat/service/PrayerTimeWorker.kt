package com.focussholat.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.focussholat.data.repository.PrayerTimeRepository
import com.focussholat.util.AppPreferences
import com.focussholat.util.NotificationHelper
import com.focussholat.util.PrayerTimeCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class PrayerTimeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val appPreferences: AppPreferences
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val locationMode = appPreferences.locationMode.first()
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val today = dateFormat.format(Date())

            val provinsi = appPreferences.provinsi.first().ifEmpty { "Jawa Barat" }
            val kabkota = appPreferences.kabkota.first().ifEmpty { "Kota Bogor" }
            val latitude = appPreferences.latitude.first()
            val longitude = appPreferences.longitude.first()

            // A fresh row is reusable only when it belongs to the currently selected city.
            // Otherwise changing the manual city would incorrectly keep the old schedule.
            val existing = prayerTimeRepository.getTodayPrayerTime()
            val selectedCity = "$kabkota, $provinsi"
            val existingMatchesLocation = if (locationMode == "GPS") {
                existing?.cityName == "Lokasi GPS" && latitude != 0.0 && longitude != 0.0 &&
                    existing.latitude == latitude && existing.longitude == longitude
            } else {
                existing?.cityName == selectedCity
            }
            if (existing != null && existingMatchesLocation &&
                (System.currentTimeMillis() - existing.fetchedAt) < 12 * 60 * 60 * 1000) {
                scheduleAlarmsIfNeeded()
                return Result.success()
            }

            val fetchResult = if (locationMode == "GPS" && latitude != 0.0 && longitude != 0.0) {
                prayerTimeRepository.fetchAndSavePrayerTimeByCoordinates(latitude, longitude)
            } else {
                prayerTimeRepository.fetchAndSavePrayerTime(provinsi, kabkota)
            }

            if (fetchResult.isSuccess) {
                scheduleAlarmsIfNeeded()
                prayerTimeRepository.cleanupOldData()
                Result.success()
            } else {
                // Retry later
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun scheduleAlarmsIfNeeded() {
        try {
            val prayerTime = prayerTimeRepository.getTodayPrayerTime() ?: return
            val reminderMinutes = appPreferences.reminderMinutes.first()
            val alarmHelper = AlarmScheduler(applicationContext)

            val prayers = PrayerTimeCalculator.getPrayerTimesForToday(prayerTime)
            val now = System.currentTimeMillis()

            prayers.forEach { prayer ->
                val reminderTime = prayer.calendar.timeInMillis - (reminderMinutes * 60 * 1000L)
                if (reminderTime > now) {
                    alarmHelper.schedulePrayerReminder(
                        prayer.key,
                        prayer.displayName,
                        reminderTime
                    )
                }
                if (prayer.calendar.timeInMillis > now) {
                    alarmHelper.schedulePrayerStart(
                        prayer.key,
                        prayer.displayName,
                        prayer.calendar.timeInMillis
                    )
                }
            }
        } catch (e: Exception) {
            // Failed to schedule alarms
        }
    }

    companion object {
        const val WORK_NAME = "PrayerTimeWorker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<PrayerTimeWorker>(
                6, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun scheduleImmediate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<PrayerTimeWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
