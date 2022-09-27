package com.zjz.pzp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zjz.pzp.central.ChannelWSCentral;
import com.zjz.pzp.pojo.*;
import com.zjz.pzp.proxy.CallServerProxy;
import com.zjz.pzp.proxy.WsSendUdpProxy;
import com.zjz.pzp.webServe.MyUDPServe;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static cn.hutool.http.ContentType.JSON;

/**
 * @author zjz
 * @date 2022/9/8
 */
@Slf4j
public class MySocketNettyHandler extends SimpleChannelInboundHandler {
    private WebSocketServerHandshaker handshaker;
    private String wsUrl;
    private Object controller;
    private Object wsUdpController;
    public MySocketNettyHandler(String wsUrl, Object controller, Object wsUdpController) {
        this.wsUdpController = wsUdpController;
        this.controller = controller;
        this.wsUrl = wsUrl;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info(ctx.channel().localAddress().toString() + " channel active！");
    }
    /**
     * channelInactive
     *
     * channel 通道 Inactive 不活跃的
     *
     * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
     *
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info(ctx.channel().localAddress().toString() + " close！");
        // 关闭流
    }
    /**
     * 功能：读取服务器发送过来的信息-进行一个协议的判断而已
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {// 如果是HTTP请求，进行HTTP操作
            System.out.println("into hettpHandle");
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {// 如果是Websocket请求，则进行websocket操作
            System.out.println("into websockethandel");
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

    /**
     * 处理HTTP的代码
     * @param ctx
     * @param req
     * @throws UnsupportedEncodingException
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws UnsupportedEncodingException {
       log.info("handleHttpRequest method==========" + req.method());
        log.info("handleHttpRequest uri==========" + req.uri());
        // 如果HTTP解码失败，返回HHTP异常
        Map<String, String> parmMap = new HashMap<>();
        if (req instanceof HttpRequest) {
            HttpMethod method = req.method();
            // 如果是websocket请求就握手升级
            if (wsUrl.equalsIgnoreCase(req.uri())) {
                /**
                 * webSocketURL:注意，这条地址别被误导了，其实这里填写什么都无所谓，WS协议消息的接收不受这里控制?待验证
                 */
                WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                        "http://localhost:8880/index.html#/", null, false);
                handshaker = wsFactory.newHandshaker(req);
                if (handshaker == null) {
                    WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                } else {
                    handshaker.handshake(ctx.channel(), req);
                }
            }
            if (!wsUrl.equalsIgnoreCase(req.uri())) {
                // 是POST请求-
                System.out.println(req.content().toString(CharsetUtil.UTF_8));
                // 构建http响应
                CallServerBox box = CallServerProxy.callMapper(controller, req);
                // 设置头信息
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,Unpooled.copiedBuffer(box.getReturnObject().toString(), CharsetUtil.UTF_8));
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, box.getHead());
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    /**
     * 握手请求不成功时返回的应答
     * @param ctx
     * @param req
     * @param res
     */
    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
    }

    /**
     * 处理Websocket的代码
     * @param ctx
     * @param frame
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否是关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            ChannelWSCentral.deleteCtx(ctx.channel().id().asShortText());
            return;
        }
        // 判断是否是Ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 文本消息，不支持二进制消息
        if (frame instanceof TextWebSocketFrame) {
            // 返回应答消息
            String request = ((TextWebSocketFrame) frame).text();
            try {
                WsSendUdpBox wsSendUdpBox = JSONObject.parseObject(request, WsSendUdpBox.class);
                Validate.notNull(wsSendUdpBox);
                WsSendUdpProxy.callMapper(wsUdpController, wsSendUdpBox, ctx);
            } catch (InvocationTargetException e) {
                log.error("数据解析错误0", e);
                Throwable targetException = e.getTargetException();
                ctx.writeAndFlush(new TextWebSocketFrame(SendWsBox.builderErrorObjToJson(targetException.getMessage())));
            } catch (IllegalArgumentException e) {
                log.error("数据解析错误1", e);
                ctx.writeAndFlush(new TextWebSocketFrame(SendWsBox.builderErrorObjToJson(e.getMessage())));
            } catch (Exception e) {
                log.error("数据解析错误2", e);
                ctx.writeAndFlush(new TextWebSocketFrame(SendWsBox.builderErrorObjToJson(e.getMessage())));
            }
        }
    }
    /**
     * 功能：服务端发生异常的操作
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        log.error("error:", cause);
        System.out.println("异常信息：\r\n" + cause.getMessage());
    }

}
