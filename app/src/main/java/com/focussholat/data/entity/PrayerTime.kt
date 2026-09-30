package com.focussholat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_times")
data class PrayerTime(
    @PrimaryKey
    val date: String, // "dd-MM-yyyy"
    val fajr: String,    // "04:30"
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "",
    val fetchedAt: Long = System.currentTimeMillis()
)
