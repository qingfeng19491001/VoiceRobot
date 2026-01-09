package com.voicerobot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.voicerobot.audio.AudioAmplitudeReader
import com.voicerobot.lottie.AgentAnimMapper
import com.voicerobot.lottie.widget.WaveformView
import com.voicerobot.ui.chat.ChatAdapter
import com.voicerobot.ui.robot.MainViewModel
import com.voicerobot.ui.robot.MainViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var vm: MainViewModel

    private val chatAdapter = ChatAdapter()
    private var isChatVisible = false

    private lateinit var amplitudeReader: AudioAmplitudeReader

    private val requestAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d("MainActivity", "RECORD_AUDIO permission result: $granted")
        if (granted) {
            vm.startIfNeeded()
            amplitudeReader.start(lifecycleScope)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val app = application as VoiceRobotApp
        vm = ViewModelProvider(
            this,
            MainViewModelFactory(app.container.voiceEngineRepository)
        )[MainViewModel::class.java]

        amplitudeReader = AudioAmplitudeReader(this)

        val avatar = findViewById<LottieAnimationView>(R.id.robotAvatar)
        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvConversation)
        val toggle = findViewById<android.widget.TextView>(R.id.tvToggleTextPanel)
        val waveform = findViewById<WaveformView>(R.id.waveform)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = chatAdapter

        toggle.setOnClickListener {
            isChatVisible = !isChatVisible
            rv.visibility = if (isChatVisible) View.VISIBLE else View.GONE
            Log.d("MainActivity", "toggle chat visible=$isChatVisible")
        }

        // robot animation state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state ->
                    val res = AgentAnimMapper.animRes(state.phase)
                    avatar.setAnimation(res)
                    avatar.playAnimation()
                }
            }
        }

        // chat list
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.chatMessages.collect { list ->
                    chatAdapter.submitList(list)
                    if (list.isNotEmpty()) {
                        rv.post { rv.smoothScrollToPosition(list.lastIndex) }
                    }
                }
            }
        }

        // amplitude -> waveform (decoupled from SDK)
        amplitudeReader.amplitude
            .onEach { amp -> waveform.pushAmplitude01(amp) }
            .launchIn(lifecycleScope)

        ensureAudioPermissionAndStart()
    }

    private fun ensureAudioPermissionAndStart() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        Log.d("MainActivity", "RECORD_AUDIO granted=$granted")

        if (granted) {
            vm.startIfNeeded()
            amplitudeReader.start(lifecycleScope)
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onDestroy() {
        amplitudeReader.stop()
        super.onDestroy()
    }
}
