package com.testndk.jnistudy.ui.andfix;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解:
 * 什么是注解?
 * 注解可以理解为代码中的特殊标记,可以在编译,类加载,运行时被读取,并且进行相应的处理.
 * 注解标签分为五种
 * Retention 保留策略
 * 该注解标签的作用是,注明该标签是在编译,加载还是运行时.
 * 其有三个值
 * 1.RetentionPolicy.SOURCE 注解只在源码阶段保留，在编译器进行编译时它将被丢弃忽视。
 * 2.RetentionPolicy.CLASS 注解只被保留到编译进行的时候，它并不会被加载到 JVM 中。
 * 3.RetentionPolicy.RUNTIME 注解可以保留到程序运行的时候，它会被加载进入到 JVM 中，所以在程序运行时可以获取到它们。
 *
 * Target 适用的元素种类
 * 该注解作用是说明该注解作用的是方法还是类
 * 1.ElementType.ANNOTATION_TYPE 注解类型声明
 * 2.ElementType.CONSTRUCTOR 构造方法声明
 * 3.ElementType.FIELD 字段声明（包括枚举常量）
 * 4.ElementType.LOCAL_VARIABLE 局部变量声明
 * 5.ElementType.METHOD 方法声明
 * 6.ElementType.PACKAGE 包声明
 * 7.ElementType.PARAMETER 参数声明
 * 8.ElementType.TYPE 类、接口（包括注解类型）或枚举声明
 *
 * Documented 文档化
 * 它的作用是能够将注解中的元素包含到 Javadoc 中去。
 * Inherited 自动继承
 * 但是它并不是说注解本身可以继承，而是说如果一个超类被 @Inherited 注解过的注解进行注解的话，那么如果它的子类没有被任何注解应用的话，那么这个子类就继承了超类的注解。比方说定义一个test注解被@Inherited注解，类A被test注解，类B继承类A但是没有test注解，但是类b也会继承类A的注解
 * Repeatable 重复标注
 * Java 1.8新特性,当给一个注解注释的是，改注释能重复注解方法类等，并且该注解能设置不同的值
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodReplace {
    /**
     * 需要修复方法的类名
     * @return
     */
    String className();

    /**
     * 需要修复方法的方法名
     * @return
     */
    String methodName();
}
