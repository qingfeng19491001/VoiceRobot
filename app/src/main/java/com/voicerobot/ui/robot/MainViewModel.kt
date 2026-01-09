package com.voicerobot.ui.robot

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

    fun startIfNeeded() {
        if (started) return
        started = true

        voiceEngine.prepare()
        voiceEngine.init(
            VoiceConfig(
                appId = "8115608739",
                // 临时用服务端返回的 expected 值验证（后续请用控制台真实 AppKey 替换/迁移到配置）
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
                        _uiState.update {
                            it.copy(
                                isEngineRunning = false,
                                phase = AgentAnimMapper.Phase.STANDBY,
                            )
                        }
                    }

                    VoiceEvent.AsrStarted -> {
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.THINKING) }
                    }

                    is VoiceEvent.AsrText -> {
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.THINKING) }
                        appendOrReplaceLastUserText(SpeechPayloadParser.extractBestText(event.text))
                    }

                    VoiceEvent.AsrEnded -> {
                        // 不立刻回待机，等待 Chat 回复开始/结束
                    }

                    is VoiceEvent.ChatText -> {
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.RESPONSE) }
                        appendOrReplaceLastBotText(SpeechPayloadParser.extractBestText(event.text))
                    }

                    VoiceEvent.ChatEnded -> {
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.STANDBY) }
                    }

                    is VoiceEvent.Volume -> {
                        val a = event.amplitude.coerceIn(0f, 1f)
                        val boosted = if (a < 0.02f) 0.02f else (a * 1.8f).coerceIn(0f, 1f)
                        _uiState.update { it.copy(amplitude01 = boosted) }
                    }

                    is VoiceEvent.Debug -> {
                        appendOrReplaceLastBotText("[debug] ${event.message}")
                    }

                    is VoiceEvent.Error -> {
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.STANDBY) }
                        appendOrReplaceLastBotText("[error] ${event.code ?: ""} ${event.message}")
                    }
                }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.Main.immediate) {
            voiceEngine.startEngine()
        }
    }

    private fun appendOrReplaceLastUserText(text: String) {
        if (text.isBlank()) return
        _chatMessages.update { list ->
            val last = list.lastOrNull()
            if (last?.speaker == Speaker.USER) {
                list.dropLast(1) + last.copy(text = text)
            } else {
                list + ChatMessage(Speaker.USER, text)
            }
        }
    }

    private fun appendOrReplaceLastBotText(text: String) {
        if (text.isBlank()) return
        _chatMessages.update { list ->
            val last = list.lastOrNull()
            if (last?.speaker == Speaker.BOT) {
                list.dropLast(1) + last.copy(text = text)
            } else {
                list + ChatMessage(Speaker.BOT, text)
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
