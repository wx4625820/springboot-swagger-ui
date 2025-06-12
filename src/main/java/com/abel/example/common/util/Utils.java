package com.abel.example.common.util;

import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Utils {
    /**
     * @return 获取当前机器在局域网的ip地址
     * @throws Exception
     */
    public static String getLocalIP() {
        try {
            NetworkInterface en0 = NetworkInterface.getByName("en0");
            if (en0 != null && en0.isUp() && !en0.isLoopback() && !en0.isVirtual()) {
                Enumeration<InetAddress> addresses = en0.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Utils#getEn0IP fail", e);
        }
        throw new RuntimeException("无法获取 en0 网卡的 IP 地址");
    }




    // 校验 Email 的正则表达式方法
    public static boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
