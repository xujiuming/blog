package com.ming.base.interceptor;

import com.ming.base.interceptor.thymeleaf.ThymeleafLayoutInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置
 *
 * @author ming
 * @date 2017-08-28 11点
 */
@Configuration
public class WebInterceptors implements WebMvcConfigurer {


    /**
     * 添加拦截器
     *
     * @author ming
     * @date 2017-11-07 10:08
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        excludePathPatterns(registry.addInterceptor(new AccessLogInterceptor()));
        excludePathPatterns(registry.addInterceptor(new ThymeleafLayoutInterceptor()));
    }

    /**
     * 统一为所有拦截器 设置排除 url等配置
     *
     * @author ming
     * @date 2018-08-28 09:59:36
     */
    private void excludePathPatterns(InterceptorRegistration interceptorRegistration) {
        interceptorRegistration.excludePathPatterns("/static/**");
    }


    /**
     * 添加资源文件地址
     *
     * @author ming
     * @date 2018-08-28 09:59:58
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }
}
