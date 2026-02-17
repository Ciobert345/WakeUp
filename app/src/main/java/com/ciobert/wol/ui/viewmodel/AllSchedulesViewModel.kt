package com.ciobert.wol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciobert.wol.data.local.entity.ScheduleEntity
import com.ciobert.wol.data.repository.PcRepository
import com.ciobert.wol.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduleWithDetail(
    val schedule: ScheduleEntity,
    val pcName: String
)

@HiltViewModel
class AllSchedulesViewModel @Inject constructor(
    private val pcRepository: PcRepository,
    private val scheduleRepository: ScheduleRepository,
    private val scheduleManager: com.ciobert.wol.worker.ScheduleManager
) : ViewModel() {

    val uiState: StateFlow<List<ScheduleWithDetail>> = combine(
        pcRepository.allPcs,
        scheduleRepository.getAllSchedules()
    ) { pcs, schedules ->
        val pcMap = pcs.associateBy { it.id }
        
        schedules.mapNotNull { schedule ->
            val pc = pcMap[schedule.pcId]
            if (pc != null) {
                ScheduleWithDetail(schedule, pc.name)
            } else {
                null // Skip schedules for deleted PCs? Or show "Unknown Device"?
            }
        }.sortedBy { it.schedule.timeHour * 60 + it.schedule.timeMinute }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Actions
    fun toggleSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            scheduleRepository.updateSchedule(schedule.copy(enabled = !schedule.enabled))
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            // Cancel the alarm first
            scheduleManager.cancelSchedule(schedule.id)
            // Then delete from DB
            scheduleRepository.deleteSchedule(schedule)
        }
    }

    fun updateSchedule(schedule: ScheduleEntity, hour: Int, minute: Int, days: Int) {
        viewModelScope.launch {
            scheduleRepository.updateSchedule(
                schedule.copy(
                    timeHour = hour,
                    timeMinute = minute,
                    daysBitmap = days,
                    enabled = true // Re-enable on edit
                )
            )
        }
    }
}
