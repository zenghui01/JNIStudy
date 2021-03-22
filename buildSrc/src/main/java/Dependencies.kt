/**
 * android buildSrc
 */
object Versions {
    val compileSdkVersion = 28
    val buildToolsVersion = "28.0.2"
    val minSdkVersion = 21
    val targetSdkVersion = 28
    val versionCode = 2
    val versionName = "v2.0"
}

object Commons {
    // https://mvnrepository.com/artifact/com.alibaba/fastjson
    val fastJson = "com.alibaba:fastjson:1.1.71.android"
    val viewpager2 = "androidx.viewpager2:viewpager2:1.0.0"
    val base_recycler_view_adapter_helper = "com.github.CymChad:BaseRecyclerViewAdapterHelper:2.9.34"
}

object ImageBrowser {
    val photoView = "com.github.chrisbanes:PhotoView:2.3.0"
    val subScaleImg = "com.davemorrissey.labs:subsampling-scale-image-view-androidx:3.10.0"
}

object ImageLibs {
    val GLIDE_VERSION = "4.9.0"
    val glide = "com.github.bumptech.glide:glide:${GLIDE_VERSION}"
    val glide_compiler = "com.github.bumptech.glide:compiler:${GLIDE_VERSION}"
    val glide_transformations = "jp.wasabeef:glide-transformations:4.0.1"
}

object TestModel {
    val junit = "junit:junit:4.12";
}