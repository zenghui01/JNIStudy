package com.testndk.jnistudy.ui.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.testndk.jnistudy.R
import com.testndk.jnistudy.ui.BaseActivity

class BitmapOptionActivity : BaseActivity() {
   override fun initLayout() = R.layout.activity_bitmap_option

   override fun initView() {
       val bitmap = BitmapFactory.decodeResource(
           resources,
           R.drawable.test_img,
           BitmapFactory.Options().apply {
               inDensity = 2160
               inScaled = true
               inTargetDensity = 1080
               inPreferredConfig = Bitmap.Config.RGB_565
           })
       findViewById<ImageView>(R.id.ivBitmap).setImageBitmap(bitmap)
   }
}
