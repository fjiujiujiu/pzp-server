package com.zjz.pzp.pojo;

/**
 * @author zjz
 * @date 2022/9/11
 */
public enum MethodTypeEnum {
    // 心跳-原封返回
    HEAD_UDP((byte) 0x05),
    // 注册用户数据到服务器
    REGISTRY_USER((byte) 0x02),
    // 中介服务器返回用户名和ip 信息格式：name:ip:port;name:ip:port
    USER_IPS((byte) 0x03),
    // 进行数据发送
    SEND_MESSAGE((byte) 0x06),
    // 文件发送的单号
    SEND_FILE_TICKET((byte) 0x07),
    // 通知消息传递失败
    SEND_ERROR_MESSAGE((byte) 0x08),
    // 通知消息
    SEND_INFO_MESSAGE((byte) 0x18),
    /**
     * 发送文件索引信息
     * 8位 index long | 8位 length long | 4位 fileCode int|
     */
    SEND_FILE_INDEX((byte)0x09),
    /**
     * 一个一个的字节包
     */
    SEND_FILE_BOX((byte) 0x10),
    ;

    private byte code;

     MethodTypeEnum(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }
}
