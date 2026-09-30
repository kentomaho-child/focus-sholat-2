package com.focussholat.ui.blockedapps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focussholat.data.entity.BlockedApp
import com.focussholat.data.repository.BlockedAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppDisplayItem(
    val packageName: String,
    val appName: String,
    val isChecked: Boolean,
    val isSystem: Boolean = false
)

@HiltViewModel
class BlockedAppsViewModel @Inject constructor(
    private val blockedAppRepository: BlockedAppRepository
) : ViewModel() {

    val blockedAppsDisplay = MutableLiveData<List<AppDisplayItem>>()
    val whitelistedAppsDisplay = MutableLiveData<List<AppDisplayItem>>()
    val isLoading = MutableLiveData(false)

    private var allInstalledApps: List<AppDisplayItem> = emptyList()
    private var currentBlockedPackages: Set<String> = emptySet()
    private var currentWhitelistedPackages: Set<String> = emptySet()
    private var currentFilter = ""

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            isLoading.value = true
            withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val blockedPkgs = blockedAppRepository.getActiveBlockedPackageNames().toSet()
                val whitelistedPkgs = blockedAppRepository.getWhitelistedPackageNames().toSet()
                currentBlockedPackages = blockedPkgs
                currentWhitelistedPackages = whitelistedPkgs

                val launchableApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { appInfo ->
                        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val hasLaunchIntent = pm.getLaunchIntentForPackage(appInfo.packageName) != null
                        !isSystemApp && hasLaunchIntent &&
                                appInfo.packageName != context.packageName
                    }
                    .map { appInfo ->
                        AppDisplayItem(
                            packageName = appInfo.packageName,
                            appName = pm.getApplicationLabel(appInfo).toString(),
                            isChecked = appInfo.packageName in blockedPkgs,
                            isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        )
                    }
                    .sortedBy { it.appName }

                allInstalledApps = launchableApps
            }
            isLoading.value = false
            filterApps(currentFilter)
        }
    }

    fun filterApps(query: String) {
        currentFilter = query
        val filtered = if (query.isEmpty()) allInstalledApps
        else allInstalledApps.filter {
            it.appName.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
        }

        blockedAppsDisplay.value = filtered.map {
            it.copy(isChecked = it.packageName in currentBlockedPackages)
        }
        whitelistedAppsDisplay.value = filtered.map {
            it.copy(isChecked = it.packageName in currentWhitelistedPackages)
        }
    }

    fun toggleBlocked(packageName: String, appName: String, isBlocked: Boolean) {
        viewModelScope.launch {
            if (isBlocked) {
                blockedAppRepository.insertApp(
                    BlockedApp(
                        packageName = packageName,
                        appName = appName,
                        isEnabled = true,
                        isWhitelisted = false
                    )
                )
                currentBlockedPackages = currentBlockedPackages + packageName
            } else {
                blockedAppRepository.deleteByPackage(packageName)
                currentBlockedPackages = currentBlockedPackages - packageName
            }
            filterApps(currentFilter)
        }
    }

    fun toggleWhitelisted(packageName: String, appName: String, isWhitelisted: Boolean) {
        viewModelScope.launch {
            val existing = blockedAppRepository.getAppByPackage(packageName)
            if (isWhitelisted) {
                if (existing != null) {
                    blockedAppRepository.setWhitelisted(packageName, true)
                } else {
                    blockedAppRepository.insertApp(
                        BlockedApp(
                            packageName = packageName,
                            appName = appName,
                            isEnabled = false,
                            isWhitelisted = true
                        )
                    )
                }
                currentWhitelistedPackages = currentWhitelistedPackages + packageName
            } else {
                blockedAppRepository.setWhitelisted(packageName, false)
                currentWhitelistedPackages = currentWhitelistedPackages - packageName
            }
            filterApps(currentFilter)
        }
    }
}
