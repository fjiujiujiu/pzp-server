package com.zjz.pzp.central;

import org.apache.commons.lang3.StringUtils;
import org.zjz.base.IPAddrUtils;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zjz
 * @date 2022/9/16
 */
public class UserNameIpCenter {
    public static Map<String, InetSocketAddress> USERNAME_IP = new ConcurrentHashMap<>();

    /**
     * 进行数据的注册
     * @param ip ss:127.0.0.1:9998;sccs:127.0.0.1:9998;scccccs:127.0.0.1:9998;scccccaas:127.0.0.1:9998
     */
    public static void register(String ip) {
        USERNAME_IP.clear();
        String[] ipElement = StringUtils.split(ip, ";");
        for (int i = 0; i < ipElement.length; i++) {
            String[] useripport = StringUtils.split(ipElement[i], ":");
            if (useripport.length == 3) {
                InetSocketAddress address = IPAddrUtils.getInetSocketAddressByString(useripport[1], useripport[2]);
                USERNAME_IP.put(useripport[0], address);
            }
        }
    }

    /**
     * 获取对应的用户地址
     * @param userName
     * @return
     */
    public static InetSocketAddress getInetByUserName(String userName) {
        return USERNAME_IP.get(userName);
    }
}
