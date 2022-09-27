package org.zjz.base.fileAnalysis;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author zjz
 * @date 2022/8/17
 */
public class MyFileUtils {
    /**
     * 根据Path获取 {{@link FileStore}}
     * @param path
     * @return
     * @throws IOException
     */
    public static File getFileByPath(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            return file;
        }else {
            throw new IOException("未找到文件");
        }
    }
    public static void main(String[] args) throws IOException {
        BigDecimal totalB = new BigDecimal(597754562632L);
        BigDecimal boxSizeB = new BigDecimal(900);
        //664171736.257778
        System.out.println(totalB.divide(boxSizeB,0,BigDecimal.ROUND_UP).longValue());
    }
}
