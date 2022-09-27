package org.zjz.base.fileAnalysis;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zjz
 * @date 2022/8/17
 */
public class MyFileManager {
    public static Map<String, SendFileBox> FILE_LOGISTICS = new ConcurrentHashMap<>();

    public static SendFileBox registryFile(File file) throws IOException {
        SendFileBox bod = new SendFileBox(file, 900);
        FILE_LOGISTICS.put(file.getName(), bod);
        return bod;
    }

    public static ByteBuf sendFilePack(String fileName) throws IOException {
        return FILE_LOGISTICS.get(fileName).sendPackFile();
    }
    public static SendFileBox getFileBox(String fileName) {
        return FILE_LOGISTICS.get(fileName);
    }
}
