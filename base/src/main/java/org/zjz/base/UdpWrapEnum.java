package org.zjz.base;

/**
 * @author zjz
 * @date 2022/8/16
 */
public enum UdpWrapEnum {
    // 发送ip和端口-注册到服务器
    SEND_IP_PORT((byte) 0x06),
    // 发送聊天
    SEND_MESSAGE((byte) 0x04),
    // 发送心跳
    SEND_HEARD((byte)0x03),
    // 发送注册表消息
    SEND_LOGIN((byte)0x02),
    // 发送文件-提示进行文件发送
    SEND_FILE((byte)0x01),
    // 接收文件
    RECEPTION_FILE((byte) 0x11),
    // 发送文件-包
    SEND_FILE_PACK((byte) 0x12);
    private byte functionCode;
    UdpWrapEnum(byte functionCode) {
        this.functionCode = functionCode;
    }
    public byte getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(byte functionCode) {
        this.functionCode = functionCode;
    }

    public static void main(String[] args) {

    }
}
