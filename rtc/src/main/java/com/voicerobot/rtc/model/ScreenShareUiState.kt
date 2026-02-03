package com.voicerobot.rtc.model

data class ScreenShareUiState(
    val state: ScreenShareState = ScreenShareState.Idle,
    val lastError: String? = null,
)

