//package com.testndk.jnistudy.ui.activity;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.os.Bundle;
//import android.view.WindowManager;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.testndk.jnistudy.R;
//import com.testndk.jnistudy.aspect.core.IPermission;
//import com.testndk.jnistudy.utils.PermissionUtils;
//
//
//// TODO 专门权限处理的Activity
//public class MyPermissionActivity extends Activity {
//
//    // 定义权限处理的标识 -- 接收用户传递进来的
//    private final static String PARAM_PREMISSION = "param_permission";
//    private final static String PARAM_REQUEST_CODE = "param_request_code";
//    public final static int PARAM_REQUEST_CODE_DEFAULT = -1;
//
//    private String[] permissions;
//    private int requestCode;
//    // 这个Activity  已经授权，取消授权，被拒绝授权
//    private static IPermission permissionListener;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        getWindow().getDecorView().setBackground(null);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        setContentView(R.layout.activity_my_permission);
//        permissions = getIntent().getStringArrayExtra(PARAM_PREMISSION);
//        requestCode = getIntent().getIntExtra(PARAM_REQUEST_CODE, PARAM_REQUEST_CODE_DEFAULT);
//        if (permissions == null && requestCode < 0 && permissionListener == null) {
//            this.finish();
//            return;
//        }
//        // 能走到这里，就开始去检查，是否已经授权了
//        boolean permissionRequest = PermissionUtils.hasPermissionRequest(this, permissions);
//        // 已经授权了，无需在申请
//        if (permissionRequest) {
//            // 通过监听接口，告诉外界，已经授权了
//            permissionListener.ganted();
//            this.finish();
//            return;
//        }
//        // 能走到这里，就证明，还需要去申请权限
//        ActivityCompat.requestPermissions(this, permissions, requestCode);
//    }
//
//
//    /**
//     * 申请权限之后的结果 方法
//     *
//     * @param requestCode
//     * @param permissions
//     * @param grantResults
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) { // grantResults.length = 3
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        //  返回的结果，需要去验证一下，是否完全成功了,
//        if (PermissionUtils.requestPermissionSuccess(grantResults)) {
//            // 通过监听接口，告诉外界，已经授权成功
//            permissionListener.ganted();
//            this.finish();
//            return;
//        }
//
//        // 如果用户点击了，拒绝，（不再提示打勾） 等操作，告诉外界
//        if (!PermissionUtils.shouldShowRequestPermissionRationale(this, permissions)) {
//            // 用户拒绝，不再提醒
//            // 通过接口监听，告诉外界，被拒绝，（不再提示打勾）
//            permissionListener.denied();
//            this.finish();
//            return;
//        }
//
//        // 如果执行到这里来了，就证明 权限被取消了
//        permissionListener.cancel();
//        this.finish();
//    }
//
//
//    // 专门处理 当前Activity结束的时候，不需要有动画效果
//
//    @Override
//    public void finish() {
//        super.finish();
//        overridePendingTransition(0, 0);
//    }
//
//    // TODO 把当前整个Activity暴露给外界使用
//    public static void requestPermissionAction(Context context, String[] permissions,
//                                               int requestCode, IPermission iPermission) {
//        permissionListener = iPermission;
//
//        Intent intent = new Intent(context, MyPermissionActivity.class);
//
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        Bundle bundle = new Bundle();
//        bundle.putInt(PARAM_REQUEST_CODE, requestCode);
//        bundle.putStringArray(PARAM_PREMISSION, permissions);
//
//        intent.putExtras(bundle);
//
//        context.startActivity(intent);
//    }
//}
