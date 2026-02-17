package com.ciobert.wol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciobert.wol.data.local.entity.PcEntity
import com.ciobert.wol.data.repository.PcRepository
import com.ciobert.wol.domain.usecase.CheckPcStatusUseCase
import com.ciobert.wol.domain.usecase.SendWakePacketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val pcRepository: PcRepository,
    private val sendWakePacketUseCase: SendWakePacketUseCase,
    private val checkPcStatusUseCase: CheckPcStatusUseCase
) : ViewModel() {

    private val _pcs = MutableStateFlow<List<PcUiModel>>(emptyList())
    val pcs: StateFlow<List<PcUiModel>> = _pcs

    private val _wakeResult = MutableStateFlow<String?>(null)
    val wakeResult: StateFlow<String?> = _wakeResult

    init {
        viewModelScope.launch {
            pcRepository.allPcs.collect { entities ->
                val currentStatuses = _pcs.value.associate { it.pc.id to it.isOnline }
                val uiModels = entities.map { PcUiModel(it, currentStatuses[it.id]) }
                _pcs.value = uiModels
                
                // Trigger an immediate check for any new or unknown devices
                if (uiModels.any { it.isOnline == null }) {
                    refreshPcs()
                }
            }
        }
        
        // Periodic polling loop (every 60 seconds)
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60000)
                refreshPcs()
            }
        }
    }

    private fun checkAllStatuses(models: List<PcUiModel>) {
        viewModelScope.launch {
            // Run all checks in parallel
            val updatedList = models.map { model ->
                async {
                    val isOnline = checkPcStatusUseCase(model.pc)
                    model.copy(isOnline = isOnline)
                }
            }.awaitAll() // Wait for all to finish

            // Emit ONCE
            _pcs.value = updatedList
        }
    }
    
    fun refreshPcs() {
        val currentModels = _pcs.value
        if (currentModels.isNotEmpty()) {
            checkAllStatuses(currentModels)
        }
    }
    
    private fun updatePcStatus(pcId: String, isOnline: Boolean) {
        val currentList = _pcs.value
        _pcs.value = currentList.map { 
            if (it.pc.id == pcId) it.copy(isOnline = isOnline) else it
        }
    }

    fun wakePc(pc: PcEntity) {
        viewModelScope.launch {
            val result = sendWakePacketUseCase(pc.id)
            if (result.isSuccess) {
                _wakeResult.value = "Magic Packet sent to ${pc.name}"
                pcRepository.updateLastSeen(pc.id, System.currentTimeMillis())
            } else {
                _wakeResult.value = "Failed to send packet: ${result.exceptionOrNull()?.message}"
            }
        }
    }
    
    fun clearMessage() {
        _wakeResult.value = null
    }
    
    fun deletePc(pc: PcEntity) {
        viewModelScope.launch {
            pcRepository.deletePc(pc)
        }
    }
}

data class PcUiModel(
    val pc: PcEntity,
    val isOnline: Boolean? = null
)
