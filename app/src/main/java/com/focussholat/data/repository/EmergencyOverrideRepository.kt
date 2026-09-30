package com.focussholat.data.repository

import com.focussholat.data.dao.EmergencyOverrideDao
import com.focussholat.data.entity.EmergencyOverride
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyOverrideRepository @Inject constructor(
    private val emergencyOverrideDao: EmergencyOverrideDao
) {
    fun getActiveOverrides(): Flow<List<EmergencyOverride>> =
        emergencyOverrideDao.getActiveOverrides()

    suspend fun getActiveOverrideForPackage(packageName: String): EmergencyOverride? =
        emergencyOverrideDao.getActiveOverrideForPackage(packageName)

    suspend fun createOverride(packageName: String, appName: String, durationMinutes: Int): Long {
        val now = System.currentTimeMillis()
        val override = EmergencyOverride(
            packageName = packageName,
            appName = appName,
            startTime = now,
            durationMinutes = durationMinutes,
            endTime = now + (durationMinutes * 60 * 1000L),
            isActive = true
        )
        return emergencyOverrideDao.insertOverride(override)
    }

    suspend fun deactivateOverride(id: Long) = emergencyOverrideDao.deactivateOverride(id)

    suspend fun deactivateExpiredOverrides() = emergencyOverrideDao.deactivateExpiredOverrides()

    suspend fun deactivateOverridesForPackage(packageName: String) =
        emergencyOverrideDao.deactivateOverridesForPackage(packageName)

    suspend fun isPackageOverridden(packageName: String): Boolean {
        return emergencyOverrideDao.getActiveOverrideForPackage(packageName) != null
    }
}
