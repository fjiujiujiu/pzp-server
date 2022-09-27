package com.zjz.pzp.central;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zjz
 * @date 2022/9/11
 * ws 的注册使用中心
 */
public class ChannelWSCentral {
    /**
     * 用户名-channel短名称
     */
    private static Map<String, String> USERNAME_SHORTNAME = new ConcurrentHashMap<>();
    /**
     *channel短名称-对应{{@link ChannelHandlerContext}}
     */
    public static Map<String, ChannelHandlerContext> SHORTNAME_CHANNEL = new ConcurrentHashMap<>();

    /**
     * 设置用户名称和{{@link ChannelHandlerContext}}的对应
     * @param userName
     * @param ctx
     */
    public static void registryUserNameChannel(String userName, ChannelHandlerContext ctx) {
        String shortName = ctx.channel().id().asShortText();
        Validate.notBlank(shortName);
        USERNAME_SHORTNAME.put(userName, shortName);
        SHORTNAME_CHANNEL.put(shortName, ctx);
    }

    /**
     * 依据用户名查询{{@link ChannelHandlerContext}}
     * @param userName
     * @return
     */
    public static ChannelHandlerContext getChannelHandlerByUserName(String userName) {
        String shortName = USERNAME_SHORTNAME.get(userName);
        if (StringUtils.isBlank(shortName)) {
            return null;
        }
        return SHORTNAME_CHANNEL.get(shortName);
    }

    public static void deleteUserNameChannel(String userName) {
        String shortName = USERNAME_SHORTNAME.get(userName);
        if (StringUtils.isBlank(shortName)) {
            throw new IllegalArgumentException("用户下线");
        }
        USERNAME_SHORTNAME.remove(userName);
        SHORTNAME_CHANNEL.remove(shortName);
    }
    public static ChannelHandlerContext registryShortName(ChannelHandlerContext ctx) {
        SHORTNAME_CHANNEL.put(ctx.channel().id().asShortText(), ctx);
        return ctx;
    }

    /**
     * 传入短名称移除ChannelHandlerContext
     * @param name
     */
    public static void deleteCtx(String name) {
        SHORTNAME_CHANNEL.remove(name);
    }

    /**
     * 发送数据至websocket中
     * @param name
     * @param message
     */
    public static void sendBodyMessage(String name, String message) {
        ChannelHandlerContext ctx = SHORTNAME_CHANNEL.get(name);
        Validate.notNull(ctx);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(message));
    }

}
