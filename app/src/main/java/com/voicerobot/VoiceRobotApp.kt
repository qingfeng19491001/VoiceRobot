package com.voicerobot

import android.app.Application
import com.voicerobot.di.AppContainer

class VoiceRobotApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

