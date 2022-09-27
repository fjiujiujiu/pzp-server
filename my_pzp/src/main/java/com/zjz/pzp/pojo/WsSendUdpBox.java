package com.zjz.pzp.pojo;

import lombok.Data;

/**
 * @author zjz
 * @date 2022/9/13
 * 用来处理从websocket数据中传递数据至udp中
 */
@Data
public class WsSendUdpBox {
    /**
     * 收件人名称
     */
    private String recipientName;
    /**
     * 发件人名称
     */
    private String senderName;
    /**
     * 具体的一个操作类型
     */

    private int methodType;
    /**
     * 数据信息
     */
    private Object body;
    /**
     * 数据格式类型
     */
    private String bodyType;
    /**
     * 中介服务器 ip
     */
    private String serverUdpProxy;
}
