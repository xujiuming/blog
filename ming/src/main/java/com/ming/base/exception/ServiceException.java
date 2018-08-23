package com.ming.base.exception;

import lombok.Data;

import java.util.Map;

/**
 * 自定义服务异常
 *
 * @author ming
 * @date 2017-06-24
 */
@Data
public class ServiceException extends RuntimeException {

    /**
     * 异常编码
     */
    private int code;
    /**
     * 消息
     */
    private String msg;
    /**
     * 扩展参数
     */
    private Map<String, Object> extParams;


}
