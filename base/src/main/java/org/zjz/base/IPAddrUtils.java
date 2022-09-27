package org.zjz.base;

import org.apache.commons.lang3.Validate;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * @author zjz
 * @date 2022/8/16
 */
public class IPAddrUtils {
    /**
     * 从{{@link InetSocketAddress}} 中获取到ip:port
     * @param inetAddress
     * @return
     */
    public static String getIpAddrByInetAddress(InetSocketAddress inetAddress) {
        Validate.notNull(inetAddress);
        return String.format("%s:%s", inetAddress.getHostString(), inetAddress.getPort());

    }

    /**
     * 把ip|port 转为{{@link InetSocketAddress}}
     * @param ipPort
     * @return
     */
    public static InetSocketAddress getInetSocketAddressByString(String ipPort) {
        Validate.notBlank(ipPort);
        String[] arr = ipPort.split(":");
        return new InetSocketAddress(arr[0], Integer.valueOf(arr[1]));
    }

    /**
     * 把ip|port 转为{{@link InetSocketAddress}}
     * @return
     */
    public static InetSocketAddress getInetSocketAddressByString(String ip,String port) {
        Validate.notBlank(ip);
        Validate.notBlank(port);
        return new InetSocketAddress(ip, Integer.valueOf(port));
    }

    /**
     * 构建IP表格格式为 username:ip:port;username:ip:port
     * @param userNameAndIp
     * @return
     */
    public static String loginMessageBuilder(HashMap<String, InetSocketAddress> userNameAndIp) {
        StringBuilder builder = new StringBuilder();
        userNameAndIp.forEach((k,v)->{
            builder.append(k + ":");
            builder.append(getIpAddrByInetAddress(v) + ";");
        });
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }
}
