//package com.testndk.jnistudy.ui.activity;
//
//import android.Manifest;
//import android.view.View;
//import android.widget.Toast;
//
//import com.testndk.jnistudy.R;
//import com.testndk.jnistudy.aspect.Permission;
//import com.testndk.jnistudy.aspect.PermissionCancel;
//import com.testndk.jnistudy.aspect.PermissionDenied;
//
//public class TestAspectActivity extends BaseActivity {
//
//
//    @Override
//    public int initLayout() {
//        return R.layout.activity_aspect;
//    }
//
//    @Override
//    public void initView() {
//        super.initView();
//    }
//
//    @Permission(value = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
//            , requestCode = 200)
//    public void onClickAspect(View view) {
//        testRequest();
//    }
//
//
//    @Permission(value = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
//            , requestCode = 200)
//    public void testRequest() {
//        Toast.makeText(this, "权限申请成功...", Toast.LENGTH_SHORT).show();
//    }
//
//    @PermissionCancel
//    public void testCancel() {
//        Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
//    }
//
//    @PermissionDenied
//    public void testDenied() {
//        Toast.makeText(this, "权限被拒绝（用户勾选了，不再提醒）", Toast.LENGTH_SHORT).show();
//    }
//
//}
