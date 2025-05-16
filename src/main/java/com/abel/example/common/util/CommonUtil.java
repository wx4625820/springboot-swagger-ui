package com.abel.example.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @auther wangxu
 * @date 2025/05/16
 */
public class CommonUtil {

    public static Map<String, Object> errorJson(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        result.put("message", message);
        return result;
    }
}