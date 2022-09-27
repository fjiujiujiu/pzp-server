package com.zjz.pzp.webServe;

import com.zjz.pzp.controller.MyWsUdpController;
import com.zjz.pzp.handler.MySocketNettyHandler;
import com.zjz.pzp.controller.MyServerController;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zjz
 * @date 2022/9/2
 */
@Slf4j
public class MyWebServe {
    /**
     * 用来启动一个简单的webSocket服务
     *
     * @param httpPort     http服务启动的端口号
     * @param webSocketUrl webSocket地址
     * @throws InterruptedException
     */
    public void run(int httpPort, String webSocketUrl) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(4);
        EventLoopGroup wortGroup = new NioEventLoopGroup(4);
        log.info("PZP_SERVER_HTTP loading....");
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, wortGroup) //设置两个线程组
                    .channel(NioServerSocketChannel.class)// 设置服务其所使用的通道最后是什么类型的，进行一个包裹
                    .option(ChannelOption.SO_BACKLOG, 128)//option 主要负责设置 Boss 线程组 SO_BACKLOG 完成三次握手的请求队列最大长度 设置长度为128个
                    .childOption(ChannelOption.SO_KEEPALIVE, true)// 设置 work线程组 true 代表启用了 TCP SO_KEEPALIVE 属性，TCP 会主动探测连接状态，即连接保活
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 基于http协议，使用http的编码和解密器
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            pipeline.addLast(new ChunkedWriteHandler());
                            /**
                             * websocket是以帧的形式进行传递
                             * WebSocketFrame 旗下有6个子类
                             * 浏览器请求时 ws://localhost:8888/webSocketUrl
                             * WebSocketServerProtocolHandler 核心功能把http协议升级为了 ws 长连接的协议
                             */
                            pipeline.addLast(new MySocketNettyHandler(webSocketUrl, new MyServerController(httpPort), new MyWsUdpController()));

                        }
                    });
            log.info("PZP_SERVER_HTTP finish httpPort:{},ws url:{}", httpPort, webSocketUrl);
            /**
             * 绑定一个端口并进行同步
             */
            ChannelFuture channelFuture = serverBootstrap.bind(httpPort).sync();
            /**
             * 对关闭通道进行监听
             */
            channelFuture.addListener((future) -> {
                // 判断是否成功
                if (future.isSuccess()) {
                    log.info("httpPort:{} is success!!", httpPort);
                    log.info("please open the http://127.0.0.1:{}/index", httpPort);

                } else {
                    log.info("httpPort:{} is fail!!", httpPort);
                }
            });
            channelFuture.channel().closeFuture().sync();
        } finally {
            wortGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}


