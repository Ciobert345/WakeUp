package com.ciobert.wol.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciobert.wol.data.local.entity.PcEntity
import com.ciobert.wol.data.local.entity.ScheduleEntity
import com.ciobert.wol.data.repository.PcRepository
import com.ciobert.wol.data.repository.ScheduleRepository
import com.ciobert.wol.worker.ScheduleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val pcRepository: PcRepository,
    private val scheduleManager: ScheduleManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pcId: String = checkNotNull(savedStateHandle["pcId"])
    
    // We fetch the PC to ensure it exists and get its name if needed
    private val _pc = MutableStateFlow<PcEntity?>(null)
    val pc: StateFlow<PcEntity?> = _pc

    val schedules: StateFlow<List<ScheduleEntity>> = scheduleRepository.getSchedulesForPc(pcId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            _pc.value = pcRepository.getPcById(pcId)
        }
    }

    fun addSchedule(hour: Int, minute: Int, daysBitmap: Int) {
        val currentPc = _pc.value ?: return
        viewModelScope.launch {
            val schedule = ScheduleEntity(
                pcId = pcId,
                timeHour = hour,
                timeMinute = minute,
                daysBitmap = daysBitmap,
                enabled = true
            )
            scheduleRepository.insertSchedule(schedule)
            scheduleManager.scheduleNextWake(currentPc, schedule.id, hour, minute, daysBitmap)
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            scheduleRepository.deleteSchedule(schedule)
            scheduleManager.cancelSchedule(schedule.id)
        }
    }

    fun toggleSchedule(schedule: ScheduleEntity) {
        val currentPc = _pc.value ?: return
        viewModelScope.launch {
             val newEnabled = !schedule.enabled
             val updated = schedule.copy(enabled = newEnabled)
             scheduleRepository.updateSchedule(updated)
             
             if (newEnabled) {
                 scheduleManager.scheduleNextWake(currentPc, updated.id, updated.timeHour, updated.timeMinute, updated.daysBitmap)
             } else {
                 scheduleManager.cancelSchedule(updated.id)
             }
        }
    }

    fun updateSchedule(schedule: ScheduleEntity, newHour: Int, newMinute: Int, newDaysBitmap: Int) {
        val currentPc = _pc.value ?: return
        viewModelScope.launch {
            // 1. Cancel old alarm
            scheduleManager.cancelSchedule(schedule.id)
            
            // 2. Update DB
            val updated = schedule.copy(
                timeHour = newHour,
                timeMinute = newMinute,
                daysBitmap = newDaysBitmap,
                enabled = true // Re-enable on edit usually
            )
            scheduleRepository.updateSchedule(updated)
            
            // 3. Schedule new alarm
            scheduleManager.scheduleNextWake(currentPc, updated.id, newHour, newMinute, newDaysBitmap)
        }
    }
}
