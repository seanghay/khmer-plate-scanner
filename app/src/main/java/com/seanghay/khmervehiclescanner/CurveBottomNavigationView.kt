package com.seanghay.khmervehiclescanner

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.core.animation.doOnCancel
import com.google.android.material.bottomnavigation.BottomNavigationView


class CurveBottomNavigationView : BottomNavigationView {

    private var animRef: ValueAnimator? = null

    var curveRadius: Float
        set(value) {
            backgroundDrawable.radius = value
            backgroundDrawable.invalidateSelf()
        }
        get() = backgroundDrawable.radius

    var animateRadius: Float
        set(value) {
            animRef?.cancel()
            animRef = ValueAnimator.ofFloat(backgroundDrawable.radius, value).apply {
                addUpdateListener {
                    backgroundDrawable.radius = it.animatedValue as Float
                    backgroundDrawable.invalidateSelf()
                }

                doOnCancel {
                    backgroundDrawable.radius = value
                    backgroundDrawable.invalidateSelf()
                }

                duration = 250
                start()
            }
        }
        get() = backgroundDrawable.radius

    private val backgroundDrawable =
        CurveDrawable(adjustAlpha(backgroundColor(), 0.9f), 0f)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrSet: AttributeSet) : super(context, attrSet)
    constructor(context: Context, attrSet: AttributeSet, defStyle: Int) : super(
        context,
        attrSet,
        defStyle
    )

    init {
        background = backgroundDrawable
    }

    @ColorInt
    fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun backgroundColor(): Int {
        val typedValue = TypedValue()
        val a =
            context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorSurface))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    private fun dpToPx(dp: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        )
    }

    class CurveDrawable(
        @ColorInt private var color: Int,
        radius: Float
    ) : Drawable() {

        private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rectF: RectF = RectF()
        private val path: Path = Path()


        private var firstStartPoint = PointF()
        private var firstEndPoint = PointF()
        private var secondStartPaint = PointF()
        private var secondEndPoint = PointF()
        private var firstControlPoint = PointF()
        private var firstControlPoint2 = PointF()
        private var secondControlPoint1 = PointF()
        private var secondControlPoint2 = PointF()

        var radius: Float = radius
            set(value) {
                field = value
                applyChanges()
            }


        init {
            paint.style = Paint.Style.FILL
            path.fillType = Path.FillType.EVEN_ODD
        }


        private fun applyChanges() {
            path.reset()
            rectF.set(bounds)

            val width = rectF.width()
            val height = rectF.height()

            firstStartPoint.set((width / 2f) - (radius * 2f) - (radius / 3f), 0f)
            firstEndPoint.set(width / 2, radius + (radius / 4))
            secondStartPaint = firstEndPoint
            secondEndPoint.set((width / 2) + (radius * 2) + (radius / 3), 0f)
            firstControlPoint.set(
                firstStartPoint.x + radius + (radius / 4),
                firstStartPoint.y
            )
            firstControlPoint2.set(
                firstEndPoint.x - (radius * 2) + radius,
                firstEndPoint.y
            )
            secondControlPoint1.set(
                secondStartPaint.x + (radius * 2) - radius,
                secondStartPaint.y
            )
            secondControlPoint2.set(
                secondEndPoint.x - (radius + (radius / 4)),
                secondEndPoint.y
            )


            path.reset()
            path.moveTo(0f, 0f)
            path.lineTo(firstStartPoint.x, firstStartPoint.y)

            path.cubicTo(
                firstControlPoint.x, firstControlPoint.y,
                firstControlPoint2.x, firstControlPoint2.y,
                firstEndPoint.x, firstEndPoint.y
            );

            path.cubicTo(
                secondControlPoint1.x, secondControlPoint1.y,
                secondControlPoint2.x, secondControlPoint2.y,
                secondEndPoint.x, secondEndPoint.y
            )

            path.lineTo(width, 0f)
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()
        }

        override fun onBoundsChange(bounds: Rect?) {
            if (bounds == null) return
            applyChanges()
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSLUCENT
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun draw(canvas: Canvas) {
            paint.color = color
            canvas.drawPath(path, paint)
        }
    }
}