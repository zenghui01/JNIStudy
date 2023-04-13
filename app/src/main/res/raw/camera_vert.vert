// 把顶点坐标给这个变量， 确定要画画的形状
//字节定义的  4个   数组  矩阵
attribute vec4 vPosition;//0
//cpu
//接收纹理坐标，接收采样器采样图片的坐标  camera
attribute vec4 vCoord;
// oepngl 坐标系和 camre 的坐标系是不一样的，所以需要进行转换
uniform mat4 vMatrix;
// 易变变量，顶点着色器输出数据，是从顶点着色器传递到片元着色器的数据变量
// 简单的说如果这个变量需要在顶点程序和片元程序中传递数据，那么就用 varying 进行修饰
// 这里我们需要传递坐标，x,y 使用 vec2 即可
varying vec2 aCoord;
void main(){
    // gpu  需要渲染的 什么图像   形状
    gl_Position=vPosition;
    // 这里每个 GPU 块都会去执行，当这个值发生变化 camera_frag1.frag 中的 aCoord 也会相应发生变化
    aCoord= (vMatrix * vCoord).xy;
}
