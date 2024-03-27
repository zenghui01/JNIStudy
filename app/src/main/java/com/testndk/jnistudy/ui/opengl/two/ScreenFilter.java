//package com.testndk.jnistudy.ui.opengl.two;
//
//import static android.opengl.GLES20.GL_FLOAT;
//import static android.opengl.GLES20.GL_TEXTURE0;
//
//import android.content.Context;
//import android.opengl.GLES20;
//
//import com.testndk.jnistudy.R;
//
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.FloatBuffer;
//
//public class ScreenFilter {
//    /**
//     * opengl 程序
//     */
//    private int program;
//    /**
//     * 句柄  gpu中  vPosition
//     */
//    private int vPosition;
//    FloatBuffer textureBuffer;
//    /**
//     * 纹理坐标
//     */
//    private int vCoord;
//    private int vTexture;
//    private int vMatrix;
//    private int mWidth;
//    private int mHeight;
//    private float[] mtx;
//    /**
//     * gpu顶点缓冲区
//     */
//    FloatBuffer vertexBuffer;
//    /**
//     * 顶点坐标缓存区
//     */
//    float[] VERTEX = {
//            -1.0f, -1.0f,
//            1.0f, -1.0f,
//            -1.0f, 1.0f,
//            1.0f, 1.0f
//    };
//
//    float[] TEXTURE = {
//            0.0f, 0.0f,
//            1.0f, 0.0f,
//            0.0f, 1.0f,
//            1.0f, 1.0f
//    };
//
//    public ScreenFilter(Context context) {
//        /**
//         * 为什么要这样申请？大小为什么是 4*4*2? 调用 order 是为了什么？
//         * 1. 为什么要这样申请？
//         * 由于我们什么内存的之后如果直接调用 allocate 方法申请，内存不是连续的而我们这里需要连续的内存
//         * 2. 为什么大小是 4 * 4 * 2
//         * 由于我们顶点程序 vPosition 要求的是 vec4 就需要传递 4 个坐标，由于类型是 float 占用内存 4 个字节，每个坐标又有 x 和 y
//         * 所以需要的内存大小就为 4 * 4 * 2
//         * 3. 为什么调用 order
//         * 这里是防止连续申请的内存混乱，重新进行排序
//         */
//        vertexBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
//        vertexBuffer.clear();
//        vertexBuffer.put(VERTEX);
//
//        textureBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
//        textureBuffer.clear();
//        textureBuffer.put(TEXTURE);
//        // 获取到顶点着色器程序字符串
//        String vertexShader = OpenGLUtils.readRawTextFile(context, R.raw.camera_vert);
//        //  先编译    再链接   再运行  程序
//        String fragShader = OpenGLUtils.readRawTextFile(context, R.raw.camera_frag1);
//        // cpu 1  没有用  索引     program gpu
//        program = OpenGLUtils.loadProgram(vertexShader, fragShader);
//        // 通过 program 获取到顶点着色器 vPosition 变量
//        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
//        // 接收纹理坐标，接收采样器采样图片的坐标
//        vCoord = GLES20.glGetAttribLocation(program, "vCoord");
//        // 采样点的坐标
//        vTexture = GLES20.glGetUniformLocation(program, "vTexture");
//        // 变换矩阵， 需要将原本的vCoord（01,11,00,10） 与矩阵相乘
//        vMatrix = GLES20.glGetUniformLocation(program, "vMatrix");
//        // 构造 的时候 给 数据  vPosition gpu 是1  不是 2
//    }
//
//    public void setSize(int width, int height) {
//        mWidth = width;
//        mHeight = height;
//
//    }
//
//    public void setTransformMatrix(float[] mtx) {
//        this.mtx = mtx;
//    }
//
//    public void onDraw(int texture) {
//        // opengl
//        // View 的大小
//        GLES20.glViewport(0, 0, mWidth, mHeight);
//        // 使用程序
//        GLES20.glUseProgram(program);
//        // 从索引位0的地方读
//        vertexBuffer.position(0);
//        /**
//         * glVertexAttribPointer
//         * index 指定要修改的通用顶点属性的索引。
//         * size 指定每个通用顶点属性的组件数。
//         * type 指定数组中每个组件的数据类型。接受符号常量 GL_FLOAT、GL_BYTE、GL_UNSIGNED_BYTE、GL_SHORT、GL_UNSIGNED_SHORT或GL_FIXED。 初始值为GL_FLOAT。
//         * normalized  指定在访问定点数据值时是应将其标准化（GL_TRUE）还是直接转换为定点值（GL_FALSE）
//         * 如何理解 normalized
//         * 就是 normalized 为 ture 当你坐标系传入了 -2f 会默认转换成 -1f
//         */
//        GLES20.glVertexAttribPointer(vPosition, 2, GL_FLOAT, false, 0, vertexBuffer);
//        // 生效
//        GLES20.glEnableVertexAttribArray(vPosition);
//
//        textureBuffer.position(0);
//        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT,
//                false, 0, textureBuffer);
//        //CPU传数据到GPU，默认情况下着色器无法读取到这个数据。 需要我们启用一下才可以读取
//        GLES20.glEnableVertexAttribArray(vCoord);
//        GLES20.glActiveTexture(GL_TEXTURE0);
//
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
//        GLES20.glUniform1i(vTexture, 0);
//        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0);
//        // 通知绘制
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//    }
//}
//
