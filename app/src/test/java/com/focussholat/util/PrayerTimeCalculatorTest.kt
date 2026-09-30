package com.focussholat.util

import com.focussholat.data.entity.PrayerTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class PrayerTimeCalculatorTest {

    @Test
    fun customSchedule_isActiveWithinWindowOnSelectedDay() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 30, 10, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertEquals(
            true,
            PrayerTimeCalculator.isScheduleActiveAt(
                now, 10, 0, 11, 0, "3"
            )
        )
    }

    @Test
    fun customSchedule_isInactiveOutsideWindow() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 30, 11, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertEquals(
            false,
            PrayerTimeCalculator.isScheduleActiveAt(
                now, 10, 0, 11, 0, "3"
            )
        )
    }

    @Test
    fun currentPrayer_usesConfiguredDuration() {
        val prayer = PrayerTime(
            date = "30-09-2026",
            fajr = "04:30",
            dhuhr = "12:00",
            asr = "15:00",
            maghrib = "18:00",
            isha = "19:00",
            latitude = 0.0,
            longitude = 0.0,
            cityName = "Kota Bogor"
        )
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 30, 12, 15, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val result = PrayerTimeCalculator.getCurrentPrayerAt(
            prayer, 30, emptyMap(), now
        )

        assertEquals("dhuhr", result?.key)
    }

    @Test
    fun nextPrayer_afterIshaIsSubuh() {
        val prayer = samplePrayer()
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 30, 23, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val result = PrayerTimeCalculator.getNextPrayerAt(prayer, now)

        assertEquals("fajr", result?.key)
        assertEquals("Subuh", result?.displayName)
    }

    @Test
    fun manualBlocking_isActiveWithoutPrayerOrSchedule() {
        assertEquals(true, PrayerTimeCalculator.shouldBlock(true, true, false, false))
        assertEquals(false, PrayerTimeCalculator.shouldBlock(true, false, false, false))
        assertEquals(false, PrayerTimeCalculator.shouldBlock(false, true, true, true))
    }

    @Test
    fun formatTime_removesTimezoneSuffixFromApiValue() {
        assertEquals("18:05", PrayerTimeCalculator.formatTime("18:05 (WIB)"))
    }

    private fun samplePrayer() = PrayerTime(
        date = "30-09-2026",
        fajr = "04:30",
        dhuhr = "12:00",
        asr = "15:00",
        maghrib = "18:00",
        isha = "19:00",
        latitude = 0.0,
        longitude = 0.0,
        cityName = "Kota Bogor"
    )

    @Test
    fun currentPrayer_isNullAfterConfiguredWindow() {
        val prayer = PrayerTime(
            date = "30-09-2026",
            fajr = "04:30",
            dhuhr = "12:00",
            asr = "15:00",
            maghrib = "18:00",
            isha = "19:00",
            latitude = 0.0,
            longitude = 0.0,
            cityName = "Kota Bogor"
        )
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 30, 12, 31, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertNull(
            PrayerTimeCalculator.getCurrentPrayerAt(prayer, 30, emptyMap(), now)
        )
    }
}
