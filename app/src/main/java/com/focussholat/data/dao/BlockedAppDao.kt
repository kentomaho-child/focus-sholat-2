package com.focussholat.data.dao

import androidx.room.*
import com.focussholat.data.entity.BlockedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {

    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun getAllApps(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps WHERE isEnabled = 1 AND isWhitelisted = 0")
    fun getActiveBlockedApps(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps WHERE isWhitelisted = 1")
    fun getWhitelistedApps(): Flow<List<BlockedApp>>

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppByPackage(packageName: String): BlockedApp?

    @Query("SELECT packageName FROM blocked_apps WHERE isEnabled = 1 AND isWhitelisted = 0")
    suspend fun getActiveBlockedPackageNames(): List<String>

    @Query("SELECT packageName FROM blocked_apps WHERE isWhitelisted = 1")
    suspend fun getWhitelistedPackageNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: BlockedApp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<BlockedApp>)

    @Update
    suspend fun updateApp(app: BlockedApp)

    @Delete
    suspend fun deleteApp(app: BlockedApp)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)

    @Query("UPDATE blocked_apps SET isEnabled = :enabled WHERE packageName = :packageName")
    suspend fun setEnabled(packageName: String, enabled: Boolean)

    @Query("UPDATE blocked_apps SET isWhitelisted = :whitelisted WHERE packageName = :packageName")
    suspend fun setWhitelisted(packageName: String, whitelisted: Boolean)
}
