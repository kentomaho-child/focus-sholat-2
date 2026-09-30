package com.focussholat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long = 0,
    val type: String, // "PRAYER_FAJR", "PRAYER_DHUHR", "PRAYER_ASR", "PRAYER_MAGHRIB", "PRAYER_ISHA", "CUSTOM"
    val scheduleName: String = "",
    val durationMinutes: Long = 0
)
