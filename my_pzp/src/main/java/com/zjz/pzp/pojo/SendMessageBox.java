package com.zjz.pzp.pojo;

import lombok.Data;

/**
 * @author zjz
 * @date 2022/9/16
 * 用于对发送消息进行数据的处理
 */
@Data
public class SendMessageBox {
    /**
     * 接收人
     */
    private String recipientName;
    /**
     * 发送人
     */
    private String senderName;
    /**
     * 具体的数据消息
     */
    private String message;
    public SendMessageBox(String box) {
        String senderName = box.substring(box.indexOf("(")+1,box.indexOf(")"));
        String recipientName = box.substring(box.indexOf("[") + 1, box.indexOf("]"));
        String message = box.substring(box.indexOf(":")+1);
        this.recipientName = recipientName;
        this.senderName = senderName;
        this.message = message;
    }

}
