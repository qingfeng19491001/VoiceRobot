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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 系统录音振幅采集（解耦 SDK）
 *
 * - 16kHz, mono, PCM16
 * - 每 ~16ms 输出一次振幅（0..1）
 */
class AudioAmplitudeReader(
    private val context: Context,
) {
    private val _amplitude = MutableSharedFlow<Float>(extraBufferCapacity = 8)
    val amplitude: SharedFlow<Float> = _amplitude

    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        if (job != null) return

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            _amplitude.tryEmit(0f)
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

        job = scope.launch(Dispatchers.Default) {
            val buf = ShortArray(256) // ~=16ms @16k
            try {
                audioRecord.startRecording()
                Log.d("AudioAmp", "AudioRecord started")
                while (isActive) {
                    val read = audioRecord.read(buf, 0, buf.size)
                    if (read > 0) {
                        val amp = rmsAmplitude01(buf, read)
                        // 非线性放大：让小声也能明显驱动动画
                        val boosted = ((amp.toDouble().pow(0.55)) * 4.0).coerceIn(0.0, 1.0).toFloat()
                        _amplitude.tryEmit(boosted)
                    }
                    delay(16)
                }
            } catch (t: Throwable) {
                Log.e("AudioAmp", "AudioRecord loop error", t)
            } finally {
                try { audioRecord.stop() } catch (_: Throwable) {}
                try { audioRecord.release() } catch (_: Throwable) {}
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
