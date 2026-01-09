package com.voicerobot.ui.robot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerobot.lottie.AgentAnimMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /**
     * Temp function for UI testing: cycle to the next phase.
     */
    fun cyclePhase() {
        viewModelScope.launch {
            _uiState.update {
                val nextPhase = when (it.phase) {
                    AgentAnimMapper.Phase.STANDBY -> AgentAnimMapper.Phase.THINKING
                    AgentAnimMapper.Phase.THINKING -> AgentAnimMapper.Phase.RESPONSE
                    AgentAnimMapper.Phase.RESPONSE -> AgentAnimMapper.Phase.STANDBY
                }
                it.copy(phase = nextPhase)
            }
        }
    }
}

