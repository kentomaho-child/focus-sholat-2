package com.focussholat.ui.stats

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.focussholat.data.entity.BlockEvent
import com.focussholat.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    val totalBlockCount = statsRepository.getTotalBlockCount().asLiveData()
    val totalFocusMinutes = statsRepository.getTotalFocusMinutes().asLiveData()
    val totalSessions = statsRepository.getTotalSessionCount().asLiveData()
    val recentEvents = statsRepository.getAllEvents().asLiveData()

    val blockCountToday = MutableLiveData(0)
    val focusMinutesWeek = MutableLiveData(0L)

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            blockCountToday.postValue(statsRepository.getBlockCountToday())
            focusMinutesWeek.postValue(statsRepository.getFocusMinutesThisWeek())
        }
    }
}
