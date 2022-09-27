package com.zjz.pzp.pojo;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * @author zjz
 * @date 2022/9/19
 * 发送数据到ws服务器进行解析
 */
@Data
public class SendWsBox {
    /**
     * 由于在ws中使用相同的一个数据通道
     * 为此需要依据不同的返回方法进行相应
     * 的数据定义
     */
    private int resCode;
    private String errorMsg;
    private Object body;

    public static SendWsBox builderErrorObj(String errorMsg) {
        SendWsBox sendWsBox = new SendWsBox();
        sendWsBox.setResCode(SendWsEnum.ERROR_METHOD.getMethodType());
        sendWsBox.setErrorMsg(errorMsg);
        return sendWsBox;
    }

    public static SendWsBox builderInfoObj(String errorMsg) {
        SendWsBox sendWsBox = new SendWsBox();
        sendWsBox.setResCode(SendWsEnum.INFO_METHOD.getMethodType());
        sendWsBox.setBody(errorMsg);
        return sendWsBox;
    }
    public static String builderErrorObjToJson(String errorMsg) {
        SendWsBox sendWsBox = SendWsBox.builderErrorObj(errorMsg);
        return JSON.toJSONString(sendWsBox);
    }
    public static SendWsBox builderSuccessOb(Object body,SendWsEnum wsEnum) {
        SendWsBox sendWsBox = new SendWsBox();
        sendWsBox.setResCode(wsEnum.getMethodType());
        sendWsBox.setBody(body);
        return sendWsBox;
    }
}
