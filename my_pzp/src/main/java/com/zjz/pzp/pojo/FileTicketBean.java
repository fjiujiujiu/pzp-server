package com.zjz.pzp.pojo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zjz
 * @date 2022/9/21
 * 用于发送文件起始包信息
 */
@Slf4j
@Data
public class FileTicketBean {
    /**
     * 数据发送人
     */
    private String senderName;
    /**
     * 数据接收人
     */
    private String recipientName;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件大小
     */
    private Long fileLength;
    /**
     * 文件hashcode
     */
    private Integer fileHashCode;

    public FileTicketBean() { }

    public FileTicketBean(WsSendUdpBox box,File file) {
        this.senderName = box.getSenderName();
        this.recipientName = box.getRecipientName();
        this.fileLength = file.length();
        this.fileHashCode = file.getName().hashCode();
        this.fileName = file.getName();
    }

    /**
     * 依旧udp接收的数据构建{{@link FileTicketBean}}
     * @param byteBuf
     */
    public FileTicketBean(ByteBuf byteBuf) {
        int stringLength = byteBuf.readableBytes() - 14;
        String senderRecipientFileN = byteBuf.toString(0, byteBuf.readableBytes() - 14, CharsetUtil.UTF_8);
        String[] senderRecipientFileNArray = senderRecipientFileN.split("\\|");
        this.senderName = senderRecipientFileNArray[0];
        this.recipientName = senderRecipientFileNArray[1];
        this.fileName = senderRecipientFileNArray[2];
        ByteBuffer fileLengthByte = byteBuf.nioBuffer(stringLength,8);
        ByteBuffer fileHashCodeByte= byteBuf.nioBuffer(stringLength+8,4);
        this.fileLength = fileLengthByte.getLong();
        this.fileHashCode = fileHashCodeByte.getInt();

    }

    /**
     * 依据文件路径构建一个需要进行发送的文件包
     * @param box 接收到的数据包
     * @param filePath
     * @return
     */
    public static ByteBuf builderFileTicketByFilePath(WsSendUdpBox box,String filePath) throws IOException {
        File file = new File(filePath);
        log.info("申请发送文件:{}", filePath);
        if (file.exists()&&file.isFile()) {
            // 文件名称
            String fileName = box.getSenderName() + "|" + box.getRecipientName() + "|" + file.getName();
            Long fileBox = file.length();
            int fileHashCode = fileName.hashCode();
            log.info("需要进行发送的文件内容，用户文件信息:{},文件大小:{},唯一标识:{}", fileName, fileBox, fileHashCode);
            ByteBuf byteBuf = Unpooled.copiedBuffer(
                    Unpooled.copiedBuffer(fileName, CharsetUtil.UTF_8),
                    Unpooled.copyLong(fileBox),
                    Unpooled.copyInt(fileHashCode));
            return byteBuf;
        }else {
            throw new IOException(filePath + "not found");
        }
    }
}
