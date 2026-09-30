package com.focussholat.data.dao

import androidx.room.*
import com.focussholat.data.entity.BlockEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockEventDao {

    @Query("SELECT * FROM block_events ORDER BY blockedAt DESC")
    fun getAllEvents(): Flow<List<BlockEvent>>

    @Query("SELECT * FROM block_events WHERE blockedAt >= :since ORDER BY blockedAt DESC")
    fun getEventsSince(since: Long): Flow<List<BlockEvent>>

    @Query("SELECT COUNT(*) FROM block_events")
    fun getTotalBlockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM block_events WHERE blockedAt >= :since")
    suspend fun getBlockCountSince(since: Long): Int

    @Query("SELECT COUNT(*) FROM block_events WHERE blockedAt >= :startOfDay AND blockedAt < :endOfDay")
    suspend fun getBlockCountForDay(startOfDay: Long, endOfDay: Long): Int

    @Insert
    suspend fun insertEvent(event: BlockEvent)

    @Query("DELETE FROM block_events WHERE blockedAt < :before")
    suspend fun deleteOldEvents(before: Long)
}
