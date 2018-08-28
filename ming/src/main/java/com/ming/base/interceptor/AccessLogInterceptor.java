package com.ming.base.interceptor;

import com.ming.core.utils.JacksonSingleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 记录登录日志
 *
 * @author ming
 * @date 2017-11-06 14:13
 */
@Slf4j
public class AccessLogInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("param:{} cookies:{} ", JacksonSingleton.getInstance().writeValueAsString(request.getParameterMap()), JacksonSingleton.getInstance().writeValueAsString(request.getCookies()));
        return super.preHandle(request, response, handler);
    }
}
