package com.zjz.pzp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zjz
 * @date 2022/9/9
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyRequestMapper {
    /**
     * 映射的url地址
     *
     * @return
     */
    String path();

    String headers() default "application/json;";
}
