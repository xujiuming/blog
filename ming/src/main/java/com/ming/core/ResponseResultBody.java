package com.ming.core;


import lombok.Data;

/**
 * @author ming
 * @date 2018-09-04 14:50:12
 */
@Data
public class ResponseResultBody<T> {
    /**
     * 编码
     * {@linkplain com.ming.base.exception.ServiceException.Code#code}
     */
    private Integer code;
    /**
     * 错误编码提示信息
     * <p>
     * {@linkplain com.ming.base.exception.ServiceException.Code#msg}
     */
    private String msg;
    /**
     * 补充信息  测试开启 生产禁用此字段
     * {@linkplain com.ming.base.exception.ServiceException#extraInfo}
     */
    private String extraInfo;

    /**
     * 返回对象
     */
    private T body;

    /**
     * 异常的时候使用的构造器
     *
     * @author ming
     * @date 2018-09-04 14:51:48
     */
    public ResponseResultBody(Integer code, String msg, String extraInfo) {
        this.code = code;
        this.msg = msg;
        this.extraInfo = extraInfo;
    }

    /**
     * 异常的时候使用的构造器
     *
     * @author ming
     * @date 2018-09-04 14:51:48
     */
    public ResponseResultBody(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public ResponseResultBody(T body) {
        this.code = 0;
        this.body = body;
    }


}
