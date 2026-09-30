package com.focussholat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_schedules")
data class CustomSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: String, // "1,2,3,4,5,6,7" (1=Mon ... 7=Sun)
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
