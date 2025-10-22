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
//    public static String getLocalIP() {
//        try {
//            NetworkInterface en0 = NetworkInterface.getByName("en0");
//            if (en0 != null && en0.isUp() && !en0.isLoopback() && !en0.isVirtual()) {
//                Enumeration<InetAddress> addresses = en0.getInetAddresses();
//                while (addresses.hasMoreElements()) {
//                    InetAddress addr = addresses.nextElement();
//                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
//                        return addr.getHostAddress();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Utils#getEn0IP fail", e);
//        }
//        throw new RuntimeException("无法获取 en0 网卡的 IP 地址");
//    }

    public static String getLocalIP() {
        try {
            Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
            while (ifs.hasMoreElements()) {
                NetworkInterface ni = ifs.nextElement();
                // 过滤掉不可用/回环/虚拟/容器/代理网卡
                String name = ni.getName().toLowerCase();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                if (name.startsWith("lo") || name.startsWith("veth") || name.startsWith("docker")
                        || name.startsWith("br-") || name.startsWith("vmnet") || name.startsWith("wg")
                        || name.contains("hyper-v") || name.contains("vbox") || name.contains("nat")) continue;

                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address
                            && !addr.isLoopbackAddress()
                            && !addr.isLinkLocalAddress()
                            && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();    // 例如 192.168.x.x / 10.x.x.x
                    }
                }
            }
        } catch (Exception e) {
            // 记录但不吞
        }
        throw new RuntimeException("无法自动探测本机可用 IPv4，请改用配置项 file.public-host");
    }





    // 校验 Email 的正则表达式方法
    public static boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
