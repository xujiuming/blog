package com.ming.core.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求工具类 从request中 获取 各种信息的 工具类
 *
 * @author ming
 * @date 2018-09-07 17:28:39
 */
public class WebHttpUtils extends org.springframework.web.util.WebUtils {
    private static final String USER_AGENT = "UserInfo-Agent";


    /**
     * 根据 request对象获取ua信息
     *
     * @author ming
     * @date 2018-09-10 09:34:03
     */
    public static String getUa(HttpServletRequest request) {
        String ua = request.getHeader(USER_AGENT);
        if (StringUtils.isEmpty(ua)) {
            throw new RuntimeException("无法获取ua");
        }
        return ua;
    }


}
