package com.zjz.pzp.central;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringStack;
import com.zjz.pzp.pojo.FileTicketBean;
import com.zjz.pzp.pojo.MethodTypeEnum;
import com.zjz.pzp.pojo.UdpSendBox;
import com.zjz.pzp.webServe.MyUDPServe;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zjz
 * @date 2022/9/21
 * 构建了一个filecode 为主键，同时值为HashTable的以
 * index|len
 */
public class FileSendMapCenter {
    public static volatile Map<Integer, Map<String, String>> FILE_INDEX_CENTER = new ConcurrentHashMap<>();

    public static Map<String, String> builderFileIndexCenter(FileTicketBean bean, long boxSize) {
        long firstBox = bean.getFileLength() % boxSize;
        long len = bean.getFileLength() / boxSize;
        long index =(bean.getFileLength()-firstBox);
        Map<String, String> base = new HashMap<>();
        if (firstBox > 0) {
            base.put(index + "|" + firstBox, "");
        }
        for (; len > 0; len--) {
            index = index - boxSize;
            base.put(index + "|" + boxSize, "");
        }

        FILE_INDEX_CENTER.put(bean.getFileHashCode(), base);
        return base;
    }

    /**
     * 删除 接收到的key
     * @param fileCode
     * @param key
     */
    public static synchronized void removeKey(Integer fileCode, String key) {
        Map<String, String> keys = FILE_INDEX_CENTER.get(fileCode);
        if (Objects.nonNull(keys)) {
            keys.remove(key);
        }
    }

    public static synchronized ByteBuf nextSendByte(Integer fileCode) {
        Map<String, String> keys = FILE_INDEX_CENTER.get(fileCode);
        if (Objects.nonNull(keys)) {
            if (keys.size() > 0) {
                String[] indexLengthArray = keys.keySet().iterator().next().split("\\|");
                return Unpooled.copiedBuffer(
                        Unpooled.copyLong(Long.valueOf(indexLengthArray[0])),
                        Unpooled.copyLong(Long.valueOf(indexLengthArray[1])),
                        Unpooled.copyInt(fileCode)
                );
            }
            else {
                return null;
            }
        }
        return null;
    }
}
