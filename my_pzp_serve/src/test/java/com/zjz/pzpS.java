package com.zjz;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.zjz.base.IPAddrUtils;


import java.net.*;
import java.nio.ByteBuffer;

/**
 * @author zjz
 * @date 2022/9/11
 */
public class pzpS {
    @Test
    public void test1() {
        ByteBuf m = Unpooled.copiedBuffer(new byte[]{0x68, 0x01, 0x32});

        System.out.println( ByteBufUtil.hexDump(m).toUpperCase());

    }


}
