package com.voicerobot.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

object AudioAmplitudeReader {
    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private var job: Job? = null
    private lateinit var appContext: Context
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun start() {
        if (job != null) return

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            _amplitude.value = 0f
            _isRecording.value = false
            return
        }

        val sampleRate = 16000
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = (minBuf * 2).coerceAtLeast(4096)

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        job = scope.launch {
            val buf = ShortArray(256)
            try {
                audioRecord.startRecording()
                _isRecording.value = true
                Log.d("AudioAmp", "AudioRecord started")
                while (isActive) {
                    val read = audioRecord.read(buf, 0, buf.size)
                    if (read > 0) {
                        val amp = rmsAmplitude01(buf, read)
                        val boosted = ((amp.toDouble().pow(0.55)) * 4.0).coerceIn(0.0, 1.0).toFloat()
                        _amplitude.value = boosted
                    }
                    delay(16)
                }
            } catch (t: Throwable) {
                Log.e("AudioAmp", "AudioRecord loop error", t)
            } finally {
                try { audioRecord.stop() } catch (_: Throwable) {}
                try { audioRecord.release() } catch (_: Throwable) {}
                _isRecording.value = false
                _amplitude.value = 0f
                Log.d("AudioAmp", "AudioRecord stopped")
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun rmsAmplitude01(buf: ShortArray, n: Int): Float {
        var sum = 0.0
        for (i in 0 until n) {
            val v = buf[i].toDouble()
            sum += v * v
        }
        val mean = sum / n
        val rms = sqrt(mean)
        return (rms / 32768.0).toFloat().coerceIn(0f, 1f)
    }
}
