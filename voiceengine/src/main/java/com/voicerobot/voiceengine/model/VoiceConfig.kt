package com.voicerobot.voiceengine.model

/**
 * Volc Dialog SDK auth/config.
 *
 * NOTE: In production, do NOT hardcode token/appKey in repo; inject from secure source.
 */
data class VoiceConfig(
    val appId: String,
    val appKey: String,
    val accessToken: String,
    val resourceId: String = "volc.speech.dialog",
    val uid: String = "demo_uid",
    val dialogAddress: String = "wss://openspeech.bytedance.com",
    val dialogUri: String = "/api/v3/realtime/dialogue",
    /** assets file name inside APK, e.g. "aec.model" */
    val aecModelAssetName: String = "aec.model",
)
