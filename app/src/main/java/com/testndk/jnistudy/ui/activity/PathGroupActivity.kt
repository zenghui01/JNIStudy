//package com.testndk.jnistudy.ui.activity
//
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.core.graphics.drawable.toBitmap
//import androidx.core.view.doOnLayout
//import com.testndk.jnistudy.R
//import com.testndk.jnistudy.ui.weight.DiyBackgroundCanvas
//
//
//class PathGroupActivity : BaseActivity() {
//    override fun initLayout() = R.layout.activity_path_group
//
//    override fun initView() {
//        super.initView()
//        findViewById<ConstraintLayout>(R.id.parent).doOnLayout {
//            val bitmap = it.resources.getDrawable(R.drawable.reference_pointcut).toBitmap()
//            it.background =
//                DiyBackgroundCanvas(it.context, it.width, it.height).getBitmapDrawable(it, bitmap)
//        }
//    }
//}