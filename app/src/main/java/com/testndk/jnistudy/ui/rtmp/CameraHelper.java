package com.testndk.jnistudy.ui.rtmp;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.testndk.jnistudy.utils.LogUtils;

import java.util.Iterator;
import java.util.List;

public class CameraHelper implements Camera.PreviewCallback, SurfaceHolder.Callback {
    private static final String TAG = "CameraHelper";
    public int mCameraID;
    public int mWidth;
    public int mHeight;
    private byte[] cameraBuffer;
    private byte[] cameraBuffer_;

    private Activity mActivity;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    private Camera.PreviewCallback mPreviewCallback;
    private OnChangedSizeListener mOnChangedSizeListener;
    private int mRotation;

    public CameraHelper(Activity activity, int cameraId, int width, int height) {
        mActivity = activity;
        mCameraID = cameraId;
        mWidth = width;
        mHeight = height;
    }


    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        if (mSurfaceHolder != null) {
            mSurfaceHolder.removeCallback(this);
            mSurfaceHolder.addCallback(this);
        }
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        stopPreview();
        startPreview();
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        try {
            //获得camera对象
            mCamera = Camera.open(mCameraID);
            //配置camera的属性
            Camera.Parameters parameters = mCamera.getParameters();
            //设置预览数据格式为nv21
            parameters.setPreviewFormat(ImageFormat.NV21);
            //这是摄像头宽、高
            setPreviewSize(parameters);
            // 设置摄像头 图像传感器的角度、方向
            setPreviewOrientation(parameters);
            mCamera.setParameters(parameters);
            cameraBuffer = new byte[mWidth * mHeight * 3 / 2];
            cameraBuffer_ = new byte[mWidth * mHeight * 3 / 2];
            //数据缓存区
            mCamera.addCallbackBuffer(cameraBuffer);
            mCamera.setPreviewCallbackWithBuffer(this);
            //设置预览画面
            mCamera.setPreviewDisplay(mSurfaceHolder);
            //            if (mOnChangedSizeListener != null) {
            //                mOnChangedSizeListener.onChanged(mWidth, mHeight);
            //            }
            //开启预览
            mCamera.startPreview();
            LogUtils.eLog("相机开启成功");
        } catch (Exception e) {
            LogUtils.eLog("相机开启失败",e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            //预览数据回调接口
            mCamera.setPreviewCallback(null);
            //停止预览
            mCamera.stopPreview();
            //释放摄像头
            mCamera.release();
            mCamera = null;
        }
    }

    private void setPreviewSize(Camera.Parameters parameters) {
        //获取摄像头支持的宽、高
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = supportedPreviewSizes.get(0);
        Log.d(TAG, "Camera支持: " + size.width + "x" + size.height);
        //选择一个与设置的差距最小的支持分辨率
        int m = Math.abs(size.width * size.height - mWidth * mHeight);
        supportedPreviewSizes.remove(0);
        Iterator<Camera.Size> iterator = supportedPreviewSizes.iterator();
        //遍历
        while (iterator.hasNext()) {
            Camera.Size next = iterator.next();
            Log.d(TAG, "支持 " + next.width + "x" + next.height);
            int n = Math.abs(next.height * next.width - mWidth * mHeight);
            if (n < m) {
                m = n;
                size = next;
            }
        }
        mWidth = size.width;
        mHeight = size.height;
        parameters.setPreviewSize(mWidth, mHeight);
        Log.d(TAG, "预览分辨率 width:" + mWidth + " height:" + mHeight);
    }

    private void setPreviewOrientation(Camera.Parameters parameters) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
        mRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (mRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                if (mOnChangedSizeListener != null) {
                    mOnChangedSizeListener.onChanged(mHeight, mWidth);
                }
                break;
            case Surface.ROTATION_90: // 横屏 左边是头部(home键在右边)
                degrees = 90;
                if (mOnChangedSizeListener != null) {
                    mOnChangedSizeListener.onChanged(mWidth, mHeight);
                }
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                if (mOnChangedSizeListener != null) {
                    mOnChangedSizeListener.onChanged(mHeight, mWidth);
                }
                break;
            case Surface.ROTATION_270:// 横屏 头部在右边
                degrees = 270;
                if (mOnChangedSizeListener != null) {
                    mOnChangedSizeListener.onChanged(mWidth, mHeight);
                }
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        //设置角度, 参考源码注释
        mCamera.setDisplayOrientation(result);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtils.eLog("surfaceCreated打开推流预览");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtils.eLog("surfaceChanged推流预览宽高变动");
        //释放摄像头
        stopPreview();
        //开启摄像头
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtils.eLog("surfaceDestroyed销毁推流预览");
        stopPreview();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        switch (mRotation) {
            case Surface.ROTATION_0:
                rotation90(data);
                break;
            case Surface.ROTATION_90: // 横屏 左边是头部(home键在右边)
                break;
            case Surface.ROTATION_270:// 横屏 头部在右边
                break;
        }
        if (mPreviewCallback != null) {
            //            mPreviewCallback.onPreviewFrame(data, camera);
            mPreviewCallback.onPreviewFrame(cameraBuffer_, camera);
        }
        camera.addCallbackBuffer(cameraBuffer);
    }

    private void rotation90(byte[] data) {
        int index = 0;
        int ySize = mWidth * mHeight;
        //u和v
        int uvHeight = mHeight / 2;
        //后置摄像头顺时针旋转90度
        if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            //将y的数据旋转之后 放入新的byte数组
            for (int i = 0; i < mWidth; i++) {
                for (int j = mHeight - 1; j >= 0; j--) {
                    cameraBuffer_[index++] = data[mWidth * j + i];
                }
            }

            //每次处理两个数据
            for (int i = 0; i < mWidth; i += 2) {
                for (int j = uvHeight - 1; j >= 0; j--) {
                    // v
                    cameraBuffer_[index++] = data[ySize + mWidth * j + i];
                    // u
                    cameraBuffer_[index++] = data[ySize + mWidth * j + i + 1];
                }
            }
        } else {
            //逆时针旋转90度
            //            for (int i = 0; i < mWidth; i++) {
            //                for (int j = 0; j < mHeight; j++) {
            //                    cameraBuffer_[index++] = data[mWidth * j + mWidth - 1 - i];
            //                }
            //            }
            //            //  u v
            //            for (int i = 0; i < mWidth; i += 2) {
            //                for (int j = 0; j < uvHeight; j++) {
            //                    cameraBuffer_[index++] = data[ySize + mWidth * j + mWidth - 1 - i - 1];
            //                    cameraBuffer_[index++] = data[ySize + mWidth * j + mWidth - 1 - i];
            //                }
            //            }

            //旋转并镜像
            for (int i = 0; i < mWidth; i++) {
                for (int j = mHeight - 1; j >= 0; j--) {
                    cameraBuffer_[index++] = data[mWidth * j + mWidth - 1 - i];
                }
            }
            //  u v
            for (int i = 0; i < mWidth; i += 2) {
                for (int j = uvHeight - 1; j >= 0; j--) {
                    // v
                    cameraBuffer_[index++] = data[ySize + mWidth * j + mWidth - 1 - i - 1];
                    // u
                    cameraBuffer_[index++] = data[ySize + mWidth * j + mWidth - 1 - i];
                }
            }
        }
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
    }

    public void setOnChangedSizeListener(OnChangedSizeListener listener) {
        mOnChangedSizeListener = listener;
    }

    public interface OnChangedSizeListener {
        void onChanged(int width, int height);
    }
}

