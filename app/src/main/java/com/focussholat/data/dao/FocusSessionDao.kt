package com.focussholat.data.dao

import androidx.room.*
import com.focussholat.data.entity.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :since ORDER BY startTime DESC")
    fun getSessionsSince(since: Long): Flow<List<FocusSession>>

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions")
    fun getTotalFocusMinutes(): Flow<Long?>

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE startTime >= :since")
    suspend fun getFocusMinutesSince(since: Long): Long?

    @Query("SELECT COUNT(*) FROM focus_sessions")
    fun getTotalSessionCount(): Flow<Int>

    @Insert
    suspend fun insertSession(session: FocusSession): Long

    @Update
    suspend fun updateSession(session: FocusSession)

    @Query("UPDATE focus_sessions SET endTime = :endTime, durationMinutes = :duration WHERE id = :id")
    suspend fun completeSession(id: Long, endTime: Long, duration: Long)
}
