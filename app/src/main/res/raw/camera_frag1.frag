#extension GL_OES_EGL_image_external : require
//所有 float 类型数据的精度是 lowp
precision mediump float;
// 易变变量，顶点着色器输出数据，是从顶点着色器传递到片元着色器的数据变量
// 简单的说如果这个变量需要在顶点程序和片元程序中传递数据，那么就用 varying 进行修饰
// 这里我们需要传递坐标，x,y 使用 vec2 即可
varying vec2 aCoord;
// 采样器  uniform static
// 如何理解采样器？
// 我们摄像头采集过来的数据到 gpu 中，当我们要用过 OpenGL 渲染出来的时候就需要采样
// 采样器必须申明 #extension GL_OES_EGL_image_external : require
// uniform 类似于 static
uniform samplerExternalOES vTexture;
void main(){
    // Opengl 自带函数 texture2D
    // 通过这个方法获取到这个像素点的颜色
    vec4 rgba = texture2D(vTexture, aCoord);
    // 灰色  滤镜
    float color=(rgba.r + rgba.g + rgba.b) / 3.0;
    vec4 tempColor=vec4(color, color, color, 1);
    // 这里对 gl_FragColor 进行赋值就好了
    gl_FragColor=tempColor;

}