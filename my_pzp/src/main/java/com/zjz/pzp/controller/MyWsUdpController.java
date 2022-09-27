package com.zjz.pzp.controller;

import com.alibaba.fastjson.JSONObject;
import com.zjz.pzp.annotation.WsFunctionUdpMapper;
import com.zjz.pzp.central.ChannelWSCentral;
import com.zjz.pzp.central.FileSendMapCenter;
import com.zjz.pzp.central.FileTicketCenter;
import com.zjz.pzp.central.UserNameIpCenter;
import com.zjz.pzp.pojo.*;
import com.zjz.pzp.webServe.MyUDPServe;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

/**
 * @author zjz
 * @date 2022/9/13
 */
public class MyWsUdpController {
    /**
     * 进行注册
     */
    @WsFunctionUdpMapper(methodType = 5)
    public void register(WsSendUdpBox box, ChannelHandlerContext ctx) {
        String name = box.getSenderName();
        ByteBuf buf = Unpooled.wrappedBuffer(name.getBytes(CharsetUtil.UTF_8));
        UdpSendBox udpSendBox = new UdpSendBox(MethodTypeEnum.REGISTRY_USER, box.getServerUdpProxy(), buf);
        MyUDPServe.sendMessage(udpSendBox.builderPacket());
        // 进行注册
        ChannelWSCentral.registryUserNameChannel(box.getSenderName(), ctx);
        SendWsBox sendWsBox = SendWsBox.builderSuccessOb("连接成功", SendWsEnum.CONNECT_SUCCESS);
        sendWsBox.setResCode(9);
        ctx.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(sendWsBox)));
    }

    /**
     * 经行数据发送
     */
    @WsFunctionUdpMapper(methodType = 6)
    public void sendMessage(WsSendUdpBox box) {
        String recipientName = box.getRecipientName();
        String type = box.getBodyType();
        InetSocketAddress inetSocketAddress = UserNameIpCenter.getInetByUserName(recipientName);
        if (inetSocketAddress == null) {
            throw new IllegalArgumentException("未找到对应的用户");
        }
        if (MyBodyType.STRING.equals(type)) {
            // 为 字符串 类型
            String body = (String) box.getBody();
            // 设置谁发送的消息
            String whoToWho = String.format("(%s)[%s]", box.getSenderName(), box.getRecipientName()).toString();
            body = whoToWho + ":" + body;
            UdpSendBox udpSendBox = new UdpSendBox(MethodTypeEnum.SEND_MESSAGE, inetSocketAddress, Unpooled.copiedBuffer(body, CharsetUtil.UTF_8));
            MyUDPServe.sendMessage(udpSendBox.builderPacket());
        }
    }

    /**
     * 用来发送文件订单号信息到其余对象中
     */
    @WsFunctionUdpMapper(methodType = 7)
    public void sendFileTicker(WsSendUdpBox box) throws IOException {
        String recipient = box.getRecipientName();
        // 需要发送的文件名以及具体的路径
        String filePath = (String) box.getBody();
        InetSocketAddress inetSocketAddress = UserNameIpCenter.getInetByUserName(recipient);
        if (inetSocketAddress == null) {
            throw new IllegalArgumentException("未找到对应的用户");
        }
        ByteBuf byteBuf = FileTicketBean.builderFileTicketByFilePath(box,filePath);

        File file = new File(filePath);
        String fileName = box.getSenderName() + "|" + box.getRecipientName() + "|" + file.getName();
        FileTicketBean fileTicketBean = new FileTicketBean(box, file);
        // 把fileCode 和 filePath塞入
        FileTicketCenter.FILE_CODE_FILE_BOOK_CENTER.put(fileName.hashCode(), new FileTickerBook(fileTicketBean, filePath));
        UdpSendBox udpSendBox = new UdpSendBox(MethodTypeEnum.SEND_FILE_TICKET, inetSocketAddress, byteBuf);
        MyUDPServe.sendMessage(udpSendBox.builderPacket());
    }
    /**
     * 确定需要接收文件
     */
    @WsFunctionUdpMapper(methodType = 8)
    public void acceptFileYes(WsSendUdpBox box) {
        // 构建一个Map
        FileTicketBean bean = JSONObject.parseObject((String) box.getBody(), FileTicketBean.class);
        FileTicketCenter.FILE_TICKER_CENTER.put(bean.getFileHashCode(), bean);
        InetSocketAddress inetSocketAddress = UserNameIpCenter.getInetByUserName( bean.getSenderName());
        if (inetSocketAddress == null) {
            throw new IllegalArgumentException("未找到对应的用户");
        }

            Map<String ,String > indexMap = FileSendMapCenter.builderFileIndexCenter(bean, 600);
            if (indexMap.size() > 0) {
                Set<String> keyArray = indexMap.keySet();
                int j = 0;
                for (String key : keyArray) {
                    if (j < keyArray.size() && j < 4) {
                        String[] indexLengthArray = key.split("\\|");
                        UdpSendBox udpSendBox = new UdpSendBox(MethodTypeEnum.SEND_FILE_INDEX, inetSocketAddress, Unpooled.wrappedBuffer(
                                Unpooled.copyLong(Long.valueOf(indexLengthArray[0])),
                                Unpooled.copyLong(Long.valueOf(indexLengthArray[1])),
                                Unpooled.copyInt(bean.getFileHashCode())
                        ));
                        MyUDPServe.sendMessage(udpSendBox.builderPacket());
                    }
                    j++;
                }
            }
        }
}
