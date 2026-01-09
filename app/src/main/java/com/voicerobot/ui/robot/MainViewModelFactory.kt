package com.voicerobot.ui.robot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.voicerobot.voiceengine.api.VoiceEngineRepository

class MainViewModelFactory(
    private val voiceEngineRepository: VoiceEngineRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(voiceEngineRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

