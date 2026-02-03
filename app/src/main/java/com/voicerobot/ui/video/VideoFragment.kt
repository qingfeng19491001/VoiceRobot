package com.voicerobot.ui.video

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.voicerobot.R
import com.voicerobot.VoiceRobotApp
import com.voicerobot.databinding.FragmentVideoBinding
import kotlinx.coroutines.launch

class VideoFragment : Fragment(R.layout.fragment_video) {

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!

    private lateinit var vm: VideoViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVideoBinding.bind(view)

        val app = requireActivity().application as VoiceRobotApp
        vm = ViewModelProvider(this, VideoViewModelFactory(app))[VideoViewModel::class.java]

        Log.d(TAG, "onViewCreated")

        binding.btnFlashlight.setOnClickListener {
            vm.onTorchClicked()
        }
        binding.btnSwitchCamera.setOnClickListener {
            vm.onSwitchCameraClicked()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.uiState.collect { state ->
                state.lastError?.let {
                    Log.e(TAG, "error: $it")
                    Toast.makeText(requireContext(), "失败: $it", Toast.LENGTH_LONG).show()
                }
                state.startVoiceChatResult?.let {
                    val msg = buildString {
                        append("StartVoiceChat=").append(it)
                        state.startVoiceChatRequestId?.let { rid -> append("\nRequestId=").append(rid) }
                    }
                    Log.d(TAG, msg)
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                }
                state.cameraSwitched?.let {
                    Toast.makeText(requireContext(), if (it) "已切换摄像头" else "切换摄像头失败", Toast.LENGTH_SHORT).show()
                }
                state.torchEnabled?.let {
                    Toast.makeText(requireContext(), if (it) "手电筒已切换" else "手电筒不可用", Toast.LENGTH_SHORT).show()
                }
            }
        }

        requestPermissionsIfNeededAndStart()
    }

    override fun onResume() {
        super.onResume()
        enterImmersive()
    }

    override fun onPause() {
        exitImmersive()
        super.onPause()
    }

    private fun enterImmersive() {
        val window = activity?.window ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
        }
    }

    private fun exitImmersive() {
        val window = activity?.window ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun requestPermissionsIfNeededAndStart() {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            needed += Manifest.permission.CAMERA
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            needed += Manifest.permission.RECORD_AUDIO
        }

        if (needed.isNotEmpty()) {
            Log.d(TAG, "requestPermissions: $needed")
            @Suppress("DEPRECATION")
            requestPermissions(needed.toTypedArray(), REQ_PERMISSIONS)
        } else {
            Log.d(TAG, "permissions already granted, start")
            startAfterPermissions()
        }
    }

    private fun startAfterPermissions() {
        // 1. Initialize RTC engine synchronously.
        vm.initializeRtc()

        // 2. Post the binding to ensure the container view is laid out.
        binding.videoContainer.post {
            // 3. Bind the preview surface.
            vm.bindLocalPreview(binding.videoContainer)
            // 4. Now that the engine is initialized and preview is bound, start network operations.
            vm.joinRoomAndStartAi()
        }
    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PERMISSIONS) {
            val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            Log.d(TAG, "onRequestPermissionsResult granted=$granted")
            if (granted) {
                startAfterPermissions()
            } else {
                Toast.makeText(requireContext(), "需要相机/麦克风权限才能继续", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "VideoFragment"
        private const val REQ_PERMISSIONS = 1001
    }
}
