package com.voicerobot.lottie.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import kotlin.math.sin

/**
 * 三个小球声纹：
 * - 待机：三个小球轻微呼吸/起伏
 * - 讲话：根据输入振幅（0..1），三个小球从前往后依次沿曲线跳动
 * 
 * 优化：完全基于 ValueAnimator，统一动画驱动机制
 * - 时间轴动画（t）：无限循环，驱动三个小球的波浪效果
 * - 振幅平滑：从当前值平滑过渡到目标值，使用 AccelerateDecelerateInterpolator
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

    // 时间轴：用于三个小球的波浪动画（无限循环）
    private var t: Float = 0f
    private var timeAxisAnimator: ValueAnimator? = null

    // 振幅平滑：用 ValueAnimator 从当前值动画到目标值
    private var smoothAmp: Float = 0f
    private var targetAmp: Float = 0f
    private var amplitudeAnimator: ValueAnimator? = null

    // ValueAnimator 配置
    private val amplitudeAnimDuration = 150L // 150ms 过渡，快速响应但足够平滑
    private val amplitudeInterpolator = AccelerateDecelerateInterpolator()
    
    // 时间轴动画：无限循环更新 t（用于三个小球的波浪效果）
    private val timeAxisStep = 0.06f // 每次更新 t 的增量

    fun setBallColor(@ColorInt color: Int) {
        ballColor = color
        invalidate()
    }

    /** 输入 0..1，使用 ValueAnimator 平滑过渡到目标值 */
    fun pushAmplitude01(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        if (clamped == targetAmp) return // 避免重复动画

        targetAmp = clamped

        // 如果 animator 正在运行，先取消
        amplitudeAnimator?.cancel()

        // 创建新的 animator：从当前 smoothAmp 动画到 targetAmp
        amplitudeAnimator = ValueAnimator.ofFloat(smoothAmp, targetAmp).apply {
            duration = amplitudeAnimDuration
            interpolator = amplitudeInterpolator
            addUpdateListener { anim ->
                smoothAmp = anim.animatedValue as Float
                invalidate() // 振幅变化时触发重绘
            }
            start()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startTimeAxisAnimation()
    }

    override fun onDetachedFromWindow() {
        stopTimeAxisAnimation()
        amplitudeAnimator?.cancel()
        amplitudeAnimator = null
        super.onDetachedFromWindow()
    }

    private fun startTimeAxisAnimation() {
        if (timeAxisAnimator != null) return

        // 时间轴动画：无限循环更新 t（用于三个小球的波浪效果）
        // 使用简单的 0..1 循环，每次更新时 t += step
        timeAxisAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 16L // 约 60fps，每帧更新
            interpolator = LinearInterpolator() // 线性插值，保证 t 匀速增长
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                t += timeAxisStep
                invalidate() // 每帧都触发重绘
            }
            start()
        }
    }

    private fun stopTimeAxisAnimation() {
        timeAxisAnimator?.cancel()
        timeAxisAnimator = null
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
