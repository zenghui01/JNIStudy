//package com.testndk.jnistudy.ui.weight
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.util.AttributeSet
//import android.view.View
//
//class CustomView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyleAttr: Int = 0
//) :
//    View(context, attrs, defStyleAttr) {
//    var offY = 100f
//    val paint by lazy(LazyThreadSafetyMode.NONE) {
//        Paint(Paint.ANTI_ALIAS_FLAG).apply {
//            color = Color.parseColor("#000000")
//            style = Paint.Style.FILL_AND_STROKE
//            strokeWidth = 30f
//        }
//    }
//
//    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)
//        if (offY > 800f) {
//            offY = 100f
//        }
//        canvas?.drawPoint(100f, offY, paint)
//        offY += 1f
//        invalidate()
//    }
//
//}