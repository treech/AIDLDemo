package com.treech.notificationdemo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import kotlinx.coroutines.*
import kotlin.math.max

class FaceDetectView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val previewImageRect: RectF = RectF()
    private val faceRect: RectF = RectF()

    private var previewBitmap: Bitmap? = null

    private var locations: List<String>? = null

    private var leftTopRadius = 0f
    private var rightTopRadius = 0f
    private var rightBottomRadius = 0f
    private var leftBottomRadius = 0f

    private var fillColor = Color.TRANSPARENT
    private var strokeColor = Color.TRANSPARENT
    private var strokeWidth = 0f
    private val fillPaint = Paint()
    private val strokePaint = Paint()
    private var boxBorderWidth: Float
    private var boxBorderColor: Int

    private val path = Path()

    private val paint = Paint()

    //人像框paint
    private lateinit var boxPaint: Paint

    init {
        context.obtainStyledAttributes(attrs, R.styleable.FaceDetectView).apply {
            boxBorderWidth = getDimension(R.styleable.FaceDetectView_fvBorderWidth, 3f)
            boxBorderColor = getColor(R.styleable.FaceDetectView_fvBorderColor,
                ContextCompat.getColor(context, R.color.colorAccent))

            if (hasValue(R.styleable.FaceDetectView_fvRadius)) {
                val radius = getDimension(R.styleable.FaceDetectView_fvRadius, 0f)
                setLeftTopRadius(radius)
                setRightTopRadius(radius)
                setRightBottomRadius(radius)
                setLeftBottomRadius(radius)
            }
            if (hasValue(R.styleable.FaceDetectView_fv_left_top_radius)) {
                val radius = getDimension(R.styleable.FaceDetectView_fv_left_top_radius, 0f)
                setLeftTopRadius(radius)
            }
            if (hasValue(R.styleable.FaceDetectView_fv_right_top_radius)) {
                val radius = getDimension(R.styleable.FaceDetectView_fv_right_top_radius, 0f)
                setRightTopRadius(radius)
            }
            if (hasValue(R.styleable.FaceDetectView_fv_right_bottom_radius)) {
                val radius = getDimension(R.styleable.FaceDetectView_fv_right_bottom_radius, 0f)
                setRightBottomRadius(radius)
            }
            if (hasValue(R.styleable.FaceDetectView_fv_left_bottom_radius)) {
                val radius = getDimension(R.styleable.FaceDetectView_fv_left_bottom_radius, 0f)
                setLeftBottomRadius(radius)
            }
            if (hasValue(R.styleable.FaceDetectView_fv_fill_color)) {
                fillColor = getColor(R.styleable.FaceDetectView_fv_fill_color, fillColor)
            }
            if (hasValue(R.styleable.FaceDetectView_fv_stroke_color)) {
                strokeColor = getColor(R.styleable.FaceDetectView_fv_stroke_color, strokeColor)
            }
            if (hasValue(R.styleable.FaceDetectView_fv_stroke_width)) {
                strokeWidth = getDimension(R.styleable.FaceDetectView_fv_stroke_width, 0f)
            }
            recycle()
        }
        initPaint()
    }

    private fun initPaint() {
        boxPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
            style = Paint.Style.STROKE
            color = boxBorderColor
            strokeWidth = boxBorderWidth
        }
        fillPaint.style = Paint.Style.FILL
        fillPaint.isAntiAlias = true
        fillPaint.color = fillColor
        strokePaint.style = Paint.Style.STROKE
        strokePaint.isAntiAlias = true
        strokePaint.color = strokeColor
        // Stroke是沿着最外层边界画的，有一半会延伸到可视界面以外
        // 此处手动设置两倍的StrokeWidth就是为了达到边缘显示效果
        strokePaint.strokeWidth = strokeWidth * 2
    }

    @Px
    fun getLeftTopRadius(): Float {
        return leftTopRadius
    }

    fun setLeftTopRadius(@Px leftTopRadius: Float) {
        if (this.leftTopRadius != leftTopRadius) {
            this.leftTopRadius = leftTopRadius
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @Px
    fun getRightTopRadius(): Float {
        return rightTopRadius
    }

    fun setRightTopRadius(@Px rightTopRadius: Float) {
        if (this.rightTopRadius != rightTopRadius) {
            this.rightTopRadius = rightTopRadius
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @Px
    fun getRightBottomRadius(): Float {
        return rightBottomRadius
    }

    fun setRightBottomRadius(@Px rightBottomRadius: Float) {
        if (this.rightBottomRadius != rightBottomRadius) {
            this.rightBottomRadius = rightBottomRadius
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @Px
    fun getLeftBottomRadius(): Float {
        return leftBottomRadius
    }

    fun setLeftBottomRadius(@Px leftBottomRadius: Float) {
        if (this.leftBottomRadius != leftBottomRadius) {
            this.leftBottomRadius = leftBottomRadius
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @ColorInt
    fun getFillColor(): Int {
        return fillColor
    }

    fun setFillColor(@ColorInt fillColor: Int) {
        if (this.fillColor != fillColor) {
            this.fillColor = fillColor
            fillPaint.color = this.fillColor
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @ColorInt
    fun getStrokeColor(): Int {
        return strokeColor
    }

    fun setStrokeColor(@ColorInt strokeColor: Int) {
        if (this.strokeColor != strokeColor) {
            this.strokeColor = strokeColor
            strokePaint.color = this.strokeColor
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @Px
    fun getStrokeWidth(): Int {
        return strokeWidth.toInt()
    }

    fun setStrokeWidth(@Px strokeWidth: Float) {
        if (this.strokeWidth != strokeWidth) {
            this.strokeWidth = strokeWidth.toFloat()
            // Stroke是沿着最外层边界画的，有一半会延伸到可视界面以外
            // 此处手动设置两倍的StrokeWidth就是为了达到边缘显示效果
            strokePaint.strokeWidth = this.strokeWidth * 2
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun loadImage(url: String, location: String) {
        post {
            scope.launch {
                previewBitmap = BitmapUtil.getBitmap(url)
                locations = location.split(".")
                calculatePreviewImageRect()
            }
        }
    }

    fun loadImage(bitmap: Bitmap, location: String) {
        post {
            scope.launch {
                previewBitmap = bitmap
                locations = location.split(".")
                calculatePreviewImageRect()
            }
        }
    }

    private fun calculatePreviewImageRect() {
        val bitmap = previewBitmap ?: return
        var dw = width * 1.0f - strokeWidth * 2
        var dh = height * 1.0f - strokeWidth * 2
        if (dh > height - strokeWidth * 2) {
            dh = height * 1.0f - strokeWidth * 2
            dw = dh * bitmap.width / bitmap.height
        }

        previewImageRect.set((width - dw) / 2.0f,
            (height - dh) / 2.0f,
            (width + dw) / 2.0f,
            (height + dh) / 2.0f)

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val maxLeft = max(leftTopRadius, leftBottomRadius)
        val maxRight = max(rightTopRadius, rightBottomRadius)
        val minWidth = maxLeft + maxRight
        val maxTop = max(leftTopRadius, rightTopRadius)
        val maxBottom = max(leftBottomRadius, rightBottomRadius)
        val minHeight = maxTop + maxBottom
        // 只有图片的宽高大于设置的圆角距离的时候才进行裁剪
        if (width >= minWidth && height >= minHeight) {
            path.reset()
            // 四个角：右上，右下，左下，左上
            path.moveTo(leftTopRadius, 0f)
            path.lineTo((width - rightTopRadius), 0f)
            path.quadTo(width.toFloat(), 0f, width.toFloat(), rightTopRadius)
            path.lineTo(width.toFloat(), (height - rightBottomRadius))
            path.quadTo(width.toFloat(), height.toFloat(), (width - rightBottomRadius), height.toFloat())
            path.lineTo(leftBottomRadius, height.toFloat())
            path.quadTo(0f, height.toFloat(), 0f, (height - leftBottomRadius))
            path.lineTo(0f, leftTopRadius)
            path.quadTo(0f, 0f, leftTopRadius, 0f)
            path.close()
            canvas.clipPath(path)
        }
        if (fillColor != Color.TRANSPARENT) {
            canvas.drawPath(path, fillPaint)
        }
        super.onDraw(canvas)

        if (strokeWidth > 0) {
            canvas.drawPath(path, strokePaint)
        }

        safeLet(locations, previewBitmap) { coordinates, bitmap ->
            if (previewImageRect.isEmpty) return@safeLet
            canvas.drawBitmap(bitmap, null, previewImageRect, paint)
            if (coordinates.size < 4) return@safeLet
            val left = coordinates[0].toFloat()
            val top = coordinates[1].toFloat()
            val width = coordinates[2].toFloat() - left
            val height = coordinates[3].toFloat() - top
            val faceRectLeft = left * previewImageRect.width() / previewBitmap!!.width + (getWidth() - previewImageRect.width()) / 2
            val faceRectTop = top * previewImageRect.height() / previewBitmap!!.height + (getHeight() - previewImageRect.height()) / 2
            val faceRectRight = width * previewImageRect.width() * 1.0f / previewBitmap!!.width + faceRectLeft
            val faceRectBottom = height * previewImageRect.height() * 1.0f / previewBitmap!!.height + faceRectTop
            faceRect.set(faceRectLeft, faceRectTop, faceRectRight, faceRectBottom)
            Log.d("ygq",
                "faceDetectView w:${getWidth()},h:${getHeight()},rect w:${previewImageRect.width()},w:${previewImageRect.height()},bitmap w:${previewBitmap!!.width},h:${previewBitmap!!.height}")
            canvas.drawRect(faceRect, boxPaint)
        }
    }

    override fun onDetachedFromWindow() {
        scope.cancel()
        super.onDetachedFromWindow()
    }
}

fun <T1 : Any, T2 : Any, R : Any> safeLet(
    p1: T1?, p2: T2?,
    block: (T1, T2) -> R?,
): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}