package com.zjz.pzp.handler;

import com.zjz.pzp.controller.MyUdpServerController;
import com.zjz.pzp.pojo.SendWsBox;
import com.zjz.pzp.proxy.UdpServerProxy;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.zjz.base.FixedLengthAnalysis;
import org.zjz.base.IPAddrUtils;

import javax.security.auth.callback.Callback;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zjz
 * @date 2022/9/11
 */
@Slf4j
public class MyUdpClientHandle extends SimpleChannelInboundHandler<DatagramPacket> {
    private ChannelHandlerContext myCtx;
    private MyUdpServerController controller = new MyUdpServerController();
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        myCtx = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.content();
        String byteStr = ByteBufUtil.hexDump(buf).toUpperCase();
        log.info("byteStr:{}", byteStr);
        if (buf.readableBytes() <= 2) {
            log.info("校验长度小于2");
            return;
        }
        // 获取到倒数第二个校验码
        byte msgCheckByte = buf.getByte(buf.readableBytes() - 2);
        // 方法编码
        byte mathCheckByte = FixedLengthAnalysis.getCheckByteT(buf, 0, buf.readableBytes()  - 2);
        log.info("msgCheckByte:{},mathCheckByte:{}", msgCheckByte, mathCheckByte);
        if (mathCheckByte == msgCheckByte) {
            try {
                UdpServerProxy.callMapper(controller, buf, msg);
            } catch (InvocationTargetException e) {
                Throwable throwable = e.getTargetException();
                ctx.writeAndFlush(new TextWebSocketFrame(SendWsBox.builderErrorObjToJson(throwable.getMessage())));
                log.error("udp接收数据错误,反射", throwable);
            } catch (Exception e) {
                log.error("udp接收数据错误", e);
                e.printStackTrace();
                ctx.writeAndFlush(new TextWebSocketFrame(SendWsBox.builderErrorObjToJson(e.getMessage())));

            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        System.out.println("异常信息：\r\n" + cause.getMessage());
    }

}
