package com.ming.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 自定义服务异常
 *
 * @author ming
 * @date 2017-06-24
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceException extends RuntimeException {

    /**
     * 异常编码
     */
    private Code code;

    private String extraInfo;

    public ServiceException(Code code) {
        this.code = code;
    }

    public ServiceException(Code code, String extraInfo) {
        this.code = code;
        this.extraInfo = extraInfo;
    }


    /**
     * 自定义异常 编码
     * 总得来说  非预期异常返回-1
     * 其他的异常 按照寓意 尽量和http状态 4xx  5xx 绑定
     * 4xx:资源不存在 资源寻找不到
     * 5xx； 资源损坏
     * 其他整数:常规业务提示错误
     *
     * @author ming
     * @date 2018-08-29 09:49:44
     */
    @Getter
    @AllArgsConstructor
    public enum Code {
        /**
         * 系统异常
         */
        SYSTEM_ERROR(-1, "系统异常", HttpStatus.INTERNAL_SERVER_ERROR),
        /**
         * 数据为null
         */
        DATA_NOT_FOUND(40000, "数据为空", HttpStatus.NOT_FOUND);

        private Integer code;
        private String msg;
        private HttpStatus httpStatus;
    }

}


