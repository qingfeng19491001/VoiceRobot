package com.voicerobot.rtc

import android.content.Context
import android.util.Log
import android.view.TextureView
import android.widget.FrameLayout
import com.ss.bytertc.engine.RTCEngine
import com.ss.bytertc.engine.RTCRoom
import com.ss.bytertc.engine.RTCRoomConfig
import com.ss.bytertc.engine.UserInfo
import com.ss.bytertc.engine.VideoCanvas
import com.ss.bytertc.engine.data.CameraId
import com.ss.bytertc.engine.data.EngineConfig
import com.ss.bytertc.engine.data.StreamInfo
import com.ss.bytertc.engine.handler.IRTCEngineEventHandler
import com.ss.bytertc.engine.handler.IRTCRoomEventHandler
import com.ss.bytertc.engine.type.ChannelProfile
import com.ss.bytertc.engine.type.RTCRoomStats
import com.ss.bytertc.engine.type.TorchState

class RtcClient(
    private val context: Context,
    private val appId: String,
) {
    private var rtcEngine: RTCEngine? = null
    private var rtcRoom: RTCRoom? = null

    private var cameraId: CameraId = CameraId.CAMERA_ID_FRONT
    private var torchEnabled: Boolean = false

    private var captureStarted: Boolean = false

    fun init() {
        if (rtcEngine != null) return
        Log.d(TAG, "init appId=$appId")

        val engineConfig = EngineConfig().apply {
            this.context = this@RtcClient.context.applicationContext
            this.appID = appId
        }
        rtcEngine = RTCEngine.createRTCEngine(engineConfig, object : IRTCEngineEventHandler() {
            override fun onError(err: Int) {
                Log.e(TAG, "engine onError err=$err")
            }
        })

        captureStarted = false
    }

    fun ensureCaptureStarted() {
        val engine = rtcEngine
        if (engine == null) {
            Log.w(TAG, "ensureCaptureStarted ignored: engine is null")
            return
        }
        if (captureStarted) return
        val v = engine.startVideoCapture()
        val a = engine.startAudioCapture()
        captureStarted = true
        Log.d(TAG, "startVideoCapture ret=$v; startAudioCapture ret=$a")
    }

    fun bindLocalPreview(container: FrameLayout) {
        val engine = rtcEngine
        if (engine == null) {
            Log.w(TAG, "bindLocalPreview ignored: engine is null")
            return
        }

        val textureView = TextureView(container.context)
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        container.removeAllViews()
        container.addView(textureView, lp)

        val canvas = VideoCanvas().apply {
            renderView = textureView
            renderMode = VideoCanvas.RENDER_MODE_HIDDEN
        }
        engine.setLocalVideoCanvas(canvas)
        Log.d(TAG, "bindLocalPreview: setLocalVideoCanvas")

        ensureCaptureStarted()
    }

    fun switchCamera(): Boolean {
        val engine = rtcEngine
        if (engine == null) {
            Log.w(TAG, "switchCamera ignored: engine is null")
            return false
        }

        cameraId = if (cameraId == CameraId.CAMERA_ID_FRONT) CameraId.CAMERA_ID_BACK else CameraId.CAMERA_ID_FRONT
        val ret = engine.switchCamera(cameraId)
        Log.d(TAG, "switchCamera to=$cameraId ret=$ret")

        if (cameraId != CameraId.CAMERA_ID_BACK && torchEnabled) {
            setTorchEnabled(false)
        }

        return ret == 0
    }

    fun toggleTorch(): Boolean {
        val engine = rtcEngine
        if (engine == null) {
            Log.w(TAG, "toggleTorch ignored: engine is null")
            return false
        }
        if (!engine.isCameraTorchSupported()) {
            Log.d(TAG, "toggleTorch not supported")
            return false
        }
        val target = !torchEnabled
        return setTorchEnabled(target)
    }

    private fun setTorchEnabled(enabled: Boolean): Boolean {
        val engine = rtcEngine ?: return false
        if (!engine.isCameraTorchSupported()) return false

        val state = if (enabled) TorchState.TORCH_STATE_ON else TorchState.TORCH_STATE_OFF
        val ret = engine.setCameraTorch(state)
        Log.d(TAG, "setCameraTorch enabled=$enabled state=$state ret=$ret")
        if (ret == 0) {
            torchEnabled = enabled
            return true
        }
        return false
    }

    fun joinRoom(
        roomId: String,
        userId: String,
        token: String,
        roomEventHandler: IRTCRoomEventHandler? = null,
    ) {
        val engine = rtcEngine
        if (engine == null) {
            Log.w(TAG, "joinRoom ignored: engine is null")
            return
        }

        Log.d(TAG, "joinRoom roomId=$roomId userId=$userId")

        rtcRoom?.leaveRoom()
        rtcRoom?.destroy()

        val handler = roomEventHandler ?: object : IRTCRoomEventHandler() {
            override fun onRoomStateChanged(roomId: String?, uid: String?, state: Int, extraInfo: String?) {
                Log.d(TAG, "onRoomStateChanged roomId=$roomId uid=$uid state=$state extra=$extraInfo")
            }

            override fun onRoomStateChangedWithReason(
                roomId: String?,
                uid: String?,
                state: com.ss.bytertc.engine.type.RoomState?,
                reason: com.ss.bytertc.engine.type.RoomStateChangeReason?,
            ) {
                Log.d(TAG, "onRoomStateChangedWithReason roomId=$roomId uid=$uid state=$state reason=$reason")
            }

            override fun onUserJoined(userInfo: UserInfo?) {
                Log.d(TAG, "onUserJoined user=${userInfo?.uid}")
            }

            override fun onUserLeave(uid: String?, reason: Int) {
                Log.d(TAG, "onUserLeave uid=$uid reason=$reason")
            }

            override fun onUserPublishStreamVideo(streamId: String?, streamInfo: StreamInfo?, isPublish: Boolean) {
                Log.d(TAG, "onUserPublishStreamVideo isPublish=$isPublish streamId=$streamId info=${streamInfo?.streamId}")
            }

            override fun onUserPublishStreamAudio(streamId: String?, streamInfo: StreamInfo?, isPublish: Boolean) {
                Log.d(TAG, "onUserPublishStreamAudio isPublish=$isPublish streamId=$streamId info=${streamInfo?.streamId}")
            }

            override fun onLeaveRoom(stats: RTCRoomStats?) {
                Log.d(TAG, "onLeaveRoom stats=$stats")
            }
        }

        rtcRoom = engine.createRTCRoom(roomId).apply {
            setRTCRoomEventHandler(handler)
        }

        val userInfo = UserInfo(userId, "")
        val roomConfig = RTCRoomConfig(
            ChannelProfile.CHANNEL_PROFILE_CHAT_ROOM,
            null,
            true,
            true,
            true,
            true,
        )
        rtcRoom?.joinRoom(token, userInfo, true, roomConfig)
    }

    fun leaveRoom() {
        Log.d(TAG, "leaveRoom")
        rtcRoom?.leaveRoom()
        rtcRoom?.destroy()
        rtcRoom = null
    }

    fun release() {
        Log.d(TAG, "release")
        rtcEngine?.stopVideoCapture()
        rtcEngine?.stopAudioCapture()
        rtcEngine = null
        captureStarted = false
        RTCEngine.destroyRTCEngine()
    }

    companion object {
        private const val TAG = "RtcClient"
    }
}
