package com.testndk.jnistudy.aspect;

import android.content.Context;
import android.widget.Toast;

import com.testndk.jnistudy.utils.LogUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * aspect
 * <p>
 * execution
 */
@Aspect
public class TextAnnotationAspect {

//    @Pointcut(value = "* *(..)")
//    public void pointBeanMethod() {
//
//    }

    @Pointcut("execution(@com.testndk.jnistudy.annotation.TestAnnotation * *(..))")
    public void pointActionMethod() {

    }

    @Around("pointActionMethod()")
    public void proceedJoinPoint(final ProceedingJoinPoint point) {
        LogUtils.eLog("ok了");
        Toast.makeText((Context) point.getThis(), "kkk", Toast.LENGTH_SHORT).show();
        try {
            point.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

//    @Before(value = "pointActionMethod() && pointBeanMethod()")
//    public void beforePointCutInner() {
//
//    }
}
