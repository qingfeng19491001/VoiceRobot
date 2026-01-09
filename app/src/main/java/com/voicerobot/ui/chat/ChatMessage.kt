package com.voicerobot.ui.chat

enum class Speaker {
    USER,
    BOT,
}

data class ChatMessage(
    val speaker: Speaker,
    val text: String,
)
