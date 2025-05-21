package com.abel.example.common.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@Slf4j
public class Utils {
    /**
     * @return 获取当前机器在局域网的ip地址
     * @throws Exception
     */
    public static String getLocalIP() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            // 跳过回环接口和虚拟接口（如Docker、VPN）
            if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) {
                continue;
            }
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                // 只返回IPv4地址（局域网通常是192.168.x.x或10.x.x.x）
                if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                    return addr.getHostAddress();
                }
            }
        }
        log.error("Utils#getLocalIP fail");
        throw new RuntimeException("无法获取局域网IP");
    }
}
