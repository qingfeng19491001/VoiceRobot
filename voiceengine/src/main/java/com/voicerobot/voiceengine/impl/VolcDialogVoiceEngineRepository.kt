package com.voicerobot.voiceengine.impl

import android.app.Application
import android.content.Context
import android.util.Log
import com.bytedance.speech.speechengine.SpeechEngine
import com.bytedance.speech.speechengine.SpeechEngineDefines
import com.bytedance.speech.speechengine.SpeechEngineGenerator
import com.voicerobot.voiceengine.api.VoiceEngineRepository
import com.voicerobot.voiceengine.model.VoiceConfig
import com.voicerobot.voiceengine.model.VoiceEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.File
import kotlin.math.abs


class VolcDialogVoiceEngineRepository(
    private val app: Application,
) : VoiceEngineRepository, SpeechEngine.SpeechListener {

    private val _events = MutableSharedFlow<VoiceEvent>(
        replay = 0,
        extraBufferCapacity = 128,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val events: Flow<VoiceEvent> = _events

    private var engine: SpeechEngine? = null
    private var inited = false

    override fun prepare() {
        SpeechEngineGenerator.PrepareEnvironment(app.applicationContext, app)
    }

    override fun init(config: VoiceConfig) {
        if (inited) return

        val e = SpeechEngineGenerator.getInstance()
        e.createEngine()

        
        e.setOptionString(SpeechEngineDefines.PARAMS_KEY_ENGINE_NAME_STRING, SpeechEngineDefines.DIALOG_ENGINE)

        
        e.setOptionString(SpeechEngineDefines.PARAMS_KEY_APP_ID_STRING, config.appId)
        e.setOptionString(SpeechEngineDefines.PARAMS_KEY_APP_KEY_STRING, config.appKey)
        e.setOptionString(SpeechEngineDefines.PARAMS_KEY_APP_TOKEN_STRING, config.accessToken)

        e.setOptionString(SpeechEngineDefines.PARAMS_KEY_RESOURCE_ID_STRING, config.resourceId)
        e.setOptionString(SpeechEngineDefines.PARAMS_KEY_UID_STRING, config.uid)
        e.setOptionString(SpeechEngineDefines.PARAMS_KEY_DIALOG_ADDRESS_STRING, config.dialogAddress)
        e.setOptionString(SpeechEngineDefines.PARAMS_KEY_DIALOG_URI_STRING, config.dialogUri)

        
        e.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_ENABLE_AEC_BOOL, true)
        e.setOptionString(
            SpeechEngineDefines.PARAMS_KEY_AEC_MODEL_PATH_STRING,
            extractAssetToFile(app.applicationContext, config.aecModelAssetName).absolutePath
        )

        
        val logDir = File(app.filesDir, "speech_logs")
        logDir.mkdirs()
        e.setOptionString(SpeechEngineDefines.PARAMS_KEY_DEBUG_PATH_STRING, logDir.absolutePath)
        e.setOptionString(SpeechEngineDefines.PARAMS_KEY_LOG_LEVEL_STRING, SpeechEngineDefines.LOG_LEVEL_TRACE)

        
        e.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_DIALOG_ENABLE_RECORDER_AUDIO_CALLBACK_BOOL, true)

        val ret = e.initEngine()
        if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
            _events.tryEmit(VoiceEvent.Error(ret, "initEngine failed: $ret"))
            return
        } else {
            _events.tryEmit(VoiceEvent.Debug("initEngine ok"))
        }

        e.setContext(app.applicationContext)
        e.setListener(this)

        engine = e
        inited = true
    }

    override fun startEngine(startJson: String) {
        val e = engine ?: return
        e.sendDirective(SpeechEngineDefines.DIRECTIVE_SYNC_STOP_ENGINE, "")
        val ret = e.sendDirective(SpeechEngineDefines.DIRECTIVE_START_ENGINE, startJson)
        _events.tryEmit(VoiceEvent.Debug("startEngine ret=$ret"))
        if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
            _events.tryEmit(VoiceEvent.Error(ret, "start engine failed: $ret"))
        }
    }

    override fun stopEngine() {
        engine?.sendDirective(SpeechEngineDefines.DIRECTIVE_SYNC_STOP_ENGINE, "")
    }

    override fun release() {
        val e = engine
        engine = null
        inited = false
        try {
            e?.destroyEngine()
        } catch (_: Throwable) {
        }
    }

    override fun sayHello(content: String) {
        val e = engine ?: return
        val json = "{\"content\":\"${escapeJson(content)}\"}"
        val ret = e.sendDirective(SpeechEngineDefines.DIRECTIVE_EVENT_SAY_HELLO, json)
        _events.tryEmit(VoiceEvent.Debug("sayHello ret=$ret"))
        if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
            _events.tryEmit(VoiceEvent.Error(ret, "sayHello failed: $ret"))
        }
    }

    override fun chatTextQuery(content: String) {
        val e = engine ?: return
        val json = "{\"content\":\"${escapeJson(content)}\"}"
        val ret = e.sendDirective(SpeechEngineDefines.DIRECTIVE_EVENT_CHAT_TEXT_QUERY, json)
        if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
            _events.tryEmit(VoiceEvent.Error(ret, "chatTextQuery failed: $ret"))
        }
    }

    override fun onSpeechMessage(type: Int, data: ByteArray?, len: Int) {
        val str = try {
            if (data != null) String(data, 0, len.coerceAtMost(data.size)) else ""
        } catch (_: Throwable) {
            ""
        }

        
        Log.d("VoiceEngine", "onSpeechMessage type=$type len=$len")

        when (type) {
            SpeechEngineDefines.MESSAGE_TYPE_ENGINE_START -> _events.tryEmit(VoiceEvent.EngineStarted)
            SpeechEngineDefines.MESSAGE_TYPE_ENGINE_STOP -> _events.tryEmit(VoiceEvent.EngineStopped)
            SpeechEngineDefines.MESSAGE_TYPE_ENGINE_ERROR -> _events.tryEmit(VoiceEvent.Error(type, str.ifBlank { "engine error" }))

            SpeechEngineDefines.MESSAGE_TYPE_DIALOG_ASR_INFO -> _events.tryEmit(VoiceEvent.AsrStarted)
            SpeechEngineDefines.MESSAGE_TYPE_DIALOG_ASR_RESPONSE -> _events.tryEmit(VoiceEvent.AsrText(str))
            SpeechEngineDefines.MESSAGE_TYPE_DIALOG_ASR_ENDED -> _events.tryEmit(VoiceEvent.AsrEnded)

            SpeechEngineDefines.MESSAGE_TYPE_DIALOG_CHAT_RESPONSE -> _events.tryEmit(VoiceEvent.ChatText(str))
            SpeechEngineDefines.MESSAGE_TYPE_DIALOG_CHAT_ENDED -> _events.tryEmit(VoiceEvent.ChatEnded)

            SpeechEngineDefines.MESSAGE_TYPE_DIALOG_RECORDER_AUDIO -> {
                if (data != null && len > 0) {
                    val amp = pcm16leAmplitude01(data, len)
                    _events.tryEmit(VoiceEvent.Volume(amp))
                }
            }
        }
    }

    private fun extractAssetToFile(context: Context, assetName: String): File {
        val outFile = File(context.filesDir, assetName)
        if (outFile.exists() && outFile.length() > 0) return outFile

        context.assets.open(assetName).use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return outFile
    }

    private fun escapeJson(s: String): String =
        s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

    private fun pcm16leAmplitude01(bytes: ByteArray, len: Int): Float {
        var maxAbs = 0
        val safeLen = len.coerceAtMost(bytes.size)
        var i = 0
        while (i + 1 < safeLen) {
            val lo = bytes[i].toInt() and 0xFF
            val hi = bytes[i + 1].toInt()
            val sample = (hi shl 8) or lo
            val a = abs(sample)
            if (a > maxAbs) maxAbs = a
            i += 2
        }
        return (maxAbs / 32768f).coerceIn(0f, 1f)
    }
}
