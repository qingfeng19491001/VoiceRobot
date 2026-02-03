package com.voicerobot.core.network

object StartVoiceChatJson {

    fun encode(req: StartVoiceChatRequest): String {
        return buildString {
            append("{")
            append("\"AppId\":\"").append(escape(req.appId)).append("\"")
            append(",\"RoomId\":\"").append(escape(req.roomId)).append("\"")
            append(",\"TaskId\":\"").append(escape(req.taskId)).append("\"")
            append(",\"Config\":").append(encodeConfig(req.config))
            append(",\"AgentConfig\":").append(encodeAgent(req.agentConfig))
            append("}")
        }
    }

    private fun encodeConfig(c: StartVoiceChatRequest.Config): String {
        return buildString {
            append("{")
            var first = true
            fun comma() {
                if (first) first = false else append(",")
            }

            c.asrConfig?.let {
                comma(); append("\"ASRConfig\":").append(encodeProviderConfig(it))
            }
            c.ttsConfig?.let {
                comma(); append("\"TTSConfig\":").append(encodeProviderConfig(it))
            }
            c.llmConfig?.let {
                comma(); append("\"LLMConfig\":").append(encodeLlmConfig(it))
            }
            c.interruptMode?.let {
                comma(); append("\"InterruptMode\":").append(it)
            }

            append("}")
        }
    }

    private fun encodeProviderConfig(pc: StartVoiceChatRequest.ProviderConfig): String {
        return buildString {
            append("{")
            append("\"Provider\":\"").append(escape(pc.provider)).append("\"")
            append(",\"ProviderParams\":").append(encodeAny(pc.providerParams))
            append("}")
        }
    }

    private fun encodeLlmConfig(llm: StartVoiceChatRequest.LlmConfig): String {
        return buildString {
            append("{")
            append("\"Mode\":\"").append(escape(llm.mode)).append("\"")
            append(",\"EndPointId\":\"").append(escape(llm.endPointId)).append("\"")

            llm.maxTokens?.let { append(",\"MaxTokens\":").append(it) }
            llm.temperature?.let { append(",\"Temperature\":").append(it) }
            llm.topP?.let { append(",\"TopP\":").append(it) }
            llm.thinkingType?.let { append(",\"ThinkingType\":\"").append(escape(it)).append("\"") }
            llm.historyLength?.let { append(",\"HistoryLength\":").append(it) }

            llm.systemMessages?.let {
                append(",\"SystemMessages\":").append(encodeStringList(it))
            }
            llm.userPrompts?.let {
                append(",\"UserPrompts\":").append(encodeUserPrompts(it))
            }
            llm.visionConfig?.let {
                append(",\"VisionConfig\":{")
                append("\"Enable\":").append(if (it.enable) "true" else "false")
                append("}")
            }

            append("}")
        }
    }

    private fun encodeUserPrompts(prompts: List<StartVoiceChatRequest.LlmConfig.UserPrompt>): String {
        return buildString {
            append("[")
            prompts.forEachIndexed { idx, p ->
                if (idx > 0) append(",")
                append("{")
                append("\"Role\":\"").append(escape(p.role)).append("\"")
                append(",\"Content\":\"").append(escape(p.content)).append("\"")
                append("}")
            }
            append("]")
        }
    }

    private fun encodeAgent(agent: StartVoiceChatRequest.AgentConfig): String {
        return buildString {
            append("{")
            append("\"TargetUserId\":").append(encodeStringList(agent.targetUserId))
            append(",\"UserId\":\"").append(escape(agent.userId)).append("\"")
            agent.welcomeMessage?.let {
                append(",\"WelcomeMessage\":\"").append(escape(it)).append("\"")
            }
            agent.enableConversationStateCallback?.let {
                append(",\"EnableConversationStateCallback\":").append(if (it) "true" else "false")
            }
            append("}")
        }
    }

    private fun encodeStringList(list: List<String>): String {
        return buildString {
            append("[")
            list.forEachIndexed { idx, s ->
                if (idx > 0) append(",")
                append("\"").append(escape(s)).append("\"")
            }
            append("]")
        }
    }

    private fun encodeAny(value: Any): String {
        return when (value) {
            is String -> "\"${escape(value)}\""
            is Number -> value.toString()
            is Boolean -> if (value) "true" else "false"
            is Map<*, *> -> encodeMap(value)
            is List<*> -> encodeList(value)
            else -> "\"${escape(value.toString())}\""
        }
    }

    private fun encodeMap(map: Map<*, *>): String {
        return buildString {
            append("{")
            var first = true
            for ((k, v) in map) {
                val key = k?.toString() ?: continue
                if (v == null) continue
                if (!first) append(",") else first = false
                append("\"").append(escape(key)).append("\":").append(encodeAny(v))
            }
            append("}")
        }
    }

    private fun encodeList(list: List<*>): String {
        return buildString {
            append("[")
            list.forEachIndexed { idx, v ->
                if (v == null) return@forEachIndexed
                if (idx > 0) append(",")
                append(encodeAny(v))
            }
            append("]")
        }
    }

    private fun escape(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}

