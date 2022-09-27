package org.zjz.base;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zjz
 * @date 2022/8/16
 * 用于数据包的解析加工
 */
public class FixedLengthAnalysis {
    /**
     * 计算校验码
     *
     * @param bArray cmd
     * @param len    头长  +  数据域长度
     * @return 校验码
     */
    public static byte getCheckByte(byte[] bArray, int len) {
        // 采用累加和取反的校验方式，
        // 发送方将装置号码、控制字、数据域长度和数据区的所有字节进行算术累加
        // 抛弃高位，只保留最后单字节，将单字节取反
        byte result = 0;
        for (int i = 2; i < len; i++) {
            result += bArray[i];
        }
        return (byte) ~(result & 0xff);
    }

    public static byte getCheckByteT(byte[] bArray) {
        // 采用累加和取反的校验方式，
        // 发送方将装置号码、控制字、数据域长度和数据区的所有字节进行算术累加
        // 抛弃高位，只保留最后单字节，将单字节取反
        byte result = 0;
        for (int i = 0; i < bArray.length; i++) {
            result += bArray[i];
        }
        return (byte) ~(result & 0xff);
    }

    public static byte getCheckByteT(ByteBuf byteBuf,int start,int length) {
        // 采用累加和取反的校验方式，
        // 发送方将装置号码、控制字、数据域长度和数据区的所有字节进行算术累加
        // 抛弃高位，只保留最后单字节，将单字节取反
        byte result = 0;
        for (int i = start; i < length; i++) {
            result += byteBuf.getByte(i);
        }
        return (byte) ~(result & 0xff);
    }
    public static void main(String[] args) throws IOException {

       /* ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        byteBuffer.put("ssss.txt".getBytes(CharsetUtil.UTF_8));
        byteBuffer.flip();
        byte[] bytes = byteBuffer.array();
        System.out.println(new String(bytes));*/
        byte[] bytes = HexUtils.hexStringToByte("6812737373732E7478740000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000433A5C55736572735C6A69756A69755C4465736B746F705C737373732E74787400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000E216");
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.compositeHeapBuffer();
        byteBuf.writeBytes(bytes, 2, 100);
        int hasLen = byteBuf.readableBytes();
        System.out.println(hasLen);
        byte[] lenRun = new byte[hasLen];

        byteBuf.readBytes(lenRun);
        byteBuf.release();
        System.out.println(byteBuf.readableBytes());
        int notZert = HexUtils.getMaxNotZero(lenRun);
        System.out.println(notZert);
        System.out.println(lenRun.length);
        System.out.println(new String(lenRun, 0, notZert) + "|");
       // ByteBuf byteBuf = Unpooled.copiedBuffer(bytes, 2, 100);
        System.out.println(byteBuf.refCnt());
       // System.out.println(new String(byteBuf.array(), cn.hutool.core.util.CharsetUtil.UTF_8).replaceAll(" ", "")+"|");
        System.out.println(bytes+"|");
    }

    /**
     * 字符串 转 bytes
     * @param message
     * @return
     */
    public static byte[] messageToBytesUTF8(String message){
        return message.getBytes(CharsetUtil.UTF_8);
    }

    /**
     * bytes 转字符串
     * @param bytes
     * @param offset
     * @param len
     * @return
     */
    public static String byteUF8ToMessage(byte[] bytes, int offset, int len) {

        return new String(bytes, offset, len, CharsetUtil.UTF_8);
    }

    /**
     * bytes 转字符串
     * @param bytes
     * @param offset
     * @param
     * @return
     */
    public static String byteUF8ToMessageNotZero(byte[] bytes, int offset) {
        int len = HexUtils.getMaxNotZero(bytes, offset);
        return new String(bytes, offset, len, CharsetUtil.UTF_8);
    }
}
