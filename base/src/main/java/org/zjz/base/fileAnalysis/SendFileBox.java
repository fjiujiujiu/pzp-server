package org.zjz.base.fileAnalysis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.CharsetUtil;
import lombok.Data;
import org.zjz.base.FixedLengthAnalysis;
import org.zjz.base.HexUtils;
import org.zjz.base.UdpWrapEnum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author zjz
 * @date 2022/8/17
 */
@Data
public class SendFileBox {
    /**
     * 总的长度
     */
    private long total;
    /**
     * 文件位置
     */
    private File file;
    /**
     * 最大的包数
     */
    private int max;
    /**
     * 当前的下标
     */
    private int buoy=0;
    /**
     * 每一包的大小
     */
    private int boxSize;

    private FileChannel fileChannel;
    private RandomAccessFile randomAccessFile;
    private ByteBuffer byteBuffer;
    public SendFileBox(File file, int boxSize) throws IOException {
        this.file = file;
        this.boxSize = boxSize;
        this.total = file.length();
        BigDecimal totalB = new BigDecimal(this.total);
        BigDecimal boxSizeB = new BigDecimal(boxSize);
        randomAccessFile = new RandomAccessFile(file, "rw");
        fileChannel = randomAccessFile.getChannel();
        this.max = totalB.divide(boxSizeB,0,BigDecimal.ROUND_UP).intValue();
    }

    public byte[] getNextBox() throws IOException {
        if (buoy < max) {
            byte[] bytes;
            if (max - buoy == 1) {
                bytes = new byte[new Long(this.total).intValue()];
                MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_WRITE, buoy * boxSize, bytes.length);
                map.get(bytes);

            }else {
                bytes = new byte[boxSize];
                MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_WRITE, buoy * boxSize, boxSize);
                map.get(bytes);
            }
            buoy++;
            return bytes;
        }else {
            fileChannel.close();
            randomAccessFile.close();
            MyFileManager.FILE_LOGISTICS.remove(file.getName());
            return new byte[]{};
        }
    }

    public ByteBuf sendPackFile() throws IOException {
        ByteBuf box = new UnpooledByteBufAllocator(true).buffer();
        box.writeBytes(new byte[]{0x68, UdpWrapEnum.SEND_FILE_PACK.getFunctionCode()});
        ByteBuffer fileNamePacke = ByteBuffer.allocate(100);
        ByteBuffer packLength = ByteBuffer.allocate(4);
        byte[] pack = getNextBox();
        fileNamePacke.put(file.getName().getBytes(CharsetUtil.UTF_8));
        box.writeBytes(fileNamePacke.array());
         box.writeBytes(packLength.putInt(pack.length).array());
        box.writeBytes(pack);
        byte check = FixedLengthAnalysis.getCheckByteT(box, 2, box.readableBytes());
        box.writeByte(check);
        box.writeByte(0x16);
        return box;
    }
}
