package com.focussholat.data.repository

import com.focussholat.data.dao.BlockEventDao
import com.focussholat.data.dao.FocusSessionDao
import com.focussholat.data.entity.BlockEvent
import com.focussholat.data.entity.FocusSession
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val blockEventDao: BlockEventDao,
    private val focusSessionDao: FocusSessionDao
) {
    fun getTotalBlockCount(): Flow<Int> = blockEventDao.getTotalBlockCount()

    fun getTotalFocusMinutes(): Flow<Long?> = focusSessionDao.getTotalFocusMinutes()

    fun getTotalSessionCount(): Flow<Int> = focusSessionDao.getTotalSessionCount()

    fun getAllEvents(): Flow<List<BlockEvent>> = blockEventDao.getAllEvents()

    fun getAllSessions(): Flow<List<FocusSession>> = focusSessionDao.getAllSessions()

    suspend fun logBlockEvent(packageName: String, appName: String, reason: String) {
        blockEventDao.insertEvent(
            BlockEvent(
                packageName = packageName,
                appName = appName,
                reason = reason
            )
        )
    }

    suspend fun startFocusSession(type: String, scheduleName: String = ""): Long {
        return focusSessionDao.insertSession(
            FocusSession(
                startTime = System.currentTimeMillis(),
                type = type,
                scheduleName = scheduleName
            )
        )
    }

    suspend fun endFocusSession(sessionId: Long) {
        val now = System.currentTimeMillis()
        val session = focusSessionDao.getAllSessions()
        // We need to get the session directly - use a simpler approach
        val endTime = now
        focusSessionDao.completeSession(
            id = sessionId,
            endTime = endTime,
            duration = 0 // Will be calculated separately
        )
    }

    suspend fun completeSession(sessionId: Long, startTime: Long) {
        val now = System.currentTimeMillis()
        val durationMinutes = (now - startTime) / (1000 * 60)
        focusSessionDao.completeSession(
            id = sessionId,
            endTime = now,
            duration = durationMinutes
        )
    }

    suspend fun getBlockCountToday(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000
        return blockEventDao.getBlockCountForDay(startOfDay, endOfDay)
    }

    suspend fun getFocusMinutesThisWeek(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return focusSessionDao.getFocusMinutesSince(calendar.timeInMillis) ?: 0
    }

    suspend fun cleanupOldData() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        blockEventDao.deleteOldEvents(thirtyDaysAgo)
    }
}
