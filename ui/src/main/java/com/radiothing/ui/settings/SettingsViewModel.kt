package com.radiothing.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radiothing.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings = settingsRepository.getSettings()

    fun setCrossfadeDuration(duration: Int) {
        viewModelScope.launch { settingsRepository.updateCrossfadeDuration(duration) }
    }
    
    fun setBufferSize(size: Int) {
        viewModelScope.launch { settingsRepository.updateBufferSize(size) }
    }

    fun setUseAsciiNotification(useAscii: Boolean) {
        viewModelScope.launch { settingsRepository.updateUseAsciiNotification(useAscii) }
    }

    fun setEnableCache(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateEnableCache(enabled) }
    }

    fun setEnablePreWarm(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateEnablePreWarm(enabled) }
    }
}
