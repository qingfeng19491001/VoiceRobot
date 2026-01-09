package com.voicerobot.ui.robot

import com.voicerobot.lottie.AgentAnimMapper

data class MainUiState(
    val phase: AgentAnimMapper.Phase = AgentAnimMapper.Phase.STANDBY,
)

