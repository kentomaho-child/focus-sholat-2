package com.focussholat.data.repository

import com.focussholat.data.dao.CustomScheduleDao
import com.focussholat.data.entity.CustomSchedule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomScheduleRepository @Inject constructor(
    private val customScheduleDao: CustomScheduleDao
) {
    fun getAllSchedules(): Flow<List<CustomSchedule>> = customScheduleDao.getAllSchedules()

    fun getActiveSchedules(): Flow<List<CustomSchedule>> = customScheduleDao.getActiveSchedules()

    suspend fun getActiveSchedulesList(): List<CustomSchedule> =
        customScheduleDao.getActiveSchedulesList()

    suspend fun getScheduleById(id: Long): CustomSchedule? =
        customScheduleDao.getScheduleById(id)

    suspend fun insertSchedule(schedule: CustomSchedule): Long =
        customScheduleDao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: CustomSchedule) =
        customScheduleDao.updateSchedule(schedule)

    suspend fun deleteSchedule(schedule: CustomSchedule) =
        customScheduleDao.deleteSchedule(schedule)

    suspend fun deleteById(id: Long) = customScheduleDao.deleteById(id)

    suspend fun setEnabled(id: Long, enabled: Boolean) =
        customScheduleDao.setEnabled(id, enabled)
}
