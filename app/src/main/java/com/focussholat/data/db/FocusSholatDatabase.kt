package com.focussholat.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.focussholat.data.dao.*
import com.focussholat.data.entity.*

@Database(
    entities = [
        BlockedApp::class,
        CustomSchedule::class,
        PrayerTime::class,
        BlockEvent::class,
        FocusSession::class,
        EmergencyOverride::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FocusSholatDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun customScheduleDao(): CustomScheduleDao
    abstract fun prayerTimeDao(): PrayerTimeDao
    abstract fun blockEventDao(): BlockEventDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun emergencyOverrideDao(): EmergencyOverrideDao
}
