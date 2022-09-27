package org.zjz.base.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.CharsetUtil;
import org.junit.Test;
import org.zjz.base.FixedLengthAnalysis;
import org.zjz.base.HexUtils;
import org.zjz.base.UdpWrapEnum;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zjz
 * @date 2022/8/18
 */
public class TestMain {
    @Test
    public void test1() {
        byte[] axd = HexUtils.hexStringToByte("737373732E747874000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000020433A5C55736572735C6A69756A69755C4465736B746F705C737373732E747874");
        System.out.println(axd.length);
        System.out.println(HexUtils.formatToHexStringWithASCII(new byte[]{FixedLengthAnalysis.getCheckByteT(axd)}));

    }

    @Test
    public void test2() {
        byte[] axx = HexUtils.hexStringToByte("6812737373732E747874000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000020433A5C55736572735C6A69756A69755C4465736B746F705C737373732E747874AE16");
        System.out.println(HexUtils.formatToHexStringWithASCII(new byte[]{FixedLengthAnalysis.getCheckByte(axx, axx.length-2)}));
        byte[] axd = HexUtils.hexStringToByte("737373732E747874000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000020433A5C55736572735C6A69756A69755C4465736B746F705C737373732E747874");
        System.out.println(HexUtils.formatToHexStringWithASCII(new byte[]{FixedLengthAnalysis.getCheckByteT(axd)}));

    }
    @Test
    public void test3() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("C:\\Users\\jiujiu\\Desktop\\ssss.txt", "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        byte[] bytes = new byte[new Long(randomAccessFile.length()).intValue()];
        MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length);
        map.get(bytes);
        ByteBuf box = new UnpooledByteBufAllocator(true).buffer();
        box.writeBytes(new byte[]{0x68, UdpWrapEnum.SEND_FILE_PACK.getFunctionCode()});
        ByteBuffer fileNamePacke = ByteBuffer.allocate(100);
        ByteBuffer packLength = ByteBuffer.allocate(4);
        byte[] pack = bytes;
        fileNamePacke.put("ssss.txt".getBytes(CharsetUtil.UTF_8));
        box.writeBytes(fileNamePacke.array());
        box.writeBytes(packLength.putInt(pack.length).array());
        box.writeBytes(pack);
        byte check = FixedLengthAnalysis.getCheckByteT(box, 2,box.readableBytes());
        System.out.println(HexUtils.formatToHexStringWithASCII(new byte[]{check}));
        box.writeByte(check);
        box.writeByte(0x16);
        byte[] allBytes = new byte[box.readableBytes()];
        box.readBytes(allBytes);
        System.out.println(HexUtils.formatToHexStringWithASCII(allBytes));
    }
}
