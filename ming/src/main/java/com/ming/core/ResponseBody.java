package com.ming.core;

import lombok.Data;

@Data
public class ResponseBody {
    /**
     * 错误编码
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
}
