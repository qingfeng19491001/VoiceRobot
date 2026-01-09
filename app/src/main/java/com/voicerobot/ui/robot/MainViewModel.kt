package com.voicerobot.ui.robot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerobot.lottie.AgentAnimMapper
import com.voicerobot.ui.chat.ChatMessage
import com.voicerobot.ui.chat.Speaker
import com.voicerobot.voiceengine.api.VoiceEngineRepository
import com.voicerobot.voiceengine.model.VoiceConfig
import com.voicerobot.voiceengine.model.VoiceEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val voiceEngine: VoiceEngineRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private var started = false
    private var pendingUserUtterance: String = ""

    fun startIfNeeded() {
        if (started) return
        started = true

        voiceEngine.prepare()
        voiceEngine.init(
            VoiceConfig(
                appId = "8115608739",
                appKey = "PlgvMymc7f3tQnJ6",
                accessToken = "27XpxLIs6Wmk2FdAieY61k7VCRhc4MfE",
            )
        )

        voiceEngine.events
            .onEach { event ->
                when (event) {
                    VoiceEvent.EngineStarted -> {
                        _uiState.update { it.copy(isEngineRunning = true) }
                        voiceEngine.sayHello("你好呀，我是AI语音交互机器人")
                    }
                    VoiceEvent.EngineStopped -> {
                        _uiState.update { it.copy(isEngineRunning = false, phase = AgentAnimMapper.Phase.STANDBY) }
                    }
                    VoiceEvent.AsrStarted -> {
                        pendingUserUtterance = ""
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.THINKING) }
                    }
                    is VoiceEvent.AsrText -> {
                        val parsed = SpeechPayloadParser.extractBestText(event.text)
                        if (parsed.isNotEmpty()) {
                            pendingUserUtterance = parsed
                        }
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.THINKING) }
                    }
                    VoiceEvent.AsrEnded -> {
                        val finalText = pendingUserUtterance.trim()
                        if (finalText.isNotEmpty()) {
                            appendMessage(ChatMessage(Speaker.USER, finalText))
                        }
                        pendingUserUtterance = ""
                    }
                    is VoiceEvent.ChatText -> {
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.RESPONSE) }
                        val parsed = SpeechPayloadParser.extractBestText(event.text)
                        appendOrUpdateBotMessage(parsed)
                    }
                    VoiceEvent.ChatEnded -> {
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.STANDBY) }
                    }
                    is VoiceEvent.Volume -> {
                        val a = event.amplitude.coerceIn(0f, 1f)
                        val boosted = if (a < 0.02f) 0.02f else (a * 1.8f).coerceIn(0f, 1f)
                        _uiState.update { it.copy(amplitude01 = boosted) }
                    }
                    is VoiceEvent.Error -> {
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.STANDBY) }
                        appendMessage(ChatMessage(Speaker.BOT, "[error] ${event.code ?: ""} ${event.message}"))
                    }
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.Main.immediate) {
            voiceEngine.startEngine()
        }
    }

    private fun appendMessage(message: ChatMessage) {
        if (message.text.isBlank()) return
        _chatMessages.update { it + message }
    }

    private fun appendOrUpdateBotMessage(textChunk: String) {
        if (textChunk.isBlank()) return
        _chatMessages.update { list ->
            val last = list.lastOrNull()
            if (last?.speaker == Speaker.BOT) {
                // Append to the last bot message for streaming effect
                val updatedMessage = last.copy(text = last.text + textChunk)
                list.dropLast(1) + updatedMessage
            } else {
                // Start a new bot message
                list + ChatMessage(Speaker.BOT, textChunk)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            voiceEngine.stopEngine()
        } finally {
            voiceEngine.release()
        }
    }
}
