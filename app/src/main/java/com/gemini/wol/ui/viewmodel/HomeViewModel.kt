package com.gemini.wol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.wol.data.repository.PcRepository
import com.gemini.wol.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeUiState(
    val totalPcs: Int = 0,
    val activeSchedules: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    pcRepository: PcRepository,
    scheduleRepository: ScheduleRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        pcRepository.allPcs,
        scheduleRepository.getAllSchedules()
    ) { pcs, schedules ->
        HomeUiState(
            totalPcs = pcs.size,
            activeSchedules = schedules.filter { it.enabled }.size // Only count enabled schedules
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}
