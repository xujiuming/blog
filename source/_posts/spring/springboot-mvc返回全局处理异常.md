---
title: spring boot 全局处理异常笔记
categories: 笔记
tags:
  - spring
abbrlink: 869ed45a
date: 2017-11-11 00:00:00
---

####全局异常处理 
在web中 异常也分为系统异常和业务异常 可以通过增强控制器 来对异常进行全局处理 
1:创建业务异常类
```

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

```
2:创建业务异常枚举类 
```

import lombok.Getter;

/**
 * 错误码表
 *
 * @author ming
 * @date 2017-07-16
 */
@Getter
public enum ExceptionConstants {
    ERROR(1,"业务异常");

    private Integer num;
    private String content;


    ExceptionConstants(Integer num, String content) {
        this.num = num;
        this.content = content;
    }
}
```
3:如何抛出异常
这里粗糙的直接抛出枚举值  
如果有需要 可以建立业务异常服务 统一管理错误码和错误信息  动态的变更业务异常提示
也可以继承 细分异常  例如 有的是参数错误 、有的是请求未授权之类的 
```
        throw new ServiceException(ExceptionConstants.ERROR.getNum(),ExceptionConstants.ERROR.getContent());
```
4:建立增强controller 全局异常捕捉
这里通过@ControllerAdvice增强 
通过@ExceptionHandler 捕捉异常
```

import com.ming.base.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 *
 * @author ming
 * @date 2017-11-10 13:58
 */
@ControllerAdvice
@Slf4j
public class BaseGlobalExceptionHandler {


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ReturnJsonBody defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        ReturnJsonBody rb = new ReturnJsonBody();
        if (e instanceof ServiceException) {
            ServiceException se = (ServiceException) e;
            rb.setCode(se.getCode());
            rb.setMsg(se.getMsg());
        } else {
            rb.setCode(1);
            rb.setMsg("系统异常");
            e.printStackTrace();
            rb.setStack(ExceptionUtils.getStackTrace(e));
            /*try {
                addServerExceptionLog(req, e);
            } catch (Exception ex) {
                LOGGER.error("add server exception error {}", ExceptionUtils.getStackTrace(ex));
            }
*/
        }
        return rb;
    }

    // public abstract void addServerExceptionLog(HttpServletRequest request, Throwable throwable);

}
```
#### 总结 
通过增强controller 来捕捉全局mvc异常  这样第一可以返回指定格式错误信息  第二 可以捕捉业务异常 
