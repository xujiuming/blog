package com.ming.base.global;

import com.google.common.collect.ImmutableSet;
import com.ming.core.ResponseResultBody;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Set;

/**
 * 全局 rest 控制器增强
 *
 * @author ming
 * @date 2018-08-28 09:25:04
 */
@RestControllerAdvice
public class GlobalRestControllerAdvice implements ResponseBodyAdvice<Object> {
    /**
     * 需要处理的类型
     *
     * @author ming
     * @date 2017-11-10 14:05
     */
    private final Set<MediaType> jsonMediaType = new ImmutableSet.Builder<MediaType>()
            .add(MediaType.APPLICATION_JSON)
            .add(MediaType.APPLICATION_JSON_UTF8)
            .build();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        //当类型 不属于 需要处理的包头的时候 直接返回obj
        if (!jsonMediaType.contains(selectedContentType)) {
            return body;
        }
        //当类型 是属于需要处理的时候 并且 obj不是ReturnJsonBody的时候 进行格式化处理
        if (!(body instanceof ResponseResultBody)) {
            body = new ResponseResultBody<>(body);
        }
        //当obj =null的时候 兼容前端  返回{}
        return body;
    }
}
