package com.ming.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;


/**
 * jackson 单例对象  双检锁模式
 *
 * @author ming
 * @date 2018-08-23 09:56:58
 */
@Slf4j
public class JacksonSingleton {
    private static volatile ObjectMapper objectMapper;

    private JacksonSingleton() {
    }


    public static ObjectMapper getInstance() {
        if (null == objectMapper) {
            synchronized (ObjectMapper.class) {
                if (null == objectMapper) {
                    objectMapper = new ObjectMapper();
                    //关闭 未知属性的校验
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    //通过该方法对mapper对象进行设置，所有序列化的对象都将按改规则进行系列化
                    //Include.Include.ALWAYS 默认
                    //Include.NON_DEFAULT 属性为默认值不序列化
                    //Include.NON_EMPTY 属性为 空（“”） 或者为 NULL 都不序列化
                    //Include.NON_NULL 属性为NULL 不序列化
                    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                }
            }
        }
        return objectMapper;
    }


    public static <T> String writeAsString(@NonNull T t) {
        try {
            return getInstance().writeValueAsString(t);
        } catch (JsonProcessingException e) {
            log.error("无法将{}对象转换为json字符串", t);
        }
        throw new RuntimeException("对象转化字符串异常");
    }
}
