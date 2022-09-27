package com.zjz.pzpS.server;

import com.zjz.pzpS.handle.UdpCenterHandle;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/**
 * @author zjz
 * @date 2022/9/11
 */
@Slf4j
public class UdpCenterServer {
    public void run(int port) {
        log.info("PZP_SERVER_UDP loading....");
        EventLoopGroup workEvent = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workEvent)
                    .channel(NioDatagramChannel.class)
                    .handler(new UdpCenterHandle(new HashMap<>()))
                    .option(ChannelOption.SO_BROADCAST, true);
            log.info("PZP_SERVER_UDP finish udpPort:{}", port);
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.addListener((future) -> {
                // 判断操作是否完成，但不能说明成功
                future.isDone();
                // 判断是否成功
                if (future.isSuccess()) {
                    log.info("udpPort:{} is success!!", port);
                } else {
                    log.info("udpPort:{} is fail!!", port);
                }
            });
            ChannelFuture closeFutrue = channelFuture.channel().closeFuture().sync();
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
