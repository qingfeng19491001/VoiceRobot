package com.voicerobot.core.network

data class StartVoiceChatRequest(
    val appId: String,
    val roomId: String,
    val taskId: String,
    val config: Config,
    val agentConfig: AgentConfig,
) {
    data class Config(
        val asrConfig: ProviderConfig? = null,
        val ttsConfig: ProviderConfig? = null,
        val llmConfig: LlmConfig? = null,
        val interruptMode: Int? = null,
    )

    data class ProviderConfig(
        val provider: String,
        val providerParams: Any,
    )

    data class LlmConfig(
        val mode: String,
        val endPointId: String,
        val systemMessages: List<String>? = null,
        val visionConfig: VisionConfig? = null,
        val thinkingType: String? = null,
        val maxTokens: Int? = null,
        val temperature: Double? = null,
        val topP: Double? = null,
        val historyLength: Int? = null,
        val userPrompts: List<UserPrompt>? = null,
    ) {
        data class VisionConfig(
            val enable: Boolean = false,
        )

        data class UserPrompt(
            val role: String,
            val content: String,
        )
    }

    data class AgentConfig(
        val targetUserId: List<String>,
        val welcomeMessage: String? = null,
        val userId: String,
        val enableConversationStateCallback: Boolean? = null,
    )
}

