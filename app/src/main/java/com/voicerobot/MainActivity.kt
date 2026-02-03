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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.fragment.NavHostFragment
import com.voicerobot.audio.AudioAmplitudeReader
import com.voicerobot.databinding.ActivityMainBinding
import com.voicerobot.ui.robot.MainViewModel
import com.voicerobot.ui.robot.MainViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {

    lateinit var vm: MainViewModel
        private set

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    private val requestAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d("MainActivity", "RECORD_AUDIO permission result: $granted")
        if (granted) {
            vm.startIfNeeded()
            AudioAmplitudeReader.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as VoiceRobotApp
        vm = ViewModelProvider(
            this,
            MainViewModelFactory(app.container.voiceEngineRepository)
        )[MainViewModel::class.java]

        AudioAmplitudeReader.init(this)

        val host = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = host.navController

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val destId = navController.currentDestination?.id
            if (destId == R.id.videoFragment) {
                v.setPadding(0, 0, 0, 0)
            } else {
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            }
            insets
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isVideo = destination.id == R.id.videoFragment

            binding.gradientBg.visibility = if (isVideo) View.GONE else View.VISIBLE
            binding.topBar.visibility = if (isVideo) View.GONE else View.VISIBLE

            if (isVideo) {
                AudioAmplitudeReader.stop()
                vm.stop()
            } else {
                applyMicState()
            }
        }

        AudioAmplitudeReader.amplitude
            .onEach { amp -> binding.waveform.pushAmplitude01(amp) }
            .launchIn(lifecycleScope)

        bindBottomActions()
        applyMicState()
        ensureAudioPermissionAndStart()
    }

    private var micEnabled = false

    private fun bindBottomActions() {
        binding.btnPrj.setOnClickListener {
            navigateTopLevel(R.id.prjFragment)
        }

        binding.btnVideo.setOnClickListener {
            navigateTopLevel(R.id.videoFragment)
        }

        binding.btnEnd.setOnClickListener {
            if (navController.currentDestination?.id == R.id.videoFragment) {
                navController.popBackStack()
            } else {
                navigateTopLevel(R.id.homeFragment)
            }
        }

        binding.btnMic.setOnClickListener {
            micEnabled = !micEnabled
            applyMicState()
        }
    }

    private fun applyMicState() {
        if (navController.currentDestination?.id == R.id.videoFragment) {
            // Video page owns mic/camera; keep main engine stopped.
            return
        }

        if (micEnabled) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                vm.startIfNeeded()
                AudioAmplitudeReader.start()
                binding.btnMic.setImageResource(R.drawable.ic_mic_normal)
            } else {
                requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        } else {
            AudioAmplitudeReader.stop()
            vm.stop()
            binding.waveform.pushAmplitude01(0f)
            binding.btnMic.setImageResource(R.drawable.ic_mic_mute)
        }
    }

    private fun navigateTopLevel(destId: Int) {
        val currentId = navController.currentDestination?.id
        if (currentId == destId) return

        navController.navigate(destId, null, androidx.navigation.navOptions {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        })
    }

    private fun ensureAudioPermissionAndStart() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        Log.d("MainActivity", "RECORD_AUDIO granted=$granted")

        if (granted) {
            vm.startIfNeeded()
            AudioAmplitudeReader.start()
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onDestroy() {
        AudioAmplitudeReader.stop()
        super.onDestroy()
    }
}
