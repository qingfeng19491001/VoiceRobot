package com.voicerobot.ui.video

import android.widget.FrameLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerobot.BuildConfig
import com.voicerobot.core.network.StartVoiceChatApi
import com.voicerobot.core.network.StartVoiceChatRequest
import com.voicerobot.rtc.RtcClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VideoViewModel(
    private val rtcClient: RtcClient,
    private val startVoiceChatApi: StartVoiceChatApi,
) : ViewModel() {

    data class UiState(
        val started: Boolean = false,
        val lastError: String? = null,
        val startVoiceChatResult: String? = null,
        val startVoiceChatRequestId: String? = null,
        val torchEnabled: Boolean? = null,
        val cameraSwitched: Boolean? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun initializeRtc() {
        try {
            rtcClient.init()
        } catch (t: Throwable) {
            _uiState.value = _uiState.value.copy(lastError = t.message ?: t.toString())
        }
    }

    fun bindLocalPreview(container: FrameLayout) {
        try {
            rtcClient.bindLocalPreview(container)
        } catch (t: Throwable) {
            _uiState.value = _uiState.value.copy(lastError = t.message ?: t.toString())
        }
    }

    fun onTorchClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ok = rtcClient.toggleTorch()
                _uiState.value = _uiState.value.copy(torchEnabled = ok)
                if (!ok) {
                    _uiState.value = _uiState.value.copy(lastError = "手电筒不可用（可能不支持或未初始化/未后置摄像头）")
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(lastError = t.message ?: t.toString())
            }
        }
    }

    fun onSwitchCameraClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ok = rtcClient.switchCamera()
                _uiState.value = _uiState.value.copy(cameraSwitched = ok)
                if (!ok) {
                    _uiState.value = _uiState.value.copy(lastError = "切换摄像头失败（可能未初始化）")
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(lastError = t.message ?: t.toString())
            }
        }
    }

    fun joinRoomAndStartAi() {
        if (_uiState.value.started) return
        _uiState.value = _uiState.value.copy(started = true)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                rtcClient.joinRoom(
                    roomId = BuildConfig.RTC_ROOM_ID,
                    userId = BuildConfig.RTC_USER_ID,
                    token = BuildConfig.RTC_TOKEN,
                )

                val req = buildStartVoiceChatRequest()
                val resp = startVoiceChatApi.startVoiceChat(req)
                _uiState.value = _uiState.value.copy(
                    startVoiceChatResult = resp.result,
                    startVoiceChatRequestId = resp.requestId,
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(lastError = t.message ?: t.toString())
            }
        }
    }

    private fun buildStartVoiceChatRequest(): StartVoiceChatRequest {
        val asrParams = mapOf(
            "Mode" to "bigmodel",
            "AppId" to BuildConfig.AI_ASR_APP_ID,
            "AccessToken" to BuildConfig.AI_ASR_ACCESS_TOKEN,
            "ApiResourceId" to BuildConfig.AI_ASR_API_RESOURCE_ID,
            "StreamMode" to 2,
        )

        val ttsParams = mapOf(
            "app" to mapOf(
                "appid" to BuildConfig.AI_TTS_APP_ID,
                "token" to BuildConfig.AI_TTS_ACCESS_TOKEN,
            ),
            "audio" to mapOf(
                "voice_type" to BuildConfig.AI_TTS_VOICE_TYPE,
                "speech_rate" to BuildConfig.AI_TTS_SPEECH_RATE,
            ),
            "ResourceId" to "volc.service_type.10029",
        )

        val llm = StartVoiceChatRequest.LlmConfig(
            mode = "ArkV3",
            endPointId = BuildConfig.AI_LLM_ENDPOINT_ID,
            systemMessages = listOf(BuildConfig.AI_SYSTEM_MESSAGES),
            visionConfig = StartVoiceChatRequest.LlmConfig.VisionConfig(enable = BuildConfig.AI_LLM_VISION_ENABLE),
            thinkingType = BuildConfig.AI_LLM_THINKING_TYPE,
        )

        return StartVoiceChatRequest(
            appId = BuildConfig.RTC_APP_ID,
            roomId = BuildConfig.RTC_ROOM_ID,
            taskId = BuildConfig.AI_TASK_ID,
            config = StartVoiceChatRequest.Config(
                asrConfig = StartVoiceChatRequest.ProviderConfig(
                    provider = "volcano",
                    providerParams = asrParams,
                ),
                ttsConfig = StartVoiceChatRequest.ProviderConfig(
                    provider = "volcano_bidirection",
                    providerParams = ttsParams,
                ),
                llmConfig = llm,
                interruptMode = 0,
            ),
            agentConfig = StartVoiceChatRequest.AgentConfig(
                targetUserId = listOf(BuildConfig.RTC_USER_ID),
                welcomeMessage = BuildConfig.AI_WELCOME_MESSAGE,
                userId = BuildConfig.AI_AGENT_USER_ID,
                enableConversationStateCallback = true,
            ),
        )
    }

    override fun onCleared() {
        super.onCleared()
        rtcClient.leaveRoom()
        rtcClient.release()
    }
}
