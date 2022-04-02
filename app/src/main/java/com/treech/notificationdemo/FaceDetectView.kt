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

    private var mLeftTopRadius = 0f
    private var mRightTopRadius = 0f
    private var mRightBottomRadius = 0f
    private var mLeftBottomRadius = 0f

    private var mFillColor = Color.TRANSPARENT
    private var mStrokeColor = Color.TRANSPARENT
    private var mStrokeWidth = 0f
    private val mFillPaint = Paint()
    private val mStrokePaint = Paint()
    private var boxBorderWidth: Float
    private var boxBorderColor: Int

    private val mPath = Path()

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
                mFillColor = getColor(R.styleable.FaceDetectView_fv_fill_color, mFillColor)
            }
            if (hasValue(R.styleable.FaceDetectView_fv_stroke_color)) {
                mStrokeColor = getColor(R.styleable.FaceDetectView_fv_stroke_color, mStrokeColor)
            }
            if (hasValue(R.styleable.FaceDetectView_fv_stroke_width)) {
                mStrokeWidth = getDimension(R.styleable.FaceDetectView_fv_stroke_width, 0f)
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
        mFillPaint.style = Paint.Style.FILL
        mFillPaint.isAntiAlias = true
        mFillPaint.color = mFillColor
        mStrokePaint.style = Paint.Style.STROKE
        mStrokePaint.isAntiAlias = true
        mStrokePaint.color = mStrokeColor
        // Stroke是沿着最外层边界画的，有一半会延伸到可视界面以外
        // 此处手动设置两倍的StrokeWidth就是为了达到边缘显示效果
        mStrokePaint.strokeWidth = mStrokeWidth * 2
    }

    @Px
    fun getLeftTopRadius(): Float {
        return mLeftTopRadius
    }

    fun setLeftTopRadius(@Px leftTopRadius: Float) {
        if (mLeftTopRadius != leftTopRadius) {
            mLeftTopRadius = leftTopRadius
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @Px
    fun getRightTopRadius(): Float {
        return mRightTopRadius
    }

    fun setRightTopRadius(@Px rightTopRadius: Float) {
        if (mRightTopRadius != rightTopRadius) {
            mRightTopRadius = rightTopRadius
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @Px
    fun getRightBottomRadius(): Float {
        return mRightBottomRadius
    }

    fun setRightBottomRadius(@Px rightBottomRadius: Float) {
        if (mRightBottomRadius != rightBottomRadius) {
            mRightBottomRadius = rightBottomRadius
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @Px
    fun getLeftBottomRadius(): Float {
        return mLeftBottomRadius
    }

    fun setLeftBottomRadius(@Px leftBottomRadius: Float) {
        if (mLeftBottomRadius != leftBottomRadius) {
            mLeftBottomRadius = leftBottomRadius
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @ColorInt
    fun getFillColor(): Int {
        return mFillColor
    }

    fun setFillColor(@ColorInt fillColor: Int) {
        if (mFillColor != fillColor) {
            mFillColor = fillColor
            mFillPaint.color = mFillColor
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @ColorInt
    fun getStrokeColor(): Int {
        return mStrokeColor
    }

    fun setStrokeColor(@ColorInt strokeColor: Int) {
        if (mStrokeColor != strokeColor) {
            mStrokeColor = strokeColor
            mStrokePaint.color = mStrokeColor
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    @Px
    fun getStrokeWidth(): Int {
        return mStrokeWidth.toInt()
    }

    fun setStrokeWidth(@Px strokeWidth: Float) {
        if (mStrokeWidth != strokeWidth) {
            mStrokeWidth = strokeWidth.toFloat()
            // Stroke是沿着最外层边界画的，有一半会延伸到可视界面以外
            // 此处手动设置两倍的StrokeWidth就是为了达到边缘显示效果
            mStrokePaint.strokeWidth = mStrokeWidth * 2
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
        var dw = width * 1.0f - mStrokeWidth * 2
        var dh = height * 1.0f - mStrokeWidth * 2
        if (dh > height - mStrokeWidth * 2) {
            dh = height * 1.0f - mStrokeWidth * 2
            dw = dh * bitmap.width / bitmap.height
        }

        previewImageRect.set((width - dw) / 2.0f,
            (height - dh) / 2.0f,
            (width + dw) / 2.0f,
            (height + dh) / 2.0f)

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val maxLeft = max(mLeftTopRadius, mLeftBottomRadius)
        val maxRight = max(mRightTopRadius, mRightBottomRadius)
        val minWidth = maxLeft + maxRight
        val maxTop = max(mLeftTopRadius, mRightTopRadius)
        val maxBottom = max(mLeftBottomRadius, mRightBottomRadius)
        val minHeight = maxTop + maxBottom
        // 只有图片的宽高大于设置的圆角距离的时候才进行裁剪
        if (width >= minWidth && height >= minHeight) {
            mPath.reset()
            // 四个角：右上，右下，左下，左上
            mPath.moveTo(mLeftTopRadius, 0f)
            mPath.lineTo((width - mRightTopRadius), 0f)
            mPath.quadTo(width.toFloat(), 0f, width.toFloat(), mRightTopRadius)
            mPath.lineTo(width.toFloat(), (height - mRightBottomRadius))
            mPath.quadTo(width.toFloat(), height.toFloat(), (width - mRightBottomRadius), height.toFloat())
            mPath.lineTo(mLeftBottomRadius, height.toFloat())
            mPath.quadTo(0f, height.toFloat(), 0f, (height - mLeftBottomRadius))
            mPath.lineTo(0f, mLeftTopRadius)
            mPath.quadTo(0f, 0f, mLeftTopRadius, 0f)
            mPath.close()
            canvas.clipPath(mPath)
        }
        if (mFillColor != Color.TRANSPARENT) {
            canvas.drawPath(mPath, mFillPaint)
        }
        super.onDraw(canvas)

        if (mStrokeWidth > 0) {
            canvas.drawPath(mPath, mStrokePaint)
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