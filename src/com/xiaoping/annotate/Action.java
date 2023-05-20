package com.xiaoping.annotate;

import java.lang.annotation.*;

/**
 * 定义请求方法
 * 作用于模块@Module类的公开成员方法
 * 运行时解析
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Action {

    String value() default "";
}
