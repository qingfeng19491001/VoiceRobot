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

    
    private var t: Float = 0f
    private var timeAxisAnimator: ValueAnimator? = null

    
    private var smoothAmp: Float = 0f
    private var targetAmp: Float = 0f
    private var amplitudeAnimator: ValueAnimator? = null

    
    private val amplitudeAnimDuration = 150L 
    private val amplitudeInterpolator = AccelerateDecelerateInterpolator()
    
    
    private val timeAxisStep = 0.06f 

    fun setBallColor(@ColorInt color: Int) {
        ballColor = color
        invalidate()
    }

    
    fun pushAmplitude01(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        if (clamped == targetAmp) return 

        targetAmp = clamped

        
        amplitudeAnimator?.cancel()

        
        amplitudeAnimator = ValueAnimator.ofFloat(smoothAmp, targetAmp).apply {
            duration = amplitudeAnimDuration
            interpolator = amplitudeInterpolator
            addUpdateListener { anim ->
                smoothAmp = anim.animatedValue as Float
                invalidate() 
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

        
        
        timeAxisAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 16L 
            interpolator = LinearInterpolator() 
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                t += timeAxisStep
                invalidate() 
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

        
        val centerX = w / 2f
        val gap = dp(20f)
        val x1 = centerX - gap
        val x2 = centerX
        val x3 = centerX + gap

        
        val baseR = dp(6.8f)
        val maxJump = dp(14f)

        
        val idle = 0.16f + 0.06f * sin(t)

        
        val speak = (smoothAmp * 1.25f).coerceIn(0f, 1f)

        
        val p1 = idle + speak * wave01(t + 0.0f)
        val p2 = idle + speak * wave01(t + 0.9f)
        val p3 = idle + speak * wave01(t + 1.8f)

        val y1 = cy - bezierArcY(p1.coerceIn(0f, 1f)) * maxJump
        val y2 = cy - bezierArcY(p2.coerceIn(0f, 1f)) * maxJump
        val y3 = cy - bezierArcY(p3.coerceIn(0f, 1f)) * maxJump

        
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
