package com.voicerobot.voiceengine.api

import com.voicerobot.voiceengine.model.VoiceConfig
import com.voicerobot.voiceengine.model.VoiceEvent
import kotlinx.coroutines.flow.Flow

interface VoiceEngineRepository {
    val events: Flow<VoiceEvent>

    /** Prepare environment. Must be called once per app process before init/start. */
    fun prepare()

    /** Create engine + set options + initEngine. */
    fun init(config: VoiceConfig)

    /** Start dialog engine (DIRECTIVE_START_ENGINE). */
    fun startEngine(startJson: String = "{\"dialog\":{\"bot_name\":\"豆包\"}}")

    /** Stop engine synchronously (DIRECTIVE_SYNC_STOP_ENGINE). */
    fun stopEngine()

    /** Destroy engine instance. */
    fun release()

    /** Optional: send SayHello event. */
    fun sayHello(content: String)

    /** Optional: send a text query (ChatTextQuery). */
    fun chatTextQuery(content: String)
}

