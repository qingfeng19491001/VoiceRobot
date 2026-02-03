package com.voicerobot.core.network

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object VolcTime {
    private val X_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")

    fun nowXDate(): String {
        return ZonedDateTime.now(ZoneOffset.UTC).format(X_DATE_FORMATTER)
    }
}

