package com.voicerobot.core.network

import okhttp3.OkHttpClient

object StartVoiceChatApiFactory {

    fun create(
        accessKeyId: String,
        secretAccessKey: String,
        sessionToken: String?,
        region: String = "cn-north-1",
    ): StartVoiceChatApi {
        val client = OkHttpClient.Builder().build()
        return StartVoiceChatApi(
            okHttpClient = client,
            accessKeyId = accessKeyId,
            secretAccessKey = secretAccessKey,
            sessionToken = sessionToken,
            region = region,
        )
    }
}

