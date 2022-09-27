package com.zjz.pzp.utils;

import com.zjz.pzp.central.UserNameIpCenter;
import com.zjz.pzp.pojo.FileTickerBook;
import com.zjz.pzp.pojo.MethodTypeEnum;
import com.zjz.pzp.pojo.UdpSendBox;
import com.zjz.pzp.webServe.MyUDPServe;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import sun.nio.ch.FileChannelImpl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zjz
 * @date 2022/9/22
 */
public class FileByteSend {
    /**
     * 构建,需要发送byte 同时进行发送
     * @param indexByte 由  8位 index long | 8位 length long | 4位 fileCode int| 构建的byte
     * @param fileTickerBook
     */
    public static void sendFileByte(ByteBuf indexByte, FileTickerBook fileTickerBook) throws IOException {
        Long index = indexByte.getLong(0);
        Long length = indexByte.getLong(8);
        Integer fileCode = indexByte.getInt(16);
        ByteBuf buf = getFileByte(index, length, fileCode, fileTickerBook.getFilePath());
        InetSocketAddress inetSocketAddress = UserNameIpCenter.getInetByUserName(fileTickerBook.getRecipient());
        if (inetSocketAddress == null) {
            throw new IllegalArgumentException("未找到对应的用户");
        }
        // 进行数据的发送
        UdpSendBox udpSendBox = new UdpSendBox(MethodTypeEnum.SEND_FILE_BOX, inetSocketAddress, buf);
        MyUDPServe.sendMessage(udpSendBox.builderPacket());
    }

    public static ByteBuf getFileByte(Long index, Long length, Integer fileCode, String filePath) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
        FileChannel channel = randomAccessFile.getChannel();
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, index, length);
        ByteBuf byteBuf = Unpooled.copiedBuffer(
                Unpooled.copyLong(index),
                Unpooled.copyLong(length),
                Unpooled.copyInt(fileCode),
                Unpooled.wrappedBuffer(map)
        );
        channel.close();
        return byteBuf;
    }

    /**
     * 进行数据的写入
     * @param fileName 文件名称
     * @param path 路径前缀
     * @param byteBuf 没有进行过处理的文件包 8位 index long | 8位 length long | 4位 fileCode int|字节码内容|校验码|方法编码
     * @return key 返回当前数据的key
     *
     */
    public static String writeFile(String fileName, String path, ByteBuf byteBuf) throws IOException {
        Long index = byteBuf.getLong(0);
        Long length = byteBuf.getLong(8);
        RandomAccessFile randomAccessFile = new RandomAccessFile(path+fileName, "rw");
        FileChannel channel = randomAccessFile.getChannel();
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, index, length);
        ByteBuffer boxBuffer = byteBuf.nioBuffer(20, byteBuf.readableBytes() - 22);
        map.put(boxBuffer);
        randomAccessFile.close();
        channel.close();
        return index.toString() + "|" + length.toString();
    }

    /**
     * 进行MappedByteBuffer 关闭
     * @param fileName
     * @param path
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void closeMapper(String fileName, String path) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(path+fileName, "rw");
        FileChannel channel = randomAccessFile.getChannel();
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, 0);
        Method m = FileChannelImpl.class.getDeclaredMethod("unmap", MappedByteBuffer.class);
        m.setAccessible(true);
        m.invoke(FileChannelImpl.class, map);
        randomAccessFile.close();
        channel.close();
    }
}
