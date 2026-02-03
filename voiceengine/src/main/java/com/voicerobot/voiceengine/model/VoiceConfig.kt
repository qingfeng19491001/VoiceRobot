package com.voicerobot.voiceengine.model


data class VoiceConfig(
    val appId: String,
    val appKey: String,
    val accessToken: String,
    val resourceId: String = "volc.speech.dialog",
    val uid: String = "demo_uid",
    val dialogAddress: String = "wss://openspeech.bytedance.com",
    val dialogUri: String = "/api/v3/realtime/dialogue",
    
    val aecModelAssetName: String = "aec.model",
)
