package com.zjz.pzp.proxy;

import com.zjz.pzp.annotation.WsFunctionUdpMapper;
import com.zjz.pzp.pojo.WsSendUdpBox;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author zjz
 * @date 2022/9/13
 */
public class WsSendUdpProxy {
    /**
     * 用来处理发送websocket传递至后台的数据
     *
     * @param targetObj
     * @param box
     * @return
     */
    public static void callMapper(Object targetObj, WsSendUdpBox box, ChannelHandlerContext ctx) throws InvocationTargetException, IllegalAccessException {
        Validate.notNull(targetObj);
        Validate.notNull(box);
        Validate.isTrue(box.getMethodType() > 0, "WsSendUdpBox methodType equals 0");
        Object returnObj = null;
            for (int i = 0; i < targetObj.getClass().getMethods().length; i++) {
                Method method = targetObj.getClass().getMethods()[i];
                WsFunctionUdpMapper mapper = method.getAnnotation(WsFunctionUdpMapper.class);
                if (mapper == null) {
                    continue;
                }
                //注解上对应的方法关键词
                int mapperMethodType = mapper.methodType();
                if (mapperMethodType == box.getMethodType()) {
                    // 匹配到对应的处理器
                    // 获取到所需要参数的对象信息
                    Class<?>[] parameterTypes = method.getParameterTypes();

                    if (parameterTypes.length == 1 && WsSendUdpBox.class.isAssignableFrom(parameterTypes[0])) {
                        method.invoke(targetObj, box);
                    }
                    if (parameterTypes.length == 2 && WsSendUdpBox.class.isAssignableFrom(parameterTypes[0]) && ChannelHandlerContext.class.isAssignableFrom(parameterTypes[1])) {
                        method.invoke(targetObj, box, ctx);
                    }
                }
            }

    }
}
