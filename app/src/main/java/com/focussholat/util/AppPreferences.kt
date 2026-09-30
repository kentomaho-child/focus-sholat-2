package com.focussholat.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_sholat_prefs")

class AppPreferences(private val context: Context) {

    companion object {
        val KEY_LOCATION_MODE = stringPreferencesKey("location_mode")
        val KEY_PROVINSI = stringPreferencesKey("provinsi")
        val KEY_KABKOTA = stringPreferencesKey("kabkota")
        val KEY_CITY_NAME = stringPreferencesKey("city_name")
        val KEY_COUNTRY_NAME = stringPreferencesKey("country_name")
        val KEY_LATITUDE = doublePreferencesKey("latitude")
        val KEY_LONGITUDE = doublePreferencesKey("longitude")
        val KEY_TIMEZONE = stringPreferencesKey("timezone")
        val KEY_REMINDER_MINUTES = intPreferencesKey("reminder_minutes")
        val KEY_BLOCKING_ENABLED = booleanPreferencesKey("blocking_enabled")
        val KEY_MANUAL_BLOCKING_ENABLED = booleanPreferencesKey("manual_blocking_enabled")
        val KEY_PRAYER_BLOCKING_ENABLED = booleanPreferencesKey("prayer_blocking_enabled")
        val KEY_STRICT_MODE = booleanPreferencesKey("strict_mode")
        val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        val KEY_PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val KEY_PRAYER_DURATION_MINUTES = intPreferencesKey("prayer_duration_minutes")
        val KEY_FAJR_ENABLED = booleanPreferencesKey("fajr_enabled")
        val KEY_DHUHR_ENABLED = booleanPreferencesKey("dhuhr_enabled")
        val KEY_ASR_ENABLED = booleanPreferencesKey("asr_enabled")
        val KEY_MAGHRIB_ENABLED = booleanPreferencesKey("maghrib_enabled")
        val KEY_ISHA_ENABLED = booleanPreferencesKey("isha_enabled")
        val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val KEY_LAST_PRAYER_FETCH = longPreferencesKey("last_prayer_fetch")
    }

    val locationMode: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_LOCATION_MODE] ?: "MANUAL" }

    val provinsi: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_PROVINSI] ?: "Jawa Barat" }

    val kabkota: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_KABKOTA] ?: "Kota Bogor" }

    val cityName: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_CITY_NAME] ?: "Kota Bogor" }

    val countryName: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_COUNTRY_NAME] ?: "Indonesia" }

    val latitude: Flow<Double> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_LATITUDE] ?: 0.0 }

    val longitude: Flow<Double> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_LONGITUDE] ?: 0.0 }

    val timezone: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_TIMEZONE] ?: java.util.TimeZone.getDefault().id }

    val reminderMinutes: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_REMINDER_MINUTES] ?: 10 }

    val blockingEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_BLOCKING_ENABLED] ?: false }

    val manualBlockingEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_MANUAL_BLOCKING_ENABLED] ?: false }

    val prayerBlockingEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_PRAYER_BLOCKING_ENABLED] ?: true }

    val strictMode: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_STRICT_MODE] ?: false }

    val pinEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_PIN_ENABLED] ?: false }

    val pinHash: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_PIN_HASH] ?: "" }

    val prayerDurationMinutes: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_PRAYER_DURATION_MINUTES] ?: 30 }

    val fajrEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_FAJR_ENABLED] ?: true }

    val dhuhrEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_DHUHR_ENABLED] ?: true }

    val asrEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_ASR_ENABLED] ?: true }

    val maghribEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_MAGHRIB_ENABLED] ?: true }

    val ishaEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_ISHA_ENABLED] ?: true }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_FIRST_LAUNCH] ?: true }

    suspend fun setLocationMode(mode: String) {
        context.dataStore.edit { it[KEY_LOCATION_MODE] = mode }
    }

    suspend fun setProvinsi(provinsi: String) {
        context.dataStore.edit { it[KEY_PROVINSI] = provinsi }
    }

    suspend fun setKabKota(kabkota: String) {
        context.dataStore.edit { it[KEY_KABKOTA] = kabkota }
    }

    suspend fun setCityName(city: String) {
        context.dataStore.edit { it[KEY_CITY_NAME] = city }
    }

    suspend fun setCountryName(country: String) {
        context.dataStore.edit { it[KEY_COUNTRY_NAME] = country }
    }

    suspend fun setCoordinates(lat: Double, lon: Double, zoneId: String = java.util.TimeZone.getDefault().id) {
        context.dataStore.edit {
            it[KEY_LATITUDE] = lat
            it[KEY_LONGITUDE] = lon
            it[KEY_TIMEZONE] = zoneId
        }
    }

    suspend fun setTimezone(zoneId: String) {
        context.dataStore.edit { it[KEY_TIMEZONE] = zoneId }
    }

    
    suspend fun setReminderMinutes(minutes: Int) {
        context.dataStore.edit { it[KEY_REMINDER_MINUTES] = minutes }
    }

    suspend fun setBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BLOCKING_ENABLED] = enabled }
    }

    suspend fun setManualBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_MANUAL_BLOCKING_ENABLED] = enabled }
    }

    suspend fun setPrayerBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_PRAYER_BLOCKING_ENABLED] = enabled }
    }

    suspend fun setStrictMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_STRICT_MODE] = enabled }
    }

    suspend fun setPinEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_PIN_ENABLED] = enabled }
    }

    suspend fun setPinHash(hash: String) {
        context.dataStore.edit { it[KEY_PIN_HASH] = hash }
    }

    suspend fun setPrayerDurationMinutes(minutes: Int) {
        context.dataStore.edit { it[KEY_PRAYER_DURATION_MINUTES] = minutes }
    }

    suspend fun setPrayerEnabled(prayer: String, enabled: Boolean) {
        context.dataStore.edit {
            when (prayer) {
                "fajr" -> it[KEY_FAJR_ENABLED] = enabled
                "dhuhr" -> it[KEY_DHUHR_ENABLED] = enabled
                "asr" -> it[KEY_ASR_ENABLED] = enabled
                "maghrib" -> it[KEY_MAGHRIB_ENABLED] = enabled
                "isha" -> it[KEY_ISHA_ENABLED] = enabled
            }
        }
    }

    suspend fun setFirstLaunch(isFirst: Boolean) {
        context.dataStore.edit { it[KEY_FIRST_LAUNCH] = isFirst }
    }

    suspend fun setLastPrayerFetch(timestamp: Long) {
        context.dataStore.edit { it[KEY_LAST_PRAYER_FETCH] = timestamp }
    }
}
