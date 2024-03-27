package com.testndk.jnistudy.ui.activity

import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.testndk.jnistudy.R
import com.testndk.jnistudy.ui.BaseActivity
import java.io.IOException

class CameraActivity : BaseActivity() {
   override fun initLayout(): Int {
       return R.layout.activity_camera
   }

   override fun initView() {
       super.initView()
       findViewById<SurfaceView>(R.id.svView).run {
           holder.run {
               setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
               addCallback(object : SurfaceHolder.Callback2 {
                   override fun surfaceRedrawNeeded(holder: SurfaceHolder) {}
                   override fun surfaceCreated(holder: SurfaceHolder) {
                       val camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
                       try {
                           camera.setPreviewDisplay(holder)
                       } catch (e: IOException) {
                           e.printStackTrace()
                       }
                       camera.startPreview()
                   }

                   override fun surfaceChanged(
                       holder: SurfaceHolder,
                       format: Int,
                       width: Int,
                       height: Int
                   ) {
                   }

                   override fun surfaceDestroyed(holder: SurfaceHolder) {}
               })
           }
       }
   }
}
