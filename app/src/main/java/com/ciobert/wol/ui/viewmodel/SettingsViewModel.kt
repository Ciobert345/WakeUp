package com.ciobert.wol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciobert.wol.data.repository.AppTheme
import com.ciobert.wol.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.ciobert.wol.data.local.entity.PcEntity
import com.ciobert.wol.data.repository.PcRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val pcRepository: PcRepository
) : ViewModel() {

    private val gson = Gson()

    data class ExportData(
        val pcs: List<PcEntity>,
        val schedules: List<com.ciobert.wol.data.local.entity.ScheduleEntity>
    )

    suspend fun exportDevices(): String {
        val pcs = pcRepository.allPcs.first()
        val schedules = pcRepository.allSchedules.first()
        return gson.toJson(ExportData(pcs, schedules))
    }

    suspend fun importDevices(json: String): Boolean {
        return try {
            // Support both old format (List<PcEntity>) and new format (ExportData)
            try {
                val exportData = gson.fromJson(json, ExportData::class.java)
                if (exportData.pcs != null) {
                    pcRepository.insertPcs(exportData.pcs)
                    if (exportData.schedules != null) {
                        pcRepository.insertSchedules(exportData.schedules)
                        pcRepository.rescheduleAll()
                    }
                    return true
                }
            } catch (e: Exception) {
                // Fallback to old format
                val type = object : TypeToken<List<PcEntity>>() {}.type
                val pcs: List<PcEntity> = gson.fromJson(json, type)
                pcRepository.insertPcs(pcs)
                return true
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    val currentTheme: StateFlow<AppTheme> = userPreferencesRepository.appTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    val accentColor: StateFlow<Int> = userPreferencesRepository.accentColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0xFF6200EE.toInt()
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.setAppTheme(theme)
        }
    }

    fun setAccentColor(color: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setAccentColor(color)
        }
    }
}
