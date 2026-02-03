package com.voicerobot.core.network

import java.net.URLEncoder
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object VolcRtcApiSigner {

    data class SignedHeaders(
        val url: String,
        val headers: Map<String, String>,
    )

    fun sign(
        host: String,
        action: String,
        version: String,
        region: String,
        service: String,
        httpMethod: String,
        accessKeyId: String,
        secretAccessKey: String,
        sessionToken: String?,
        bodySha256Hex: String,
        xDate: String,
    ): SignedHeaders {
        val canonicalQuery = listOf(
            "Action" to action,
            "Version" to version,
        ).sortedBy { it.first }.joinToString("&") { (k, v) ->
            "${urlEncode(k)}=${urlEncode(v)}"
        }

        val canonicalUri = "/"
        val canonicalHeaders = buildString {
            append("host:").append(host).append("\n")
            append("x-date:").append(xDate).append("\n")
        }
        val signedHeaders = "host;x-date"
        val canonicalRequest = buildString {
            append(httpMethod.uppercase()).append("\n")
            append(canonicalUri).append("\n")
            append(canonicalQuery).append("\n")
            append(canonicalHeaders).append("\n")
            append(signedHeaders).append("\n")
            append(bodySha256Hex)
        }

        val algorithm = "HMAC-SHA256"
        val credentialScope = "$xDate/$region/$service/request"
        val stringToSign = buildString {
            append(algorithm).append("\n")
            append(xDate).append("\n")
            append(credentialScope).append("\n")
            append(sha256Hex(canonicalRequest))
        }

        val kDate = hmacSha256(xDate.toByteArray(), secretAccessKey.toByteArray())
        val kRegion = hmacSha256(region.toByteArray(), kDate)
        val kService = hmacSha256(service.toByteArray(), kRegion)
        val kSigning = hmacSha256("request".toByteArray(), kService)

        val signature = hmacSha256(stringToSign.toByteArray(), kSigning)
        val signatureHex = toHex(signature)

        val authorization = "$algorithm Credential=$accessKeyId/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signatureHex"

        val headers = linkedMapOf(
            "Host" to host,
            "X-Date" to xDate,
            "Authorization" to authorization,
            "Content-Type" to "application/json",
        )
        if (!sessionToken.isNullOrBlank()) {
            headers["X-Security-Token"] = sessionToken
        }

        val url = "https://$host?$canonicalQuery"
        return SignedHeaders(url = url, headers = headers)
    }

    fun sha256Hex(content: String): String = sha256Hex(content.toByteArray())

    fun sha256Hex(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        return toHex(md.digest(bytes))
    }

    private fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    private fun toHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) sb.append(String.format("%02x", b))
        return sb.toString()
    }

    private fun urlEncode(s: String): String = URLEncoder.encode(s, Charsets.UTF_8.name()).replace("+", "%20")
}

