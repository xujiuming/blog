package com.ming.base.exception;


import lombok.Getter;

/**
 * 错误码表
 *
 * @author ming
 * @date 2017-07-16
 */
@Getter
public enum ExceptionContians {

    NUM_PARAM_ERROR(1, "参数异常");

    private Integer num;
    private String content;


    ExceptionContians(Integer num, String content) {
        this.num = num;
        this.content = content;
    }
}
