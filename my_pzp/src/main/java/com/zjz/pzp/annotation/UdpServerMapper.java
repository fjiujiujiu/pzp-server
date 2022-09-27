package com.zjz.pzp.annotation;

import com.zjz.pzp.pojo.MethodTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zjz
 * @date 2022/9/16
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UdpServerMapper {
    MethodTypeEnum methodFunction();
}
