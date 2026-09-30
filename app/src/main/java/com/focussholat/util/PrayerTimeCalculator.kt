package com.focussholat.util

import com.focussholat.data.entity.PrayerTime
import java.text.SimpleDateFormat
import java.util.*

data class PrayerTimeInfo(
    val name: String,
    val displayName: String,
    val timeString: String,
    val calendar: Calendar,
    val key: String
)

object PrayerTimeCalculator {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun getPrayerTimesForToday(
        prayerTime: PrayerTime,
        referenceDate: Calendar = Calendar.getInstance()
    ): List<PrayerTimeInfo> {
        val prayers = listOf(
            Triple("fajr", "Subuh", prayerTime.fajr),
            Triple("dhuhr", "Dzuhur", prayerTime.dhuhr),
            Triple("asr", "Ashar", prayerTime.asr),
            Triple("maghrib", "Maghrib", prayerTime.maghrib),
            Triple("isha", "Isya", prayerTime.isha)
        )

        return prayers.map { (key, displayName, timeStr) ->
            val cal = parseTimeToCalendar(formatTime(timeStr), referenceDate)
            PrayerTimeInfo(
                name = key,
                displayName = displayName,
                timeString = timeStr,
                calendar = cal,
                key = key
            )
        }
    }

    fun getNextPrayer(prayerTime: PrayerTime): PrayerTimeInfo? =
        getNextPrayerAt(prayerTime, Calendar.getInstance())

    fun getNextPrayerAt(prayerTime: PrayerTime, now: Calendar): PrayerTimeInfo? {
        val prayers = getPrayerTimesForToday(prayerTime, now)
        // Use an explicit millisecond comparison so a prayer that already started
        // is never reported as the next prayer.
        val next = prayers.firstOrNull { it.calendar.timeInMillis > now.timeInMillis }
        if (next != null) return next

        // All of today's prayers have passed. Return Subuh with tomorrow's date,
        // not a stale same-day timestamp.
        val tomorrow = (prayers.firstOrNull()?.calendar?.clone() as? Calendar)?.apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return prayers.firstOrNull()?.copy(calendar = tomorrow ?: return null)
    }

    fun shouldBlock(
        blockingEnabled: Boolean,
        manualBlocking: Boolean,
        prayerTimeActive: Boolean,
        customScheduleActive: Boolean
    ): Boolean = blockingEnabled && (manualBlocking || prayerTimeActive || customScheduleActive)

    fun getCurrentPrayer(
        prayerTime: PrayerTime,
        durationMinutes: Int,
        enabledPrayers: Map<String, Boolean>
    ): PrayerTimeInfo? = getCurrentPrayerAt(
        prayerTime, durationMinutes, enabledPrayers, Calendar.getInstance()
    )

    fun getCurrentPrayerAt(
        prayerTime: PrayerTime,
        durationMinutes: Int,
        enabledPrayers: Map<String, Boolean>,
        now: Calendar
    ): PrayerTimeInfo? {
        val prayers = getPrayerTimesForToday(prayerTime, now)

        return prayers.firstOrNull { prayer ->
            val enabled = enabledPrayers[prayer.key] ?: true
            if (!enabled) return@firstOrNull false

            val startMillis = prayer.calendar.timeInMillis
            val endMillis = startMillis + durationMinutes * 60 * 1000L
            now.timeInMillis >= startMillis && now.timeInMillis < endMillis
        }
    }

    fun isCurrentlyPrayerTime(
        prayerTime: PrayerTime,
        durationMinutes: Int,
        enabledPrayers: Map<String, Boolean>
    ): Boolean {
        return getCurrentPrayer(prayerTime, durationMinutes, enabledPrayers) != null
    }

    fun getMinutesUntilNextPrayer(prayerTime: PrayerTime): Long {
        val next = getNextPrayer(prayerTime) ?: return -1
        val now = System.currentTimeMillis()
        val prayerMs = next.calendar.timeInMillis
        return (prayerMs - now) / (60 * 1000)
    }

    fun parseTimeToCalendar(
        timeStr: String,
        referenceDate: Calendar = Calendar.getInstance()
    ): Calendar {
        val cal = (referenceDate.clone() as Calendar).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return try {
            val parts = timeStr.trim().split(":")
            cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            cal.set(Calendar.MINUTE, parts[1].toInt())
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal
        } catch (e: Exception) {
            cal
        }
    }

    fun formatTime(timeStr: String): String {
        return try {
            val match = Regex("(\\d{1,2}):(\\d{2})").find(timeStr.trim())
                ?: return timeStr.trim()
            val hour = match.groupValues[1].toInt()
            val minute = match.groupValues[2].toInt()
            String.format("%02d:%02d", hour, minute)
        } catch (e: Exception) {
            timeStr
        }
    }

    fun isScheduleActiveNow(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        daysOfWeek: String
    ): Boolean = isScheduleActiveAt(
        Calendar.getInstance(), startHour, startMinute, endHour, endMinute, daysOfWeek
    )

    /**
     * Checks a manual schedule against an explicit clock. Keeping the clock injectable
     * makes the boundary behavior testable and avoids relying on a stale cached state.
     * daysOfWeek uses 1=Monday ... 7=Sunday.
     */
    fun isScheduleActiveAt(
        now: Calendar,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        daysOfWeek: String
    ): Boolean {
        val enabledDays = daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        if (enabledDays.isEmpty()) return false

        val currentDay = when (now.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> return false
        }
        val previousDay = if (currentDay == 1) 7 else currentDay - 1
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        return if (endMinutes > startMinutes) {
            currentDay in enabledDays && currentMinutes in startMinutes until endMinutes
        } else if (startMinutes > endMinutes) {
            (currentDay in enabledDays && currentMinutes >= startMinutes) ||
                (previousDay in enabledDays && currentMinutes < endMinutes)
        } else {
            false
        }
    }
}
