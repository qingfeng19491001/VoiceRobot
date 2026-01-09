package com.voicerobot.ui.robot

import org.json.JSONArray
import org.json.JSONObject

/**
 * 将 SDK 回调 payload（通常是 JSON）提取为可读的自然语言文本。
 *
 * 规则：
 * - 如果是 JSON：尽量递归查找常见文本字段
 * - 如果找到的字段仍然是 JSON 字符串，则继续向内解析
 */
object SpeechPayloadParser {

    private val textKeys = listOf(
        // top-level common
        "text", "content", "result", "sentence", "message", "transcript",
        // volc common guesses
        "asr_text", "chat_text", "reply", "answer"
    )

    fun extractBestText(raw: String): String {
        val s = raw.trim()
        if (s.isEmpty()) return ""
        if (!s.startsWith("{") && !s.startsWith("[")) return s

        // object
        parseObjectSafely(s)?.let { obj ->
            return extractFromObject(obj).orEmpty().ifBlank { s }
        }

        // array
        parseArraySafely(s)?.let { arr ->
            for (i in 0 until arr.length()) {
                val item = arr.opt(i)
                when (item) {
                    is JSONObject -> extractFromObject(item)?.let { return it }
                    is String -> {
                        val t = extractBestText(item)
                        if (t.isNotBlank() && t != item.trim()) return t
                    }
                }
            }
        }

        return s
    }

    private fun extractFromObject(json: JSONObject): String? {
        // direct keys
        for (k in textKeys) {
            val v = json.optString(k, "").trim()
            if (v.isNotEmpty() && v != "null") {
                // if nested json string, keep digging
                if (v.startsWith("{") || v.startsWith("[")) {
                    val nested = extractBestText(v)
                    if (nested.isNotBlank() && nested != v) return nested
                }
                return v
            }
        }

        // openai-like streaming
        json.optJSONArray("choices")?.let { choices ->
            val first = choices.optJSONObject(0)
            if (first != null) {
                first.optJSONObject("delta")?.optString("content")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
                first.optString("text")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
            }
        }

        // alternatives[0].text
        json.optJSONArray("alternatives")?.let { alts ->
            val first = alts.optJSONObject(0)
            if (first != null) {
                first.optString("text")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
                first.optString("transcript")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
            }
        }

        // recursive search nested objects
        val names = json.keys()
        while (names.hasNext()) {
            val key = names.next()
            val v = json.opt(key)
            when (v) {
                is JSONObject -> extractFromObject(v)?.let { return it }
                is JSONArray -> {
                    for (i in 0 until v.length()) {
                        val item = v.opt(i)
                        if (item is JSONObject) extractFromObject(item)?.let { return it }
                        if (item is String) {
                            val t = extractBestText(item)
                            if (t.isNotBlank() && t != item.trim()) return t
                        }
                    }
                }
                is String -> {
                    val str = v.trim()
                    if (str.startsWith("{") || str.startsWith("[")) {
                        val t = extractBestText(str)
                        if (t.isNotBlank() && t != str) return t
                    }
                }
            }
        }

        return null
    }

    private fun parseObjectSafely(s: String): JSONObject? = try { JSONObject(s) } catch (_: Throwable) { null }
    private fun parseArraySafely(s: String): JSONArray? = try { JSONArray(s) } catch (_: Throwable) { null }
}
