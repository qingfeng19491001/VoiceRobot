package com.voicerobot.lottie.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import androidx.annotation.ColorInt
import kotlin.math.sin

/**
 * 三个小球声纹：
 * - 待机：三个小球轻微呼吸/起伏
 * - 讲话：根据输入振幅（0..1），三个小球从前往后依次沿曲线跳动
 */
class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    @ColorInt
    private var ballColor: Int = 0xFFFFFFFF.toInt()

    private var frameCallback: Choreographer.FrameCallback? = null

    private var amp01: Float = 0f
    private var t: Float = 0f
    private var smoothAmp: Float = 0f

    fun setBallColor(@ColorInt color: Int) {
        ballColor = color
        invalidate()
    }

    /** 输入 0..1 */
    fun pushAmplitude01(value: Float) {
        amp01 = value.coerceIn(0f, 1f)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (frameCallback == null) {
            frameCallback = Choreographer.FrameCallback {
                t += 0.06f
                smoothAmp = smoothAmp * 0.85f + amp01 * 0.15f
                invalidate()
                frameCallback?.let { cb -> Choreographer.getInstance().postFrameCallback(cb) }
            }
        }
        frameCallback?.let { Choreographer.getInstance().postFrameCallback(it) }
    }

    override fun onDetachedFromWindow() {
        frameCallback?.let { Choreographer.getInstance().removeFrameCallback(it) }
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val cy = h / 2f

        // 图二观感：三个点更靠近一些
        val centerX = w / 2f
        val gap = dp(20f)
        val x1 = centerX - gap
        val x2 = centerX
        val x3 = centerX + gap

        // 图二观感：点更小一些，且固定大小更像“...”
        val baseR = dp(6.8f)
        val maxJump = dp(14f)

        // idle：轻微起伏
        val idle = 0.16f + 0.06f * sin(t)

        // speaking：输入振幅驱动
        val speak = (smoothAmp * 1.25f).coerceIn(0f, 1f)

        // 三个球依次延迟（从左到右）
        val p1 = idle + speak * wave01(t + 0.0f)
        val p2 = idle + speak * wave01(t + 0.9f)
        val p3 = idle + speak * wave01(t + 1.8f)

        val y1 = cy - bezierArcY(p1.coerceIn(0f, 1f)) * maxJump
        val y2 = cy - bezierArcY(p2.coerceIn(0f, 1f)) * maxJump
        val y3 = cy - bezierArcY(p3.coerceIn(0f, 1f)) * maxJump

        // 半径轻微变化即可
        val r1 = baseR * (1f + 0.18f * p1)
        val r2 = baseR * (1f + 0.18f * p2)
        val r3 = baseR * (1f + 0.18f * p3)

        paint.color = ballColor
        canvas.drawCircle(x1, y1, r1, paint)
        canvas.drawCircle(x2, y2, r2, paint)
        canvas.drawCircle(x3, y3, r3, paint)
    }

    private fun wave01(x: Float): Float = ((sin(x) + 1f) * 0.5f).coerceIn(0f, 1f)

    private fun bezierArcY(t: Float): Float {
        val p0 = 0f
        val p1 = 1f
        val p2 = 0f
        val u = 1f - t
        return u * u * p0 + 2f * u * t * p1 + t * t * p2
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
}
