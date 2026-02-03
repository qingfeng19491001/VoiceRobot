package com.voicerobot.ui.video

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.voicerobot.BuildConfig
import com.voicerobot.VoiceRobotApp
import com.voicerobot.rtc.RtcClient

class VideoViewModelFactory(
    private val app: Application,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val container = (app as VoiceRobotApp).container
        val rtcClient = RtcClient(app.applicationContext, BuildConfig.RTC_APP_ID)
        return VideoViewModel(
            rtcClient = rtcClient,
            startVoiceChatApi = container.startVoiceChatApi,
        ) as T
    }
}

