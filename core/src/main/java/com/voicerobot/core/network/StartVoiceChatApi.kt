package com.voicerobot.core.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class StartVoiceChatApi(
    private val okHttpClient: OkHttpClient,
    private val accessKeyId: String,
    private val secretAccessKey: String,
    private val sessionToken: String?,
    private val region: String = "cn-north-1",
    private val host: String = "rtc.volcengineapi.com",
) {

    data class StartVoiceChatResponse(
        val result: String,
        val requestId: String?,
        val rawBody: String,
    )

    fun startVoiceChat(request: StartVoiceChatRequest): StartVoiceChatResponse {
        val requestJson = StartVoiceChatJson.encode(request)
        val xDate = VolcTime.nowXDate()
        val bodySha = VolcRtcApiSigner.sha256Hex(requestJson)

        val signed = VolcRtcApiSigner.sign(
            host = host,
            action = "StartVoiceChat",
            version = "2024-12-01",
            region = region,
            service = "rtc",
            httpMethod = "POST",
            accessKeyId = accessKeyId,
            secretAccessKey = secretAccessKey,
            sessionToken = sessionToken,
            bodySha256Hex = bodySha,
            xDate = xDate,
        )

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toRequestBody(mediaType)

        val reqBuilder = Request.Builder()
            .url(signed.url)
            .post(body)

        for ((k, v) in signed.headers) {
            reqBuilder.header(k, v)
        }

        okHttpClient.newCall(reqBuilder.build()).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw IllegalStateException("StartVoiceChat failed: http=${resp.code} body=$raw")
            }
            val result = Regex("\\\"Result\\\"\\s*:\\s*\\\"(.*?)\\\"").find(raw)?.groupValues?.getOrNull(1)
                ?: Regex("\\\"result\\\"\\s*:\\s*\\\"(.*?)\\\"").find(raw)?.groupValues?.getOrNull(1)
                ?: ""
            val requestId = Regex("\\\"RequestId\\\"\\s*:\\s*\\\"(.*?)\\\"").find(raw)?.groupValues?.getOrNull(1)
            return StartVoiceChatResponse(result = result, requestId = requestId, rawBody = raw)
        }
    }
}

