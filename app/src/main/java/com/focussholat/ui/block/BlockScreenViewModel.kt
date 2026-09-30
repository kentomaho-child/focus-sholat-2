package com.focussholat.ui.block

import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focussholat.data.repository.BlockedAppRepository
import com.focussholat.data.repository.EmergencyOverrideRepository
import com.focussholat.service.AlarmScheduler
import com.focussholat.util.AppPreferences
import com.focussholat.util.SecurityUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.Context
import javax.inject.Inject

@HiltViewModel
class BlockScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val emergencyOverrideRepository: EmergencyOverrideRepository,
    private val blockedAppRepository: BlockedAppRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val pinRequired = MutableLiveData(false)
    val overrideCreated = MutableLiveData(false)

    private var storedPinHash = ""
    private var pinEnabled = false

    init {
        viewModelScope.launch {
            storedPinHash = appPreferences.pinHash.first()
            pinEnabled = appPreferences.pinEnabled.first()
        }
    }

    fun isPinEnabled(): Boolean = pinEnabled

    fun verifyPin(pin: String): Boolean {
        return SecurityUtil.verifyPin(pin, storedPinHash)
    }

    fun createEmergencyOverride(packageName: String, durationMinutes: Int) {
        viewModelScope.launch {
            val appName = getAppName(packageName)
            val now = System.currentTimeMillis()
            val overrideId = emergencyOverrideRepository.createOverride(
                packageName = packageName,
                appName = appName,
                durationMinutes = durationMinutes
            )
            val endTime = now + (durationMinutes * 60 * 1000L)
            // Schedule alarm to end override
            AlarmScheduler(context).scheduleEmergencyOverrideEnd(overrideId, packageName, endTime)
            overrideCreated.postValue(true)
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
