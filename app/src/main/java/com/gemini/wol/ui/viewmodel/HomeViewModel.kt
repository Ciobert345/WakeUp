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
    val activeSchedules: Int = 0,
    val missingPermissions: List<com.gemini.wol.util.PermissionsHelper.PermissionItem> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pcRepository: PcRepository,
    private val scheduleRepository: ScheduleRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _permissionRefreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(
        pcRepository.allPcs,
        scheduleRepository.getAllSchedules(),
        _permissionRefreshTrigger
    ) { pcs, schedules, _ ->
        HomeUiState(
            totalPcs = pcs.size,
            activeSchedules = schedules.filter { it.enabled }.size,
            missingPermissions = com.gemini.wol.util.PermissionsHelper.getMissingPermissions(context)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
    
    // We need context for PermissionsHelper, so we'll likely handle this 
    // in the UI or by passing context to a refresh method
    fun refreshPermissions() {
        _permissionRefreshTrigger.value += 1
    }
}
