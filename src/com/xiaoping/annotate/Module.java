package com.xiaoping.annotate;

import java.lang.annotation.*;

/**
 * 定义模块注解
 * 作用于类上
 * 运行时解析
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Module {

    String value() default "/";
}
