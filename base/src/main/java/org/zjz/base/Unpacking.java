package org.zjz.base;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.apache.commons.lang3.Validate;
import org.zjz.base.fileAnalysis.MyFileManager;
import org.zjz.base.fileAnalysis.MyFileUtils;
import org.zjz.base.fileAnalysis.SendFileBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zjz
 * @date 2022/8/16
 * 数据域处理
 */
public class Unpacking {
    private UdpWrapEnum udpWrapEnum;
    private byte[] dataField;
    private static byte[] ZERO_BYTE = new byte[]{0x00, 0x00};

    /**
     * 消息发送
     * @param message
     * @return
     */
    public static Unpacking unpackingSendMessage(String message) {
        Validate.notBlank(message);
        return new Unpacking(UdpWrapEnum.SEND_MESSAGE, FixedLengthAnalysis.messageToBytesUTF8(message));
    }

    /**
     * 消息发送
     * @return
     */
    public static Unpacking unpackingRegsFileName(String fileName) {
        Validate.notBlank(fileName);
        return new Unpacking(UdpWrapEnum.RECEPTION_FILE, FixedLengthAnalysis.messageToBytesUTF8(fileName));
    }

    /**
     * 文件发送第一包
     *
     * @return
     */
    public static Unpacking unpackingSendFile0(String path) throws IOException {
        Validate.notBlank(path);
        File sendFile = MyFileUtils.getFileByPath(path);
            SendFileBox box = MyFileManager.registryFile(sendFile);
        byte[] fileNameB = FixedLengthAnalysis.messageToBytesUTF8(sendFile.getName());
        ByteBuf byteBuf = Unpooled.buffer(8 + fileNameB.length);
        byteBuf.writeBytes(ByteBuffer.allocate(8).putLong(box.getTotal()).array());
        byteBuf.writeBytes(fileNameB);
        byte[] req = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(req);
        return new Unpacking(UdpWrapEnum.SEND_FILE, req);

    }
    /**
     * 进行注册
     * @return
     */
    public static Unpacking unpackingLogin(String username) {
        Validate.notBlank(username);
        return new Unpacking(UdpWrapEnum.SEND_IP_PORT, FixedLengthAnalysis.messageToBytesUTF8(username));
    }
    public Unpacking(UdpWrapEnum udpWrapEnum, byte[] dataField) {
        this.udpWrapEnum = udpWrapEnum;
        this.dataField = dataField;
    }
    /**
     * 构建{{@link ByteBuf}}
     * @return
     */
    public ByteBuf getBuilderByteBuf() {
        ByteBuf buf = new UnpooledByteBufAllocator(true).buffer();
        buf.writeBytes(new byte[]{0x68, udpWrapEnum.getFunctionCode()});
        if (dataField.length == 0) {
            dataField = ZERO_BYTE;
        }
        buf.writeBytes(dataField);
        buf.writeByte(FixedLengthAnalysis.getCheckByteT(dataField));
        buf.writeByte(0x16);
        return buf;
    }
}
