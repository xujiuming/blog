package com.ming.base.global;

import com.ming.base.exception.ServiceException;
import com.ming.core.ResponseResultBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局 rest 控制器 异常处理
 *
 * @author ming
 * @date 2018-08-28 09:25:29
 */
@RestControllerAdvice
@Slf4j
public class GlobalRestControllerExceptionAdvice {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<ResponseResultBody> serviceExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        ResponseResultBody responseResultBody;
        HttpStatus httpStatus;
        if (e instanceof ServiceException) {
            ServiceException serviceException = (ServiceException) e;
            ServiceException.Code code = serviceException.getCode();
            responseResultBody = new ResponseResultBody(code.getCode(), code.getMsg(), serviceException.getExtraInfo());
            httpStatus = serviceException.getCode().getHttpStatus();
        } else {
            responseResultBody = new ResponseResultBody(-1, "未知异常", e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            log.error("未知异常:{}", e.getMessage());
        }
        return new ResponseEntity<>(responseResultBody, httpStatus);
    }

}
