package com.focussholat.data.dao

import androidx.room.*
import com.focussholat.data.entity.PrayerTime
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerTimeDao {

    @Query("SELECT * FROM prayer_times WHERE date = :date LIMIT 1")
    suspend fun getPrayerTimeByDate(date: String): PrayerTime?

    @Query("SELECT * FROM prayer_times ORDER BY date DESC LIMIT 1")
    suspend fun getLatestPrayerTime(): PrayerTime?

    @Query("SELECT * FROM prayer_times ORDER BY date DESC LIMIT 7")
    fun getRecentPrayerTimes(): Flow<List<PrayerTime>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTime(prayerTime: PrayerTime)

    @Query("DELETE FROM prayer_times WHERE date < :date")
    suspend fun deleteOldPrayerTimes(date: String)
}
