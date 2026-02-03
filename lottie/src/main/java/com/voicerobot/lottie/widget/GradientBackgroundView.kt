package com.voicerobot.lottie.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import com.voicerobot.lottie.R


class GradientBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    @ColorInt
    private var colorPink: Int = 0xFFFF4FD8.toInt()

    @ColorInt
    private var colorBlue: Int = 0xFF3D8BFF.toInt()

    private var shader: LinearGradient? = null
    private val shaderMatrix = Matrix()

    private var animator: ValueAnimator? = null

    
    private var progress: Float = 0f

    
    private var intensity: Float = 1f

    
    private var speedPxPerSec: Float = 180f

    init {
        
        context.withStyledAttributes(attrs, R.styleable.GradientBackgroundView) {
            intensity = getFloat(R.styleable.GradientBackgroundView_gbvIntensity, 1f)
            speedPxPerSec = getFloat(R.styleable.GradientBackgroundView_gbvSpeedPxPerSec, 180f)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimIfPossible()
    }

    override fun onDetachedFromWindow() {
        stopAnim()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        
        val gradientWidth = w * 2f
        shader = LinearGradient(
            0f, 0f,
            gradientWidth, h.toFloat(),
            intArrayOf(colorPink, colorBlue, colorPink),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = shader

        startAnimIfPossible()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        val s = shader ?: return

        
        val dx = (w * progress) * intensity
        shaderMatrix.reset()
        shaderMatrix.setTranslate(dx, 0f)
        s.setLocalMatrix(shaderMatrix)

        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    }

    fun setColors(@ColorInt pink: Int, @ColorInt blue: Int) {
        colorPink = pink
        colorBlue = blue
        
        requestLayout()
        invalidate()
    }

    
    fun setIntensity(value: Float) {
        intensity = value.coerceIn(0f, 1f)
        invalidate()
    }

    
    fun setSpeedPxPerSec(value: Float) {
        speedPxPerSec = value.coerceAtLeast(10f)
        restartAnim()
    }

    private fun startAnimIfPossible() {
        if (width <= 0 || height <= 0) return
        if (animator?.isStarted == true) return

        val w = width.coerceAtLeast(1)
        
        val durationMs = ((w / speedPxPerSec) * 1000f).toLong().coerceAtLeast(1200L)

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun restartAnim() {
        stopAnim()
        startAnimIfPossible()
    }

    private fun stopAnim() {
        animator?.cancel()
        animator = null
    }
}


