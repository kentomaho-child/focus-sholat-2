package com.focussholat.data.repository

import com.focussholat.data.dao.BlockedAppDao
import com.focussholat.data.entity.BlockedApp
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedAppRepository @Inject constructor(
    private val blockedAppDao: BlockedAppDao
) {
    fun getAllApps(): Flow<List<BlockedApp>> = blockedAppDao.getAllApps()

    fun getActiveBlockedApps(): Flow<List<BlockedApp>> = blockedAppDao.getActiveBlockedApps()

    fun getWhitelistedApps(): Flow<List<BlockedApp>> = blockedAppDao.getWhitelistedApps()

    suspend fun getActiveBlockedPackageNames(): List<String> =
        blockedAppDao.getActiveBlockedPackageNames()

    suspend fun getWhitelistedPackageNames(): List<String> =
        blockedAppDao.getWhitelistedPackageNames()

    suspend fun getAppByPackage(packageName: String): BlockedApp? =
        blockedAppDao.getAppByPackage(packageName)

    suspend fun insertApp(app: BlockedApp) = blockedAppDao.insertApp(app)

    suspend fun insertApps(apps: List<BlockedApp>) = blockedAppDao.insertApps(apps)

    suspend fun updateApp(app: BlockedApp) = blockedAppDao.updateApp(app)

    suspend fun deleteApp(app: BlockedApp) = blockedAppDao.deleteApp(app)

    suspend fun deleteByPackage(packageName: String) = blockedAppDao.deleteByPackage(packageName)

    suspend fun setEnabled(packageName: String, enabled: Boolean) =
        blockedAppDao.setEnabled(packageName, enabled)

    suspend fun setWhitelisted(packageName: String, whitelisted: Boolean) =
        blockedAppDao.setWhitelisted(packageName, whitelisted)
}
