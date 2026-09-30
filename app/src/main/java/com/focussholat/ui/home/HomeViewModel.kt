package com.focussholat.ui.home

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.*
import com.focussholat.data.entity.PrayerTime
import com.focussholat.data.repository.PrayerTimeRepository
import com.focussholat.data.repository.StatsRepository
import com.focussholat.service.PrayerTimeWorker
import com.focussholat.util.AppPreferences
import com.focussholat.util.PrayerTimeCalculator
import com.focussholat.util.PrayerTimeInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val statsRepository: StatsRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _prayerTime = MutableLiveData<PrayerTime?>()
    val prayerTime: LiveData<PrayerTime?> = _prayerTime

    private val _nextPrayer = MutableLiveData<PrayerTimeInfo?>()
    val nextPrayer: LiveData<PrayerTimeInfo?> = _nextPrayer

    private val _isBlockingActive = MutableLiveData(false)
    val isBlockingActive: LiveData<Boolean> = _isBlockingActive

    private val _isManualBlocking = MutableLiveData(false)
    val isManualBlocking: LiveData<Boolean> = _isManualBlocking

    private val _currentTime = MutableLiveData("")
    val currentTime: LiveData<String> = _currentTime

    private val _isAccessibilityEnabled = MutableLiveData(false)
    val isAccessibilityEnabled: LiveData<Boolean> = _isAccessibilityEnabled

    private val _isUsageAccessEnabled = MutableLiveData(false)
    val isUsageAccessEnabled: LiveData<Boolean> = _isUsageAccessEnabled

    private val _blockCountToday = MutableLiveData(0)
    val blockCountToday: LiveData<Int> = _blockCountToday

    private val _focusMinutesThisWeek = MutableLiveData(0L)
    val focusMinutesThisWeek: LiveData<Long> = _focusMinutesThisWeek

    init {
        refresh()
        observeBlockingState()
        startRealtimeClock()
    }

    private fun startRealtimeClock() {
        viewModelScope.launch {
            val clock = SimpleDateFormat("HH:mm:ss z", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone(appPreferences.timezone.first())
            }
            while (isActive) {
                _currentTime.postValue(clock.format(Date()))
                _prayerTime.value?.let { prayer ->
                    _nextPrayer.postValue(
                        PrayerTimeCalculator.getNextPrayerAt(
                            prayer,
                            java.util.Calendar.getInstance(clock.timeZone)
                        )
                    )
                }
                delay(1000)
            }
        }
    }

    private fun observeBlockingState() {
        viewModelScope.launch {
            appPreferences.blockingEnabled.collect { enabled ->
                _isBlockingActive.postValue(enabled)
            }
        }
        viewModelScope.launch {
            appPreferences.manualBlockingEnabled.collect { enabled ->
                _isManualBlocking.postValue(enabled)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val pt = prayerTimeRepository.getTodayPrayerTime()
            _prayerTime.postValue(pt)
            if (pt != null) {
                val zone = TimeZone.getTimeZone(appPreferences.timezone.first())
                _nextPrayer.postValue(
                    PrayerTimeCalculator.getNextPrayerAt(pt, java.util.Calendar.getInstance(zone))
                )
            }
        }
        viewModelScope.launch {
            _blockCountToday.postValue(statsRepository.getBlockCountToday())
        }
        viewModelScope.launch {
            _focusMinutesThisWeek.postValue(statsRepository.getFocusMinutesThisWeek())
        }
    }

    fun checkPermissions(context: Context) {
        _isAccessibilityEnabled.value = isAccessibilityServiceEnabled(context)
        _isUsageAccessEnabled.value = isUsageStatsPermissionGranted(context)
    }

    private val _permissionRequired = MutableLiveData<String?>()
    val permissionRequired: LiveData<String?> = _permissionRequired

    fun setBlockingEnabled(enabled: Boolean) {
        if (!enabled) {
            viewModelScope.launch {
                appPreferences.setManualBlockingEnabled(false)
                appPreferences.setBlockingEnabled(false)
            }
            return
        }
        if (enabled) {
            val accessibilityOk = isAccessibilityServiceEnabled(context)
            val usageOk = isUsageStatsPermissionGranted(context)
            if (!accessibilityOk) {
                _permissionRequired.value = "accessibility"
                return
            }
            if (!usageOk) {
                _permissionRequired.value = "usage"
                return
            }
        }
        viewModelScope.launch {
            appPreferences.setBlockingEnabled(enabled)
        }
    }

    fun setManualBlockingEnabled(enabled: Boolean) {
        if (enabled) {
            val accessibilityOk = isAccessibilityServiceEnabled(context)
            if (!accessibilityOk) {
                _permissionRequired.value = "accessibility"
                return
            }
        }
        viewModelScope.launch {
            appPreferences.setManualBlockingEnabled(enabled)
            // Manual mode is a direct override of schedules, but still uses the
            // same master switch so the service can be stopped from the UI.
            appPreferences.setBlockingEnabled(enabled)
        }
    }

    fun clearManualBlocking() {
        viewModelScope.launch {
            appPreferences.setManualBlockingEnabled(false)
            appPreferences.setBlockingEnabled(false)
        }
    }

    fun onPermissionHandled() {
        _permissionRequired.value = null
    }

    fun fetchPrayerTimes() {
        viewModelScope.launch {
            val mode = appPreferences.locationMode.first()
            val result = if (mode == "GPS") {
                val latitude = appPreferences.latitude.first()
                val longitude = appPreferences.longitude.first()
                if (latitude != 0.0 && longitude != 0.0) {
                    prayerTimeRepository.fetchAndSavePrayerTimeByCoordinates(latitude, longitude)
                } else {
                    Result.failure(Exception("Lokasi GPS belum tersedia"))
                }
            } else {
                val provinsi = appPreferences.provinsi.first().ifEmpty { "Jawa Barat" }
                val kabkota = appPreferences.kabkota.first().ifEmpty { "Kota Bogor" }
                prayerTimeRepository.fetchAndSavePrayerTime(provinsi, kabkota)
            }
            if (result.isSuccess) {
                _prayerTime.postValue(result.getOrNull())
                result.getOrNull()?.let {
                    val zone = TimeZone.getTimeZone(appPreferences.timezone.first())
                    _nextPrayer.postValue(
                        PrayerTimeCalculator.getNextPrayerAt(it, java.util.Calendar.getInstance(zone))
                    )
                }
            }
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val service = "${context.packageName}/com.focussholat.service.BlockingService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            if (splitter.next().equals(service, ignoreCase = true)) return true
        }
        return false
    }

    private fun isUsageStatsPermissionGranted(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
}
