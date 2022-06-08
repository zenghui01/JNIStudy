package com.testndk.jnistudy.ui.weight

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.View.LAYER_TYPE_HARDWARE
import androidx.core.view.ViewCompat.setLayerType


class DiyBackgroundCanvas(
    private val mContext: Context?,
    private val mWidth: Int,
    private var mHeight: Int
) : Canvas() {
    private var mBackGroupPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#F2121212")
    }
    private var mShaderPaint = Paint().apply {
        isAntiAlias = true
    }
    private var mClipPaint = Paint().apply {
        isAntiAlias = true
        color = Color.TRANSPARENT
    }
    private val largeBoxRadius = 20F
    private val centerBtmWidth = 200f
    private val topPadding = 60f
    private val largeBoxRadii =
        floatArrayOf(largeBoxRadius, largeBoxRadius, largeBoxRadius, largeBoxRadius, 0F, 0F, 0F, 0F)
    private val startX = (mWidth - centerBtmWidth) / 2
    private val endX = startX + centerBtmWidth
    private val centerRoundHeight = topPadding / 2

    private var mDefaultPath = Path().apply {
        if (mWidth <= 0 || mHeight <= 0 || startX <= 0) {
            return@apply
        }
        // 圆角大框
        addRoundRect(
            RectF(0F, topPadding, mWidth.toFloat(), mHeight.toFloat()),
            largeBoxRadii,
            Path.Direction.CW
        )
        // 中间圆角小框
        addRoundRect(RectF(startX, 0F, endX, topPadding), largeBoxRadii, Path.Direction.CW)
        // 中间左边小框
        addRect(
            RectF(startX - centerRoundHeight, centerRoundHeight, startX, topPadding),
            Path.Direction.CW
        )
        // 中间右边边小框
        addRect(
            RectF(
                endX + centerRoundHeight,
                centerRoundHeight,
                endX,
                topPadding
            ), Path.Direction.CCW
        )
    }

    fun getBitmapDrawable(view: View, shaderBitmap: Bitmap? = null): BitmapDrawable? {
        if (mWidth <= 0 || mHeight <= 0 || startX <= 0 || mContext == null) {
            return null
        }
        shaderBitmap?.let {
            if (!it.isRecycled) {
                mShaderPaint.shader =
                    BitmapShader(shaderBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            }
        }
        val bgBitmap = Bitmap.createBitmap(
            mWidth, mHeight,
            Bitmap.Config.ARGB_8888
        )
        if (bgBitmap == null || bgBitmap.isRecycled) {
            return null
        }
        setBitmap(bgBitmap)
        // 裁剪显示区域
        clipPath(mDefaultPath)
        // 绘制显示区域
        drawPath(mDefaultPath, mShaderPaint)
        // 绘制显示区域
        drawPath(mDefaultPath, mBackGroupPaint)

        // 设置 xfermode
        mClipPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        // 解决 xfermode 后白屏问题
        setLayerType(view, LAYER_TYPE_HARDWARE, null)
        mClipPaint.drawToCanvas()
        return BitmapDrawable(mContext.resources, bgBitmap)
    }

    private fun Paint.drawToCanvas() {
        // 裁切左边圆角
        drawRoundRect(
            RectF(startX - centerRoundHeight * 2, 0f, startX, topPadding),
            centerRoundHeight,
            centerRoundHeight,
            this
        )
        // 裁切右边边圆角
        drawRoundRect(
            RectF(endX, 0f, endX + centerRoundHeight * 2, topPadding),
            centerRoundHeight,
            centerRoundHeight,
            this
        )
    }
}