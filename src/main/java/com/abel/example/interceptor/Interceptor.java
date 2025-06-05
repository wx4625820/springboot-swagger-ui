package com.abel.example.interceptor;

import com.abel.example.common.enums.ResultEnum;
import com.abel.example.model.entity.User;
import com.abel.example.model.response.ResponseMessage;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * 自定义拦截器，实现简单的登录拦截
 *
 * @auther wangxu
 * @date 2025/05/16
 */
@Component
@Aspect
@Slf4j
public class Interceptor {

    @Pointcut("within (com.abel.example.controller..*) && !within(com.abel.example.controller.LoginController)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object trackInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        User user = (User) request.getSession().getAttribute("user");

        if (user == null) {
            log.warn("未登录请求: {}", request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json;charset=UTF-8");

            ResponseMessage<String> result = ResponseMessage.error(401, "未登录，请先登录");
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            return null;
        }

        return joinPoint.proceed();
    }
}
