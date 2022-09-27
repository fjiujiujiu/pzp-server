package com.zjz.pzp.webServe;

import com.zjz.pzp.handler.MyUdpClientHandle;
import com.zjz.pzp.pojo.UdpSendBox;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
/**
 * @author zjz
 * @date 2022/9/11
 */
@Slf4j
public class MyUDPServe {
    public static ChannelFuture channelFuture;
    public static void sendMessage(DatagramPacket packet) {
        channelFuture.channel().writeAndFlush(packet);
    }
    /**
     * 进行运行
     * @param port
     */
    public static void run(int port) {
        Bootstrap bootstrap = new Bootstrap();
        log.info("PZP_UDP loading....");
        EventLoopGroup workEvent = new NioEventLoopGroup(4);
        try {
            bootstrap.group(workEvent)
                    .channel(NioDatagramChannel.class)
                    .handler(new MyUdpClientHandle())
                    .option(ChannelOption.SO_BROADCAST, true);
            ChannelFuture channelFuture1 = bootstrap.bind("0.0.0.0",port).sync();
            channelFuture = channelFuture1;
            channelFuture1.addListener((future) -> {
                // 判断是否成功
                if (future.isSuccess()) {
                    log.info("udpPort:{} is success!!", port);
                } else {
                    log.info("udpPort:{} is fail!!", port);
                }
            });
            ChannelFuture closeFutrue = channelFuture1.channel().closeFuture().sync();
            closeFutrue.addListener((future) -> {
                log.info("udpPort:{} is close!!", port);
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workEvent.shutdownGracefully();
        }
    }

}
