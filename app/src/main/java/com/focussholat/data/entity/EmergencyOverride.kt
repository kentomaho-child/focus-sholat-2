package com.focussholat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_overrides")
data class EmergencyOverride(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val startTime: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val endTime: Long = startTime + (durationMinutes * 60 * 1000L),
    val isActive: Boolean = true
)
