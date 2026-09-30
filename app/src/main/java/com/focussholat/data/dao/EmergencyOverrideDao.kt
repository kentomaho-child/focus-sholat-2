package com.focussholat.data.dao

import androidx.room.*
import com.focussholat.data.entity.EmergencyOverride
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyOverrideDao {

    @Query("SELECT * FROM emergency_overrides WHERE isActive = 1 AND endTime > :now")
    fun getActiveOverrides(now: Long = System.currentTimeMillis()): Flow<List<EmergencyOverride>>

    @Query("SELECT * FROM emergency_overrides WHERE isActive = 1 AND endTime > :now AND packageName = :packageName LIMIT 1")
    suspend fun getActiveOverrideForPackage(packageName: String, now: Long = System.currentTimeMillis()): EmergencyOverride?

    @Insert
    suspend fun insertOverride(override: EmergencyOverride): Long

    @Query("UPDATE emergency_overrides SET isActive = 0 WHERE id = :id")
    suspend fun deactivateOverride(id: Long)

    @Query("UPDATE emergency_overrides SET isActive = 0 WHERE endTime <= :now")
    suspend fun deactivateExpiredOverrides(now: Long = System.currentTimeMillis())

    @Query("UPDATE emergency_overrides SET isActive = 0 WHERE packageName = :packageName")
    suspend fun deactivateOverridesForPackage(packageName: String)

    @Query("DELETE FROM emergency_overrides WHERE startTime < :before")
    suspend fun deleteOldOverrides(before: Long)
}
