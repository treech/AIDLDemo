package com.treech.notificationdemo

import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.scene)
        findViewById<FaceDetectView>(R.id.iv_scene_picture).loadImage(bitmap, "294.286.426.408")
        val imageView2 = findViewById<ImageView>(R.id.iv_scene_picture2)

        imageView2.post {
            val left = 294f
            val top = 286f
            val right = 426f
            val bottom = 408f
            Log.d("ygq",
                "activity view w:${imageView2.width},h:${imageView2.height},rect w:${imageView2.width * 1.0f - dp2Px<Float>(2)},h:${imageView2.height * 1.0f - dp2Px<Float>(2)},bitmap w:${bitmap.width},h:${bitmap.height}")
            val temp = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val paint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = dp2Px(2)
                color = ContextCompat.getColor(this@SecondActivity, R.color.colorAccent)
            }
            val canvas = Canvas(temp)
            val rectF = RectF(left, top, right, bottom)
            canvas.drawRect(rectF, paint)
            imageView2.setImageBitmap(temp)
        }
    }
}