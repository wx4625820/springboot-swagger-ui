package com.abel.example.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultEnum {

    // 基础状态码（HTTP标准扩展）
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数校验失败"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    // 业务错误码（5开头）
    VIDEO_UPLOAD_FAILED(5001, "视频上传失败"),
    FILE_TYPE_INVALID(5002, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(5003, "文件大小超过限制"),
    QINIU_SERVICE_ERROR(5004, "七牛云服务异常"),

    // 系统级错误
    SYSTEM_ERROR(500, "系统繁忙，请稍后再试"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用");

    private final int code;
    private final String msg;

    /**
     * 根据code获取枚举
     */
    public static ResultEnum getByCode(int code) {
        for (ResultEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return SYSTEM_ERROR;
    }
}