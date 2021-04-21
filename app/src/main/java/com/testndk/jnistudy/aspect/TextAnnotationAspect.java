//package com.testndk.jnistudy.aspect;
//
//import android.app.Activity;
//import android.content.Context;
//import android.widget.Toast;
//
//import androidx.fragment.app.Fragment;
//
//import com.testndk.jnistudy.utils.LogUtils;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//
///**
// * aspect
// * <p>
// * execution
// */
//@Aspect
//public class TextAnnotationAspect {
//
////    @Pointcut(value = "* *(..)")
////    public void pointBeanMethod() {
////
////    }
//
//    @Pointcut("execution(@com.testndk.jnistudy.aspect.annotation.TestAnnotation * *(..))")
//    public void pointActionMethod() {
//
//    }
//
//    @Around("pointActionMethod()")
//    public void proceedJoinPoint(final ProceedingJoinPoint point) throws Exception {
//        Object obj = point.getThis();
//        if (obj instanceof Context) {
//
//        } else if (obj instanceof Fragment) {
//
//        } else {
//            throw new Exception("当前注解方法没有在fragment中或者activity中");
//        }
////        aThis.checkPermission("")
////        Toast.makeText(context, "aspectj 拦截成功", Toast.LENGTH_SHORT).show();
//        try {
//            point.proceed();
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
//
////    @Before(value = "pointActionMethod() && pointBeanMethod()")
////    public void beforePointCutInner() {
////
////    }
//}
