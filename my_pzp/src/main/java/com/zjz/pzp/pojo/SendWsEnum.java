package com.zjz.pzp.pojo;

import lombok.Data;

/**
 * @author zjz
 * @date 2022/9/19
 * 定义发送数据至客户端的对应方法编码
 */

public enum SendWsEnum {
    // 错误消息
    ERROR_METHOD(0),
    // 连接成功
    CONNECT_SUCCESS(9),
    // 发送可以使用的用户名
    SEND_USER_LIST(4),
    // 接收用户发送的数据
    RECEIVE_MESSAGE(8),
    // 通知用户有文件来
    SEND_FILE_TICKER(7),
    // 通知消息
    INFO_METHOD(11);
    private int methodType;

    SendWsEnum(int methodType) {
        this.methodType = methodType;
    }

    public int getMethodType() {
        return methodType;
    }

    public void setMethodType(int methodType) {
        this.methodType = methodType;
    }
}
