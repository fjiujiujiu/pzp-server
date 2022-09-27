package com.zjz.pzp.pojo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import lombok.Data;
import org.zjz.base.FixedLengthAnalysis;
import org.zjz.base.IPAddrUtils;

import java.net.InetSocketAddress;

/**
 * @author zjz
 * @date 2022/9/11
 */
@Data
public class UdpSendBox {
    private byte headMethod;
    private InetSocketAddress targetIp;
    private ByteBuf byteBuf;

    /**
     * @param headMethod {{@link MethodTypeEnum}}
     * @param targetIp   127.0.0.1:878
     */
    public UdpSendBox(MethodTypeEnum headMethod, String targetIp, ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
        this.headMethod = headMethod.getCode();
        InetSocketAddress inetAddress = null;
        try {
            inetAddress = IPAddrUtils.getInetSocketAddressByString(targetIp);

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("服务器地址解析错误");
        }
        this.targetIp = inetAddress;
    }

    /**
     * @param headMethod {{@link MethodTypeEnum}}
     * @param targetIp   127.0.0.1:878
     */
    public UdpSendBox(MethodTypeEnum headMethod, InetSocketAddress address, ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
        this.headMethod = headMethod.getCode();
        this.targetIp = address;
    }

    public ByteBuf builderSendBox() {
        byte checkByte = FixedLengthAnalysis.getCheckByteT(byteBuf, 0, byteBuf.readableBytes());
        ByteBuf body = Unpooled.wrappedBuffer(new byte[]{checkByte, headMethod});
        ByteBuf allByteBuf = Unpooled.wrappedBuffer(byteBuf, body);
        return allByteBuf;
    }

    public DatagramPacket builderPacket() {
        ByteBuf body = builderSendBox();
        return new DatagramPacket(body, targetIp);
    }
}
