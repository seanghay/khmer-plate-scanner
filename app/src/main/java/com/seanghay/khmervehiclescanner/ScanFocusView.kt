package com.seanghay.khmervehiclescanner

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min





class ScanFocusView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attributeSet, defStyle) {

    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        val primaryColor = primaryColor()
        color = adjustAlpha(primaryColor, .3f)
    }


    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1f.dpToPx()
    }

    private val rect = RectF()

    private val path = Path()
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryColor()
        strokeWidth = 1.5f.dpToPx()
        style = Paint.Style.FILL_AND_STROKE
    }

    private val frameRadius = 6f.dpToPx()

    private var indicatorOffset = 0f
    private var indicatorFraction = 0f
    private var indicatorAnimator: ValueAnimator? = null


    private fun Float.dpToPx(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            resources.displayMetrics
        )
    }


    @ColorInt
    fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        path.reset()

        val frameWidth = w.toFloat() / 2f

        rect.left = frameWidth / 2f
        rect.right = rect.left + frameWidth

        rect.top = (h.toFloat() - frameWidth) / 2f
        rect.bottom = rect.top + frameWidth

        val radius = frameRadius
        val corners = floatArrayOf(
            radius, radius,
            radius, radius,
            radius, radius,
            radius, radius
        )

        path.addRoundRect(rect, corners, Path.Direction.CW)
        indicatorOffset = rect.width() / 2f

        indicatorAnimator?.cancel()
        indicatorAnimator = ValueAnimator.ofFloat(0f, frameWidth - frameRadius).apply {
            duration = 2500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE

            interpolator = LinearInterpolator()

            addUpdateListener {
                indicatorOffset = it.animatedValue as Float
                indicatorFraction = it.animatedFraction
                invalidate()
            }

            start()
        }

        path.close()
    }


    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return

        canvas.save()
        canvas.clipPath(path, Region.Op.DIFFERENCE)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        canvas.restore()
        // canvas.drawRect(rect, framePaint)

        canvas.drawPath(path, framePaint)

        var indicatorY = rect.top + indicatorOffset
        indicatorY = indicatorY.clamp(rect.top + frameRadius, rect.bottom - frameRadius)


        val f = abs(abs((indicatorFraction * 2f) - 1f) - 1f)
        indicatorPaint.alpha = (255 * f).toInt()

        canvas.drawLine(
            rect.left,
            indicatorY,
            rect.right,
            indicatorY,
            indicatorPaint
        )

    }


    @ColorInt
    private fun primaryColor(): Int {
        val typedValue = TypedValue()
        val ta = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimary))
        val color = ta.getColor(0, 0)
        ta.recycle()
        return color
    }

}

private fun Float.clamp(min: Float, max: Float): Float {
    return max(min, min(this, max))
}
