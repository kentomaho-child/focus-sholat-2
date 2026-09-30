package com.focussholat.data.dao

import androidx.room.*
import com.focussholat.data.entity.CustomSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomScheduleDao {

    @Query("SELECT * FROM custom_schedules ORDER BY startHour ASC, startMinute ASC")
    fun getAllSchedules(): Flow<List<CustomSchedule>>

    @Query("SELECT * FROM custom_schedules WHERE isEnabled = 1")
    fun getActiveSchedules(): Flow<List<CustomSchedule>>

    @Query("SELECT * FROM custom_schedules WHERE isEnabled = 1")
    suspend fun getActiveSchedulesList(): List<CustomSchedule>

    @Query("SELECT * FROM custom_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): CustomSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: CustomSchedule): Long

    @Update
    suspend fun updateSchedule(schedule: CustomSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: CustomSchedule)

    @Query("DELETE FROM custom_schedules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE custom_schedules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
