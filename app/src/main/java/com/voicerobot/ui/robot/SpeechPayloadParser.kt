package com.voicerobot.ui.robot

import org.json.JSONArray
import org.json.JSONObject


object SpeechPayloadParser {

    private val textKeys = listOf(
        
        "text", "content", "result", "sentence", "message", "transcript",
        
        "asr_text", "chat_text", "reply", "answer"
    )

    fun extractBestText(raw: String): String {
        val s = raw.trim()
        if (s.isEmpty()) return ""
        if (!s.startsWith("{") && !s.startsWith("[")) return s

        
        parseObjectSafely(s)?.let { obj ->
            return extractFromObject(obj).orEmpty().ifBlank { s }
        }

        
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
        
        for (k in textKeys) {
            val v = json.optString(k, "").trim()
            if (v.isNotEmpty() && v != "null") {
                
                if (v.startsWith("{") || v.startsWith("[")) {
                    val nested = extractBestText(v)
                    if (nested.isNotBlank() && nested != v) return nested
                }
                return v
            }
        }

        
        json.optJSONArray("choices")?.let { choices ->
            val first = choices.optJSONObject(0)
            if (first != null) {
                first.optJSONObject("delta")?.optString("content")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
                first.optString("text")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
            }
        }

        
        json.optJSONArray("alternatives")?.let { alts ->
            val first = alts.optJSONObject(0)
            if (first != null) {
                first.optString("text")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
                first.optString("transcript")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
            }
        }

        
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
