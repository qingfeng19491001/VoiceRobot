package com.voicerobot.lottie

import androidx.annotation.RawRes
import com.voicerobot.lottie.R


object AgentAnimMapper {

    enum class Phase {
        STANDBY,
        THINKING,
        RESPONSE,
    }

    @RawRes
    fun animRes(phase: Phase): Int {
        return when (phase) {
            Phase.STANDBY -> R.raw.bot_standby
            Phase.THINKING -> R.raw.bot_thinking
            Phase.RESPONSE -> R.raw.bot_response
        }
    }
}

