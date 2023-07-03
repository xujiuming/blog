---
title: spring boot 自定义mvc MessageConverter 笔记
categories: 笔记
tags:
  - spring
abbrlink: de4fca49
date: 2017-11-11 00:00:00
---
#### 指定前后端传输数据格式
在前后分离项目中 最麻烦的应该就是 前后端工程师对接
后端通过添加mvc消息转换器 返回更加符合前端的数据格式   
这个数据格式 不是说返回的格式 而是 某些类型的数据的处理 例如 date 可以转换成Long类型的时间戳返回 
直接继承实现即可 如果是继承WebMvcConfigurerAdapter 实现的mvc 相关配置  可以不用管 这样 它自己会添加到转换器链中 
如果是继承WebMvcConfigurationSupport  那么 需要重写configureMessageConverters() 手动添加到转换器链中
```
/**
 * 返回数数据格式转换器
 *
 * @author ming
 * @date 2017-11-10 13:57
 */
public class MessageConverter extends AbstractHttpMessageConverter<Object> {

    /**
     * date 转换为 时间戳
     *
     * @author ming
     * @date 2017-11-10 13:56
     */
    ValueFilter filter = (obj, s, v) -> {
        if (v instanceof Date) {
            return ((Date) v).getTime();
        }
        return v;
    };

    public MessageConverter() {
        super(MediaType.ALL);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object readInternal(Class<?> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return this.supports(clazz) && this.canWrite(mediaType);
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage httpOutputMessage) throws IOException {
        FileCopyUtils.copy(JSON.toJSONString(o, filter, SerializerFeature.DisableCircularReferenceDetect).getBytes(), httpOutputMessage.getBody());
    }


}
```
#### 这个只是返回数据中数据的类型的格式化 如果是要对返回数据格式进行格式化需要利用对ResponseBodyAdvice进行继承 进行控制器增强处理返回数据格式
