package com.voicerobot.ui.robot

import org.json.JSONObject

/**
 * 将 SDK 回调 payload（通常是 JSON）提取为可读的自然语言文本。
 */
object SpeechPayloadParser {

    fun extractBestText(raw: String): String {
        val s = raw.trim()
        if (s.isEmpty()) return ""

        // 最高优先级规则：处理 `{"key":"val"}实际文本` 这种混合格式
        val lastBraceIndex = s.lastIndexOf('}')
        if (lastBraceIndex != -1 && lastBraceIndex < s.length - 1) {
            return s.substring(lastBraceIndex + 1).trim()
        }

        // 如果是纯 JSON，尝试解析，但如果只包含元数据（如 content:""），则返回空字符串
        if (s.startsWith("{") && s.endsWith("}")) {
            try {
                val json = JSONObject(s)
                if (json.has("content")) {
                    val content = json.optString("content", "").trim()
                    if (content.isNotEmpty()) {
                        return content
                    }
                }
                if (json.has("text")) {
                    val text = json.optString("text", "").trim()
                    if (text.isNotEmpty()) {
                        return text
                    }
                }
                // 是 JSON 但没有可显示的文本，返回空，避免显示元数据
                return ""
            } catch (_: Exception) {
                // 解析失败，可能不是一个完整的 JSON
            }
        }

        // 如果以上规则都不匹配，则假定为纯文本（流的后续部分）
        return s
    }
}
