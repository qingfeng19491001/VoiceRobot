package com.voicerobot.ui.robot

import org.json.JSONObject

/**
 * 解析 SDK 回调的 strData（通常是 JSON），提取更干净的展示文本。
 *
 * 不同消息类型字段可能不同，所以这里做“尽量提取”，失败就回退原始字符串。
 */
object SpeechPayloadParser {

    fun extractBestText(raw: String): String {
        val s = raw.trim()
        if (s.isEmpty()) return ""
        if (!s.startsWith("{") && !s.startsWith("[")) return s

        return try {
            val json = JSONObject(s)

            // 常见字段兜底顺序（按可能性从高到低）
            when {
                json.has("text") -> json.optString("text")
                json.has("result") -> json.optString("result")
                json.has("content") -> json.optString("content")
                json.has("sentence") -> json.optString("sentence")
                json.has("message") -> json.optString("message")
                else -> s
            }.trim().ifEmpty { s }
        } catch (_: Throwable) {
            // 有些回调可能不是严格 JSON
            s
        }
    }
}

