package com.zjz.pzpS.handle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.zjz.base.FixedLengthAnalysis;
import org.zjz.base.HexUtils;
import org.zjz.base.IPAddrUtils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

/**
 * @author zjz
 * @date 2022/9/11
 */
@Slf4j
public class UdpCenterHandle extends SimpleChannelInboundHandler<DatagramPacket> {
    private HashMap<String, InetSocketAddress> userNameAndIp;


    public UdpCenterHandle(HashMap<String, InetSocketAddress> userNameAndIp) {
        this.userNameAndIp = userNameAndIp;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.channel().eventLoop().scheduleAtFixedRate(()->
                 {
                     log.info("head send...");
                     if (userNameAndIp.size() > 0) {
                         userNameAndIp.forEach((name, ip) -> {
                             byte[] nameIpAddrByte = name.getBytes(CharsetUtil.UTF_8);
                             byte mathCheckByte = FixedLengthAnalysis.getCheckByteT(nameIpAddrByte);
                             ByteBuf byteBuf = Unpooled.wrappedBuffer(nameIpAddrByte, new byte[]{mathCheckByte, 0x05});
                             ctx.channel().writeAndFlush(new DatagramPacket(byteBuf, ip));
                         });
                     }
                     userNameAndIp.clear();
                }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        ByteBuf buf = msg.content();
        String byteStr = ByteBufUtil.hexDump(buf).toUpperCase();
        log.info("byteStr:{}", byteStr);
        if (buf.readableBytes() <= 1) {
            log.info("校验长度小于1");
            return;
        }
        // 获取到倒数第二个校验码
        byte msgCheckByte = buf.getByte(buf.readableBytes() - 2);
        byte mathCheckByte = FixedLengthAnalysis.getCheckByteT(buf, 0, buf.readableBytes()  - 2);
        log.info("msgCheckByte:{},mathCheckByte:{}", msgCheckByte, mathCheckByte);
        if (mathCheckByte == msgCheckByte) {
            if (0x02 == buf.getByte(buf.readableBytes() - 1)) {
                String name = buf.toString(0, buf.readableBytes() - 2, CharsetUtil.UTF_8);
                userNameAndIp.put(name, msg.sender());
                log.info("username:{},ip:{},port:{} register success!!", name, msg.sender().getHostString(), msg.sender().getPort());
                String nameIpAddr = IPAddrUtils.loginMessageBuilder(userNameAndIp);
                byte[] nameIpAddrByte = nameIpAddr.getBytes(CharsetUtil.UTF_8);
                mathCheckByte = FixedLengthAnalysis.getCheckByteT(nameIpAddrByte);
                ByteBuf byteBuf = Unpooled.wrappedBuffer(nameIpAddrByte, new byte[]{mathCheckByte, 0x03});
                userNameAndIp.forEach((k,v)->{
                    byteBuf.retain();
                    ctx.channel().writeAndFlush(new DatagramPacket(byteBuf, v));
                });
                byteBuf.release();
            }
            // 为心跳
            if (0x05 == buf.getByte(buf.readableBytes() - 1)) {
                String name = buf.toString(0, buf.readableBytes() - 2, CharsetUtil.UTF_8);
                userNameAndIp.put(name, msg.sender());
                log.info("username:{},ip:{},port:{} register success!!", name, msg.sender().getHostString(), msg.sender().getPort());
                String nameIpAddr = IPAddrUtils.loginMessageBuilder(userNameAndIp);
                byte[] nameIpAddrByte = nameIpAddr.getBytes(CharsetUtil.UTF_8);
                mathCheckByte = FixedLengthAnalysis.getCheckByteT(nameIpAddrByte);
                ByteBuf byteBuf = Unpooled.wrappedBuffer(nameIpAddrByte, new byte[]{mathCheckByte, 0x03});
                userNameAndIp.forEach((k,v)->{
                    byteBuf.retain();
                    ctx.channel().writeAndFlush(new DatagramPacket(byteBuf, v));
                });
                byteBuf.release();
            }

        }
    }
}
