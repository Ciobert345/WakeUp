package com.ciobert.wol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciobert.wol.data.repository.AppTheme
import com.ciobert.wol.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import com.ciobert.wol.data.repository.UpdateRepository
import com.ciobert.wol.util.UpdateManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    private val updateRepository: UpdateRepository,
    private val updateManager: UpdateManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            android.util.Log.d("MainViewModel", "Starting update check...")
            // Check for updates
            // Hardcoded version for now, ideally inject BuildConfig.VERSION_NAME
            val currentVersion = "2.2.0" 
            android.util.Log.d("MainViewModel", "Current version: $currentVersion")
            val release = updateRepository.checkForUpdate(currentVersion)
            android.util.Log.d("MainViewModel", "Update check result: ${if (release != null) "Update found: ${release.tagName}" else "No update available"}")
            if (release != null) {
                // Determine the correct asset
                val asset = release.assets.find { it.name.endsWith(".apk") } ?: release.assets.firstOrNull()
                android.util.Log.d("MainViewModel", "Asset found: ${asset?.name ?: "none"}")
                if (asset != null) {
                    android.util.Log.d("MainViewModel", "Showing update notification for version ${release.tagName}")
                    updateManager.showUpdateNotification(release.tagName, asset.downloadUrl)
                } else {
                    android.util.Log.w("MainViewModel", "No APK asset found in release")
                }
            }
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

    fun startDownload(url: String, version: String) {
        android.util.Log.d("MainViewModel", "startDownload called with url: $url, version: $version")
        updateManager.downloadAndInstall(url, "WakeUp-$version.apk")
    }
}
