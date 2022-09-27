package com.zjz.pzp.pojo;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Data;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @author zjz
 * @date 2022/9/10
 */
@Data

public class CallServerBox implements Serializable {
    private String head;
    private String returnObject;

    public CallServerBox(String head, String returnObject) {
        this.head = head + "charset=UTF-8";
        this.returnObject = returnObject;
    }

}
