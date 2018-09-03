package com.ming.base.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局 rest 控制器 异常处理
 *
 * @author ming
 * @date 2018-08-28 09:25:29
 */
@RestControllerAdvice
@Slf4j
public class GlobalRestControllerExceptionAdvice {
/*
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<com.ming.core.ResponseBody> serviceExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e){
        com.ming.core.ResponseBody responseBody = new com.ming.core.ResponseBody();
        if (e instanceof ServiceException){

        }else {


        }

        return  new ResponseEntity<com.ming.core.ResponseBody>(responseBody);
    }*/

}
