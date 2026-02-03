package com.voicerobot.ui.robot

import com.voicerobot.lottie.AgentAnimMapper

data class MainUiState(
    val phase: AgentAnimMapper.Phase = AgentAnimMapper.Phase.STANDBY,
    val isEngineRunning: Boolean = false,
    
    val amplitude01: Float = 0f,
)
