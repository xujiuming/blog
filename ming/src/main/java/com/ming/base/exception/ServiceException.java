package com.ming.base.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

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
     *
     * @author ming
     * @date 2018-08-29 09:49:44
     */
    enum Code {
        /**
         * 系统异常
         */
        SYSTEM_ERROR(-1, "系统异常", HttpStatus.INTERNAL_SERVER_ERROR);

        private Integer code;
        private String msg;
        private HttpStatus httpStatus;

        Code(Integer code, String msg, HttpStatus httpStatus) {
            this.code = code;
            this.msg = msg;
            this.httpStatus = httpStatus;
        }
    }

}
