package com.gemini.wol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.wol.data.repository.AppTheme
import com.gemini.wol.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.gemini.wol.data.local.entity.PcEntity
import com.gemini.wol.data.repository.PcRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val pcRepository: PcRepository
) : ViewModel() {

    private val gson = Gson()

    suspend fun exportDevices(): String {
        val pcs = pcRepository.allPcs.first()
        return gson.toJson(pcs)
    }

    suspend fun importDevices(json: String): Boolean {
        return try {
            val type = object : TypeToken<List<PcEntity>>() {}.type
            val pcs: List<PcEntity> = gson.fromJson(json, type)
            pcRepository.insertPcs(pcs)
            true
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
