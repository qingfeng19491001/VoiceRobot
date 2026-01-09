package com.voicerobot.voiceengine.model

sealed interface VoiceEvent {
    data object EngineStarted : VoiceEvent
    data object EngineStopped : VoiceEvent

    data object AsrStarted : VoiceEvent
    data class AsrText(val text: String) : VoiceEvent
    data object AsrEnded : VoiceEvent

    data class ChatText(val text: String) : VoiceEvent
    data object ChatEnded : VoiceEvent

    /** 0..1 amplitude derived from recorder PCM */
    data class Volume(val amplitude: Float) : VoiceEvent

    data class Debug(val message: String) : VoiceEvent

    data class Error(val code: Int? = null, val message: String) : VoiceEvent
}
