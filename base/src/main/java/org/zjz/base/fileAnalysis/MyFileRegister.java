package org.zjz.base.fileAnalysis;



import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zjz
 * @date 2022/8/17
 */
@Slf4j
public class MyFileRegister {
    public static Map<String, FileChannel> fileRegister = new ConcurrentHashMap<>();

    public static FileChannel createFileChannel(String path,String fileName) throws FileNotFoundException {
        File file = new File(path, fileName);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        fileRegister.put(fileName, fileChannel);
        return fileChannel;
    }

    public static boolean writeFileHasNext(String fileName, byte[] writeByte) throws IOException {
        ByteBuffer packLenByf = ByteBuffer.wrap(writeByte, 102, 4);
        int packLen = packLenByf.getInt();
        if (packLen > 0) {
            log.info("进行文件名叫做{}，长度为{}的文件生成", fileName, packLen);
            MappedByteBuffer mappedByteBuffer = fileRegister.get(fileName).map(FileChannel.MapMode.READ_WRITE, 0, packLen);
            mappedByteBuffer.put(writeByte, 106, packLen);
            return true;
        }else {
            fileRegister.get(fileName).close();
            log.info("进行文件名叫做{}，长度为{}不进行生成处理", fileName, packLen);
            return false;
        }
    }
}
