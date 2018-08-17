package com.ming.base.interceptor;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 统一设定返回包头
 *
 * @author ming
 * @date 2017-11-06 18:15
 */
public class ResponseHandler extends HandlerInterceptorAdapter {

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //统一设定 返回编码集
        response.setCharacterEncoding("UTF-8");
    }
}
