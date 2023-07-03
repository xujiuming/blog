---
title: spring boot 全局处理返回数据格式笔记
categories: 笔记
tags:
  - spring
abbrlink: ba648708
date: 2017-11-11 00:00:00
---
####全局处理json数据返回格式
在前后端分离中 前后端为了更好的交互 应该指定一个较为通用的数据返回格式 
基本上都是code+data 方式返回 
在spring mvc 中通过对controller全局增强 来进行处理 

1:制定 返回json数据返回大致格式 
```
/**
 * 返回json 字符串格式
 * json接口 返回的字符串格式
 *
 * @author ming
 * @date 2017-11-10 12:56
 */
@Data
@NoArgsConstructor
public class ReturnJsonBody implements Serializable {
    /**
     * 状态码
     */
    private long code;
    /**
     * 数据
     */
    private Object data;
    /**
     * 附加信息
     */
    private String msg;
    /**
     * 异常信息
     */
    private String stack;

    /**
     * 返回成功的消息使用的构造函数
     *
     * @author ming
     * @date 2017-11-10 17:26
     */
    public ReturnJsonBody(long code, Object data) {
        this.code = code;
        this.data = data;
    }

    /**
     * 返回异常的消息使用的构造函数
     *
     * @author ming
     * @date 2017-11-10 17:26
     */
    public ReturnJsonBody(long code, String msg, String stack) {
        this.code = code;
        this.msg = msg;
        this.stack = stack;
    }
}
```
2:通过增强控制器来实现对返回数据包装 
```
/**
 * 全局json 数据返回处理
 *
 * @author ming
 * @date 2017-11-10 14:02
 */
@ControllerAdvice
public class BaseGlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    /**
     * 需要处理的类型
     *
     * @author ming
     * @date 2017-11-10 14:05
     */
    private final Set<MediaType> jsonMediaType = new ImmutableSet.Builder<MediaType>()
            .add(MediaType.APPLICATION_JSON)
            .add(MediaType.APPLICATION_JSON_UTF8)
            .build();

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object obj, MethodParameter methodParameter, MediaType mediaType,
            Class<? extends HttpMessageConverter<?>> converterType,
            ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        //当类型 不属于 需要处理的包头的时候 直接返回obj
        if (!jsonMediaType.contains(mediaType)) {
            return obj;
        }
        //当类型 是属于需要处理的时候 并且 obj不是ReturnJsonBody的时候 进行格式化处理
        if (obj == null || !(obj instanceof ReturnJsonBody)) {
            obj = new ReturnJsonBody(0L, obj);
        }
        return obj;
    }

}
```
####总结: 使用增强控制器返回格式方式 来对json接口进行返回数据格式统一处理 也是必须的 方便和前端交互 
