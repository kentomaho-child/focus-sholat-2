package com.focussholat.ui.settings

import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focussholat.service.PrayerTimeWorker
import com.focussholat.util.AppPreferences
import com.focussholat.util.SecurityUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    val locationMode = MutableLiveData("MANUAL")
    val cityName = MutableLiveData("Kota Bogor")
    val reminderMinutes = MutableLiveData(10)
    val prayerDuration = MutableLiveData(30)
    val pinEnabled = MutableLiveData(false)
    val strictMode = MutableLiveData(false)
    val fajrEnabled = MutableLiveData(true)
    val dhuhrEnabled = MutableLiveData(true)
    val asrEnabled = MutableLiveData(true)
    val maghribEnabled = MutableLiveData(true)
    val ishaEnabled = MutableLiveData(true)

    private var storedPinHash = ""

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            locationMode.postValue(appPreferences.locationMode.first())
            cityName.postValue(appPreferences.cityName.first())
            reminderMinutes.postValue(appPreferences.reminderMinutes.first())
            prayerDuration.postValue(appPreferences.prayerDurationMinutes.first())
            pinEnabled.postValue(appPreferences.pinEnabled.first())
            strictMode.postValue(appPreferences.strictMode.first())
            fajrEnabled.postValue(appPreferences.fajrEnabled.first())
            dhuhrEnabled.postValue(appPreferences.dhuhrEnabled.first())
            asrEnabled.postValue(appPreferences.asrEnabled.first())
            maghribEnabled.postValue(appPreferences.maghribEnabled.first())
            ishaEnabled.postValue(appPreferences.ishaEnabled.first())
            storedPinHash = appPreferences.pinHash.first()
        }
    }

    fun setLocationMode(mode: String, context: Context? = null) {
        viewModelScope.launch {
            appPreferences.setLocationMode(mode)
            locationMode.postValue(mode)
            if (mode == "GPS" && context != null) {
                val client = LocationServices.getFusedLocationProviderClient(context)
                try {
                    client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                viewModelScope.launch {
                                    appPreferences.setCoordinates(location.latitude, location.longitude)
                                    PrayerTimeWorker.scheduleImmediate(context)
                                }
                            }
                        }
                } catch (_: SecurityException) {
                    // Permission flow is handled by the settings screen/device.
                }
            }
        }
    }

    fun setCity(city: String, context: Context) {
        viewModelScope.launch {
            // The API request uses kabkota; keep the display name and API key in sync.
            appPreferences.setCityName(city)
            appPreferences.setKabKota(city)
            cityName.postValue(city)
            // Fetch only after both DataStore values have been committed.
            PrayerTimeWorker.scheduleImmediate(context)
        }
    }

    fun setReminderMinutes(minutes: Int) {
        viewModelScope.launch {
            appPreferences.setReminderMinutes(minutes)
        }
    }

    fun setPrayerDuration(duration: Int) {
        viewModelScope.launch {
            appPreferences.setPrayerDurationMinutes(duration)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            val hash = SecurityUtil.hashPin(pin)
            storedPinHash = hash
            appPreferences.setPinHash(hash)
            appPreferences.setPinEnabled(true)
            pinEnabled.postValue(true)
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            appPreferences.setPinEnabled(false)
            appPreferences.setPinHash("")
            storedPinHash = ""
            pinEnabled.postValue(false)
            // Also disable strict mode when PIN is removed
            appPreferences.setStrictMode(false)
            strictMode.postValue(false)
        }
    }

    fun verifyPin(pin: String): Boolean {
        return SecurityUtil.verifyPin(pin, storedPinHash)
    }

    fun setStrictMode(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setStrictMode(enabled)
            strictMode.postValue(enabled)
        }
    }

    fun setPrayerEnabled(prayer: String, enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setPrayerEnabled(prayer, enabled)
            when (prayer) {
                "fajr" -> fajrEnabled.postValue(enabled)
                "dhuhr" -> dhuhrEnabled.postValue(enabled)
                "asr" -> asrEnabled.postValue(enabled)
                "maghrib" -> maghribEnabled.postValue(enabled)
                "isha" -> ishaEnabled.postValue(enabled)
            }
        }
    }

    fun fetchPrayerTimes(context: Context) {
        PrayerTimeWorker.scheduleImmediate(context)
    }
}
