package com.zjz.pzp.proxy;

import com.zjz.pzp.annotation.UdpServerMapper;
import com.zjz.pzp.annotation.WsFunctionUdpMapper;
import com.zjz.pzp.pojo.WsSendUdpBox;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author zjz
 * @date 2022/9/16
 */
public class UdpServerProxy {
    public static void callMapper(Object targetObj, ByteBuf messageByte, DatagramPacket packet) throws InvocationTargetException, IllegalAccessException {
        Validate.notNull(targetObj);
        Validate.notNull(messageByte);
        Validate.notNull(packet);
        byte messageByteFunction = messageByte.getByte(messageByte.readableBytes() - 1);
            for (int i = 0; i < targetObj.getClass().getMethods().length; i++) {
                Method method = targetObj.getClass().getMethods()[i];
                UdpServerMapper mapper = method.getAnnotation(UdpServerMapper.class);
                if (mapper == null) {
                    continue;
                }
                //注解上对应的方法关键词
                byte mapperMethodType = mapper.methodFunction().getCode();
                if (mapperMethodType == messageByteFunction) {
                    // 匹配到对应的处理器
                    // 获取到所需要参数的对象信息
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 2 && ByteBuf.class.isAssignableFrom(parameterTypes[0]) && DatagramPacket.class.isAssignableFrom(parameterTypes[1])) {
                        method.invoke(targetObj, messageByte, packet);
                    }
                }

        }

    }
}
