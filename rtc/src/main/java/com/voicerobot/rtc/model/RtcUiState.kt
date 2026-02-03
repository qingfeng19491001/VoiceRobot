package com.voicerobot.rtc.model

data class RtcUiState(
    val connectionState: RtcConnectionState = RtcConnectionState.Idle,
    val remoteVideoStreamId: String? = null,
    val lastError: String? = null,
)

