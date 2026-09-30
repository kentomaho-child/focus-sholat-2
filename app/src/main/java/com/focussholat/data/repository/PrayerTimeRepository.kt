package com.focussholat.data.repository

import com.focussholat.data.dao.PrayerTimeDao
import com.focussholat.data.entity.PrayerTime
import com.focussholat.network.AladhanApi
import com.focussholat.network.EQuranApi
import com.focussholat.network.JadwalRequest
import com.focussholat.network.KabKotaRequest
import com.focussholat.util.AppPreferences
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerTimeRepository @Inject constructor(
    private val prayerTimeDao: PrayerTimeDao,
    private val equranApi: EQuranApi,
    private val aladhanApi: AladhanApi,
    private val appPreferences: AppPreferences
) {
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    fun getRecentPrayerTimes(): Flow<List<PrayerTime>> = prayerTimeDao.getRecentPrayerTimes()

    suspend fun getTodayPrayerTime(): PrayerTime? {
        val today = dateFormat.format(Date())
        return prayerTimeDao.getPrayerTimeByDate(today)
    }

    suspend fun getProvinsiList(): List<String> {
        return try {
            equranApi.getProvinsi().data
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getKabKotaList(provinsi: String): List<String> {
        return try {
            equranApi.getKabKota(KabKotaRequest(provinsi)).data
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchAndSavePrayerTime(
        provinsi: String,
        kabkota: String
    ): Result<PrayerTime> {
        return fetchAndSavePrayerTimeByCityInternal(provinsi, kabkota)
    }

    suspend fun fetchAndSavePrayerTimeByCoordinates(
        latitude: Double,
        longitude: Double
    ): Result<PrayerTime> {
        return try {
            val response = aladhanApi.getPrayerTimesByCoordinates(
                dateFormat.format(Date()), latitude, longitude
            )
            val today = dateFormat.format(Date())
            val data = response.data
            appPreferences.setTimezone(data.meta.timezone)
            val prayerTime = PrayerTime(
                date = today,
                fajr = data.timings.fajr,
                dhuhr = data.timings.dhuhr,
                asr = data.timings.asr,
                maghrib = data.timings.maghrib,
                isha = data.timings.isha,
                latitude = latitude,
                longitude = longitude,
                cityName = "Lokasi GPS"
            )
            prayerTimeDao.insertPrayerTime(prayerTime)
            Result.success(prayerTime)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchAndSavePrayerTimeByCityInternal(
        provinsi: String,
        kabkota: String
    ): Result<PrayerTime> {
        return try {
            val now = Calendar.getInstance()
            val bulan = now.get(Calendar.MONTH) + 1
            val tahun = now.get(Calendar.YEAR)
            val tanggal = now.get(Calendar.DAY_OF_MONTH)

            val response = equranApi.getJadwalShalat(
                JadwalRequest(provinsi = provinsi, kabkota = kabkota, bulan = bulan, tahun = tahun)
            )

            val todaySchedule = response.data.jadwal.firstOrNull { it.tanggal == tanggal }
                ?: response.data.jadwal.firstOrNull()
                ?: return Result.failure(Exception("Jadwal hari ini tidak ditemukan"))

            val today = dateFormat.format(Date())
            val prayerTime = PrayerTime(
                date = today,
                fajr = todaySchedule.subuh,
                dhuhr = todaySchedule.dzuhur,
                asr = todaySchedule.ashar,
                maghrib = todaySchedule.maghrib,
                isha = todaySchedule.isya,
                latitude = 0.0,
                longitude = 0.0,
                cityName = "$kabkota, $provinsi"
            )

            prayerTimeDao.insertPrayerTime(prayerTime)
            Result.success(prayerTime)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAndSavePrayerTimeByCity(city: String, country: String = "Indonesia"): Result<PrayerTime> {
        return fetchAndSavePrayerTime("Jawa Barat", city)
    }

    suspend fun cleanupOldData() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val oldDate = dateFormat.format(calendar.time)
        prayerTimeDao.deleteOldPrayerTimes(oldDate)
    }
}
