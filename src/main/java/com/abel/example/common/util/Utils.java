package com.abel.example.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
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
            List<String> candidateIps = Lists.newArrayList();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        if (ip.startsWith("192.168.")) {
                            return ip; // 优先返回 192.168.x.x
                        }
                        candidateIps.add(ip); // 后备 IP（如 10.x.x.x）
                    }
                }
            }

            if (!candidateIps.isEmpty()) {
                return candidateIps.get(0); // 返回第一个非192.168地址
            }
        } catch (Exception e) {
            log.error("Utils#getLocalIP fail", e);
        }
        throw new RuntimeException("无法获取局域网IP");
    }




    // 校验 Email 的正则表达式方法
    public static boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
