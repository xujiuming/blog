package com.ming.base.web;

import com.google.common.collect.Lists;
import com.ming.common.entity.log.LogAccess;
import com.ming.common.service.log.LogAccessService;
import com.ming.core.utils.JacksonSingleton;
import com.ming.core.utils.SpringBeanManager;
import com.ming.core.utils.WebHttpUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Enumeration;
import java.util.List;

/**
 * 访问日志拦截器
 *
 * @author ming
 * @date 2018-09-07 15:01:17
 */
public class AccessLogInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LogAccess logAccess = new LogAccess();
        logAccess.setCreateInstant(Instant.now());
        logAccess.setCookies(JacksonSingleton.writeAsString(request.getCookies()));
        logAccess.setUri(request.getRequestURI());
        logAccess.setHost(request.getRemoteHost());
        logAccess.setParams(JacksonSingleton.writeAsString(request.getParameterMap()));
        List<String> headers = Lists.newArrayList();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            headers.add(headerNames.nextElement());
        }
        logAccess.setHeaders(JacksonSingleton.writeAsString(headers));
        logAccess.setMethod(request.getMethod());
        logAccess.setUserAgent(WebHttpUtils.getUa(request));
        SpringBeanManager.getBean(LogAccessService.class).save(logAccess);
        return true;
    }
}
