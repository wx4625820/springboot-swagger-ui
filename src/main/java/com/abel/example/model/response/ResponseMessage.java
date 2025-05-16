package com.abel.example.model.response;

import com.abel.example.common.enums.ResultEnum;
import lombok.Data;

/**
 * 统一API响应封装
 *
 * @param <T> 数据类型
 */
@Data
public class ResponseMessage<T> {
    private int code;    // 状态码
    private String msg;  // 提示信息
    private T data;      // 响应数据

    // 私有构造器
    private ResponseMessage(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ----------- 成功响应 -----------
    public static <T> ResponseMessage<T> success() {
        return new ResponseMessage<>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMsg(), null);
    }

    public static <T> ResponseMessage<T> success(T data) {
        return new ResponseMessage<>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMsg(), data);
    }

    public static <T> ResponseMessage<T> success(String msg, T data) {
        return new ResponseMessage<>(ResultEnum.SUCCESS.getCode(), msg, data);
    }

    // ----------- 错误响应 -----------
    public static <T> ResponseMessage<T> error(int code, String msg) {
        return new ResponseMessage<>(code, msg, null);
    }

    public static <T> ResponseMessage<T> error(String msg) {
        return new ResponseMessage<>(ResultEnum.SYSTEM_ERROR.getCode(), msg, null);
    }

    // ----------- 链式调用支持 -----------
    public ResponseMessage<T> code(int code) {
        this.code = code;
        return this;
    }

    public ResponseMessage<T> msg(String msg) {
        this.msg = msg;
        return this;
    }
}