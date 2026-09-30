package com.focussholat.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.focussholat.data.entity.CustomSchedule
import com.focussholat.data.repository.CustomScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val customScheduleRepository: CustomScheduleRepository
) : ViewModel() {

    val schedules = customScheduleRepository.getAllSchedules().asLiveData()

    fun addSchedule(schedule: CustomSchedule) {
        viewModelScope.launch {
            customScheduleRepository.insertSchedule(schedule)
        }
    }

    fun updateSchedule(schedule: CustomSchedule) {
        viewModelScope.launch {
            customScheduleRepository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: CustomSchedule) {
        viewModelScope.launch {
            customScheduleRepository.deleteSchedule(schedule)
        }
    }

    fun toggleSchedule(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            customScheduleRepository.setEnabled(id, enabled)
        }
    }
}
