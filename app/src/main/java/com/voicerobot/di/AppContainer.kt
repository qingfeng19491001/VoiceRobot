package com.voicerobot.di

import android.app.Application
import com.voicerobot.voiceengine.api.VoiceEngineRepository
import com.voicerobot.voiceengine.impl.VolcDialogVoiceEngineRepository

/**
 * App 层的“组装者”，负责把各模块拼起来。
 * 不引入 Hilt 的情况下，用这个最轻量。
 */
class AppContainer(app: Application) {
    val voiceEngineRepository: VoiceEngineRepository = VolcDialogVoiceEngineRepository(app)
}

