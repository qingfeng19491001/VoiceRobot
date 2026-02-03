package com.voicerobot.di

import android.app.Application
import com.voicerobot.BuildConfig
import com.voicerobot.core.network.StartVoiceChatApi
import com.voicerobot.core.network.StartVoiceChatApiFactory
import com.voicerobot.voiceengine.api.VoiceEngineRepository
import com.voicerobot.voiceengine.impl.VolcDialogVoiceEngineRepository


class AppContainer(app: Application) {
    val voiceEngineRepository: VoiceEngineRepository = VolcDialogVoiceEngineRepository(app)

    val startVoiceChatApi: StartVoiceChatApi = StartVoiceChatApiFactory.create(
        accessKeyId = BuildConfig.VOLC_ACCESS_KEY_ID,
        secretAccessKey = BuildConfig.VOLC_SECRET_ACCESS_KEY,
        sessionToken = BuildConfig.VOLC_SESSION_TOKEN.takeIf { it.isNotBlank() },
        region = "cn-north-1",
    )
}

