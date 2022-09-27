package com.zjz.pzp.controller;

import com.alibaba.fastjson.JSONObject;
import com.zjz.pzp.annotation.UdpServerMapper;
import com.zjz.pzp.central.ChannelWSCentral;
import com.zjz.pzp.central.FileSendMapCenter;
import com.zjz.pzp.central.FileTicketCenter;
import com.zjz.pzp.central.UserNameIpCenter;
import com.zjz.pzp.pojo.*;
import com.zjz.pzp.utils.FileByteSend;
import com.zjz.pzp.webServe.MyUDPServe;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zjz
 * @date 2022/9/16
 */
@Slf4j
public class MyUdpServerController {
    /**
     * 心跳数据返回
     * @param messageByte
     * @param packet
     */
    @UdpServerMapper(methodFunction = MethodTypeEnum.HEAD_UDP)
    public void headMessage(ByteBuf messageByte, DatagramPacket packet) {
        String name = (String) messageByte.getCharSequence(0, messageByte.readableBytes()-2, CharsetUtil.UTF_8);
        UdpSendBox udpSendBox = new UdpSendBox(MethodTypeEnum.HEAD_UDP, packet.sender(), Unpooled.copiedBuffer(name, CharsetUtil.UTF_8));
        MyUDPServe.sendMessage(udpSendBox.builderPacket());
    }

    /**
     * 由中介服务器返回的用户名:ip:port|
     * @param messageByte
     * @param packet
     */
    @UdpServerMapper(methodFunction = MethodTypeEnum.USER_IPS)
    public void registerUserAndIp(ByteBuf messageByte, DatagramPacket packet) {
        String userIp = (String) messageByte.getCharSequence(0, messageByte.readableBytes()-2, CharsetUtil.UTF_8);
        log.info("接收到的用户名和ip地址为：{}", userIp);
        UserNameIpCenter.register(userIp);
        sendRegisterUser();
    }

    /**
     * 接收发送的数据，返回给websocket通道中
     * @param messageByte
     * @param packet
     */
    @UdpServerMapper(methodFunction =MethodTypeEnum.SEND_MESSAGE)
    public void acceptMessage(ByteBuf messageByte, DatagramPacket packet) {
        String message = (String) messageByte.getCharSequence(0, messageByte.readableBytes() - 2, CharsetUtil.UTF_8);
        SendMessageBox box = new SendMessageBox(message);
        ChannelHandlerContext channelHandlerContext = ChannelWSCentral.getChannelHandlerByUserName(box.getRecipientName());
        if (Objects.isNull(channelHandlerContext)) {
            sendErrorMsgToRecipient(box.getSenderName(), "用户下线", packet);
        }else {
            SendWsBox sendWsBox = SendWsBox.builderSuccessOb(box, SendWsEnum.RECEIVE_MESSAGE);
            channelHandlerContext.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(sendWsBox)));
        }
    }

    /**
     * 接收发送的文件单号数据
     * @param messageByte
     * @param packet
     */
    @UdpServerMapper(methodFunction = MethodTypeEnum.SEND_FILE_TICKET)
    public void acceptFileTicket(ByteBuf messageByte, DatagramPacket packet) {
        FileTicketBean ticketBean = new FileTicketBean(messageByte);
        ChannelHandlerContext channelHandlerContext = ChannelWSCentral.getChannelHandlerByUserName(ticketBean.getRecipientName());
        if (Objects.isNull(channelHandlerContext)) {
            sendErrorMsgToRecipient(ticketBean.getSenderName(), "用户下线", packet);
        }else {
            // 通知用户xx，有一个文件来是否需要接收
            SendWsBox sendWsBox = SendWsBox.builderSuccessOb(ticketBean, SendWsEnum.SEND_FILE_TICKER);
            channelHandlerContext.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(sendWsBox)));
        }
    }

    /**
     * 通知有一个错误的数据
     * @param messageByte
     */
    @UdpServerMapper(methodFunction = MethodTypeEnum.SEND_ERROR_MESSAGE)
    public void acceptErrorMsg(ByteBuf messageByte, DatagramPacket packet) {
        String s = messageByte.toString(0, messageByte.readableBytes() - 2, CharsetUtil.UTF_8);
        String[] userMessageArray = s.split("\\|");
        ChannelHandlerContext channelHandlerContext = ChannelWSCentral.getChannelHandlerByUserName(userMessageArray[0]);
        if (Objects.isNull(channelHandlerContext)) {
            throw new IllegalArgumentException("找寻用户错误");
        }else {
            SendWsBox sendWsBox = SendWsBox.builderErrorObj(userMessageArray[1]);
            channelHandlerContext.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(sendWsBox)));
        }
    }

    /**
     * 通知有一个消息的数据
     * @param messageByte
     */
    @UdpServerMapper(methodFunction = MethodTypeEnum.SEND_INFO_MESSAGE)
    public void acceptInfoMsg(ByteBuf messageByte, DatagramPacket packet) {
        String s = messageByte.toString(0, messageByte.readableBytes() - 2, CharsetUtil.UTF_8);
        String[] userMessageArray = s.split("\\|");
        ChannelHandlerContext channelHandlerContext = ChannelWSCentral.getChannelHandlerByUserName(userMessageArray[0]);
        if (Objects.isNull(channelHandlerContext)) {
            throw new IllegalArgumentException("找寻用户错误");
        }else {
            SendWsBox sendWsBox = SendWsBox.builderInfoObj(userMessageArray[1]);
            channelHandlerContext.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(sendWsBox)));
        }
    }

    /**
     * 接收文件索引信息
     */
    @UdpServerMapper(methodFunction = MethodTypeEnum.SEND_FILE_INDEX)
    public void acceptIndexFile(ByteBuf messageByte,DatagramPacket packet) throws IOException {
        Integer fileCode = messageByte.getInt(16);
        FileTickerBook fileTickerBook = FileTicketCenter.FILE_CODE_FILE_BOOK_CENTER.get(fileCode);
        if (Objects.isNull(fileTickerBook)) {
            sendErrorMsgToRecipient(fileTickerBook.getRecipient(), "文法发送错误", packet);
            throw new IllegalArgumentException("未找到对应文件单号");
        }
        InetSocketAddress inetSocketAddress = UserNameIpCenter.getInetByUserName(fileTickerBook.getRecipient());
        if (inetSocketAddress == null) {
            throw new IllegalArgumentException("未找到对应的用户");
        }
        // 进行文件包的发送
        FileByteSend.sendFileByte(messageByte, fileTickerBook);
    }

    @UdpServerMapper(methodFunction = MethodTypeEnum.SEND_FILE_BOX)
    public void acceptFileBox(ByteBuf messageByte,DatagramPacket packet) throws IOException, InterruptedException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Integer fileCode = messageByte.getInt(16);
        FileTicketBean fileTickerBook = FileTicketCenter.FILE_TICKER_CENTER.get(fileCode);
        InetSocketAddress inetSocketAddress = UserNameIpCenter.getInetByUserName(fileTickerBook.getSenderName());
        if (inetSocketAddress == null) {
            throw new IllegalArgumentException("未找到对应的用户");
        }
        if (Objects.isNull(fileTickerBook)) {
            sendErrorMsgToRecipient(fileTickerBook.getRecipientName(), "文法发送错误", packet);
            throw new IllegalArgumentException("未找到对应文件单号");
        }
        // 进行文件包的发送
        String key = FileByteSend.writeFile(fileTickerBook.getFileName(), "./", messageByte);
        FileSendMapCenter.removeKey(fileCode, key);
        ByteBuf byteBuf=  FileSendMapCenter.nextSendByte(fileCode);
        if (Objects.nonNull(byteBuf)) {
            UdpSendBox udpSendBox = new UdpSendBox(MethodTypeEnum.SEND_FILE_INDEX, inetSocketAddress, byteBuf);
            MyUDPServe.sendMessage(udpSendBox.builderPacket());
        }else{
            sendInfoMsgToRecipient(fileTickerBook.getRecipientName(), "文件接收完毕", packet.sender());
            FileByteSend.closeMapper(fileTickerBook.getFileName(), "./");
        }
    }

    /**
     * 发送用户表
     */
    public void sendRegisterUser() {
        List<String> userName = new ArrayList<>();
        UserNameIpCenter.USERNAME_IP.forEach((k,v)->{
            userName.add(k);
        });
        SendWsBox wsBox = SendWsBox.builderSuccessOb(userName, SendWsEnum.SEND_USER_LIST);

        ChannelWSCentral.SHORTNAME_CHANNEL.forEach((k, v) -> {

            v.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(wsBox)));
        });
    }

    /**
     * 即让谁 接收什么样子的错误消息
     *
     * @param recipient
     * @param errorMsg
     * @param packet
     * @return
     */
    public void sendErrorMsgToRecipient(String recipient, String errorMsg, DatagramPacket packet) {
        String body = recipient + "|" + errorMsg;
        UdpSendBox udpSendBox = new UdpSendBox(MethodTypeEnum.SEND_ERROR_MESSAGE, packet.sender(), Unpooled.copiedBuffer(body, CharsetUtil.UTF_8));
        MyUDPServe.sendMessage(udpSendBox.builderPacket());
    }

    /**
     * 即让谁 接收什么样子的数据
     *
     * @param userName
     * @param infoMsg
     * @param address 地址
     * @return
     */
    public void sendInfoMsgToRecipient(String userName, String infoMsg,InetSocketAddress address) throws InterruptedException {
        String body = userName + "|" + infoMsg;
        UdpSendBox udpSendBox = new UdpSendBox(MethodTypeEnum.SEND_INFO_MESSAGE, address, Unpooled.copiedBuffer(body, CharsetUtil.UTF_8));
        MyUDPServe.sendMessage(udpSendBox.builderPacket());
    }


}
