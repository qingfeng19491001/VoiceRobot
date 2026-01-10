package com.voicerobot.ui.robot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerobot.BuildConfig
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

    // 临时缓存：用户本轮说话的流式 ASR
    private var pendingUserUtterance: String = ""

    fun startIfNeeded() {
        if (started) return
        started = true

        voiceEngine.prepare()
        voiceEngine.init(
            VoiceConfig(
                appId = BuildConfig.VOLC_APP_ID,
                appKey = BuildConfig.VOLC_APP_KEY,
                accessToken = BuildConfig.VOLC_ACCESS_TOKEN,
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
                        pendingUserUtterance = ""
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.THINKING) }
                    }

                    is VoiceEvent.AsrText -> {
                        // 用户说话中：只缓存，不写入列表
                        pendingUserUtterance = SpeechPayloadParser.extractBestText(event.text)
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.THINKING) }
                    }

                    VoiceEvent.AsrEnded -> {
                        // 用户一句话结束：再把最终文本落到列表
                        val finalText = pendingUserUtterance.trim()
                        if (finalText.isNotEmpty()) {
                            appendUserFinal(finalText)
                        }
                        pendingUserUtterance = ""
                    }

                    is VoiceEvent.ChatText -> {
                        // 机器人回复：流式输出（append 到最后一条机器人消息）
                        _uiState.update { it.copy(phase = AgentAnimMapper.Phase.RESPONSE) }
                        appendBotStreaming(SpeechPayloadParser.extractBestText(event.text))
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
                        appendBotStreaming("[error] ${event.code ?: ""} ${event.message}")
                    }
                    else -> Unit // Exhaustive when
                }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.Main.immediate) {
            voiceEngine.startEngine()
        }
    }

    private fun appendUserFinal(text: String) {
        _chatMessages.update { list ->
            list + ChatMessage(Speaker.USER, text)
        }
    }

    private fun appendBotStreaming(text: String) {
        if (text.isBlank()) return
        _chatMessages.update { list ->
            val last = list.lastOrNull()
            if (last?.speaker == Speaker.BOT) {
                // 追加流式文本
                list.dropLast(1) + last.copy(text = (last.text + text).trim())
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
