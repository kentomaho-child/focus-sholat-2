package com.focussholat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_events")
data class BlockEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val blockedAt: Long = System.currentTimeMillis(),
    val reason: String, // "PRAYER_TIME", "CUSTOM_SCHEDULE"
    val sessionId: Long = 0
)
