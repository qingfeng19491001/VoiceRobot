package com.voicerobot.voiceengine.api

import com.voicerobot.voiceengine.model.VoiceConfig
import com.voicerobot.voiceengine.model.VoiceEvent
import kotlinx.coroutines.flow.Flow

interface VoiceEngineRepository {
    val events: Flow<VoiceEvent>

    
    fun prepare()

    
    fun init(config: VoiceConfig)

    
    fun startEngine(startJson: String = "{\"dialog\":{\"bot_name\":\"豆包\"}}")

    
    fun stopEngine()

    
    fun release()

    
    fun sayHello(content: String)

    
    fun chatTextQuery(content: String)
}

