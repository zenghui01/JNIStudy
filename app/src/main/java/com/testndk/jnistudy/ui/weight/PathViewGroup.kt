//package com.testndk.jnistudy.ui.weight
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.Path
//import android.graphics.PointF
//import android.graphics.PorterDuff
//import android.graphics.PorterDuffXfermode
//import android.graphics.RectF
//import android.util.AttributeSet
//import androidx.constraintlayout.widget.ConstraintLayout
//
//
//class PathViewGroup @JvmOverloads constructor(
//    private val mContext: Context,
//    attrs: AttributeSet? = null,
//    defStyleAttr: Int = -1
//) : ConstraintLayout(mContext, attrs, defStyleAttr) {
//    private var mBezierPointList: MutableList<PointF>? = null
//    val topPadding = 60F
//    val middleWidth = 60F
//    val mPaint = Paint()
//    val path = Path()
//    val largeBoxRadius = 20F
//    val cornerRadius40 = 40F
//    val centerBtmWidth = 200f
//    val largeBoxRadii = floatArrayOf(largeBoxRadius, largeBoxRadius, largeBoxRadius, largeBoxRadius, 0F, 0F, 0F, 0F)
//    val radii20 =
//        floatArrayOf(cornerRadius40, cornerRadius40, cornerRadius40, cornerRadius40, 0F, 0F, 0F, 0F)
//
//    init {
//        mPaint.isAntiAlias = true
//        mPaint.strokeWidth = 6F
//        mPaint.color = Color.BLUE
//    }
//
//
//    private fun Canvas?.drawScene() {
//        if (this == null) {
//            return
//        }
//        drawRect(0f, 0f, width.toFloat(), height.toFloat(), mPaint)
//    }
//
//    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)
//        val startX = (width - centerBtmWidth) / 2
//        val endX = startX + centerBtmWidth
//        val w = width.toFloat()
//        val h = height.toFloat()
//        val path = Path()
//        val rectF = RectF(0F, topPadding, w, h)
//        path.addRoundRect(
//            rectF,
//            largeBoxRadii,
//            Path.Direction.CW
//        )
//        val rectTop = RectF(startX, 0F, endX, h)
//        path.addRoundRect(rectTop, largeBoxRadii, Path.Direction.CW)
//
//        val centerRoundHeight = topPadding / 2
//        val centerRectF =
//            RectF(startX - centerRoundHeight, centerRoundHeight, startX, topPadding)
//        path.addRect(centerRectF, Path.Direction.CW)
//
//        val centerRectF2 =
//            RectF(
//                endX + centerRoundHeight,
//                centerRoundHeight,
//                endX,
//                topPadding
//            )
//        path.addRect(centerRectF2, Path.Direction.CCW)
//
//        canvas?.clipPath(path)      //第一个
//        canvas.drawScene()
//        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
//        setLayerType(LAYER_TYPE_HARDWARE, null)
//        val circleRectF =
//            RectF(startX - centerRoundHeight * 2, 0f, startX, topPadding)
//        canvas?.drawRoundRect(circleRectF, centerRoundHeight, centerRoundHeight, mPaint)
//
//        val circleRectF2 =
//            RectF(endX, 0f, endX + centerRoundHeight * 2, topPadding)
//        canvas?.drawRoundRect(circleRectF2, centerRoundHeight, centerRoundHeight, mPaint)
//    }
//
//}