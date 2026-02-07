package com.gemini.wol.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.wol.data.local.entity.PcEntity
import com.gemini.wol.data.repository.PcRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditPcViewModel @Inject constructor(
    private val pcRepository: PcRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pcId: String? = savedStateHandle["pcId"]
    
    private val _uiState = MutableStateFlow(PcUiState())
    val uiState: StateFlow<PcUiState> = _uiState

    init {
        if (pcId != null && pcId != "new") {
            viewModelScope.launch {
                pcRepository.getPcById(pcId)?.let { pc ->
                    _uiState.value = PcUiState(
                        name = pc.name,
                        mac = pc.mac,
                        internalIp = pc.internalIp ?: "",
                        internalPort = pc.internalPort.toString(),
                        externalIp = pc.externalIp ?: "",
                        externalPort = pc.externalPort.toString(),
                        statusPort = pc.statusCheckPort?.toString() ?: "",
                        isRelay = pc.useRelay
                    )
                }
            }
        }
    }

    fun onEvent(event: AddEditPcEvent) {
        when(event) {
            is AddEditPcEvent.NameChanged -> _uiState.value = _uiState.value.copy(name = event.name)
            is AddEditPcEvent.MacChanged -> _uiState.value = _uiState.value.copy(mac = event.mac)
            is AddEditPcEvent.InternalIpChanged -> _uiState.value = _uiState.value.copy(internalIp = event.ip)
            is AddEditPcEvent.InternalPortChanged -> _uiState.value = _uiState.value.copy(internalPort = event.port)
            is AddEditPcEvent.ExternalIpChanged -> _uiState.value = _uiState.value.copy(externalIp = event.ip)
            is AddEditPcEvent.ExternalPortChanged -> _uiState.value = _uiState.value.copy(externalPort = event.port)
            is AddEditPcEvent.StatusPortChanged -> _uiState.value = _uiState.value.copy(statusPort = event.port)
            is AddEditPcEvent.RelayChanged -> _uiState.value = _uiState.value.copy(isRelay = event.isRelay)
            is AddEditPcEvent.Save -> savePc()
        }
    }

    private fun savePc() {
        val currentState = _uiState.value
        var isValid = true
        var nameError: String? = null
        var macError: String? = null
        // Simple validation, can be improved
        
        if (currentState.name.isBlank()) {
            nameError = "Name cannot be empty"
            isValid = false
        }

        if (currentState.mac.isBlank()) {
            macError = "MAC Address cannot be empty"
            isValid = false
        } else if (!currentState.mac.matches(Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"))) {
            macError = "Invalid MAC Address format (XX:XX:XX:XX:XX:XX)"
            isValid = false
        }

        // Ports validation
        val intPort = currentState.internalPort.toIntOrNull()
        if (intPort == null || intPort !in 1..65535) isValid = false
        
        val extPort = currentState.externalPort.toIntOrNull()
        if (extPort == null || extPort !in 1..65535) isValid = false

        if (!isValid) {
            _uiState.value = _uiState.value.copy(
                nameError = nameError,
                macError = macError
            )
            return
        }

        viewModelScope.launch {
            val pc = PcEntity(
                id = if (pcId != null && pcId != "new") pcId else java.util.UUID.randomUUID().toString(),
                name = currentState.name,
                mac = currentState.mac,
                internalIp = currentState.internalIp.ifBlank { null },
                internalPort = intPort ?: 9,
                externalIp = currentState.externalIp.ifBlank { null },
                externalPort = extPort ?: 9,
                statusCheckPort = currentState.statusPort.toIntOrNull(),
                useRelay = currentState.isRelay
            )
            if (pcId != null && pcId != "new") {
                pcRepository.updatePc(pc)
            } else {
                pcRepository.insertPc(pc)
            }
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }
}

data class PcUiState(
    val name: String = "",
    val mac: String = "",
    val internalIp: String = "",
    val internalPort: String = "9",
    val externalIp: String = "",
    val externalPort: String = "9",
    val statusPort: String = "80",
    val isRelay: Boolean = false,
    val saved: Boolean = false,
    val nameError: String? = null,
    val macError: String? = null
)

sealed class AddEditPcEvent {
    data class NameChanged(val name: String): AddEditPcEvent()
    data class MacChanged(val mac: String): AddEditPcEvent()
    data class InternalIpChanged(val ip: String): AddEditPcEvent()
    data class InternalPortChanged(val port: String): AddEditPcEvent()
    data class ExternalIpChanged(val ip: String): AddEditPcEvent()
    data class ExternalPortChanged(val port: String): AddEditPcEvent()
    data class StatusPortChanged(val port: String): AddEditPcEvent()
    data class RelayChanged(val isRelay: Boolean): AddEditPcEvent()
    object Save: AddEditPcEvent()
}
