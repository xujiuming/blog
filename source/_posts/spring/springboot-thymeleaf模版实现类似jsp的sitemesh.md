---
title: springboot-thymeleaf模版实现类似jsp的sitemesh
categories: 笔记
tags:
  - spring
abbrlink: 12183fd8
date: 2017-11-11 00:00:00
---
####thymeleaf
虽然现在大部分项目 更加倾向于前后端分离 但是有时候身为后端工程师 总想手贱写点小东西 
但是jsp 又比较老 而且需要web容器支撑 spring boot 支持性太差  
所以还是用spring boot 推荐的thymeleaf 
可是 jsp中的siteMesh装饰器真的好用 苦于thymeleaf没有这个东西 
在国外博客看到别人实现了类似功能 特此写这篇笔记 方便后续查阅
1:建立layout注解
用这个注解标识 那些接口需要被装饰
```

import java.lang.annotation.*;

/**
 * 样式装饰器 注解  在controller中注解
 *
 * @author ming
 * @date 2017-08-28 11点
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Layout {
    /**
     * no layout will be used
     */
    String none = "none";
    /**
     * default layout will be used
     */
    String defaultLayOut = "default";

    String value() default defaultLayOut;
}

```
2:建立 thymeleaf 处理拦截器
这个拦截器 就是用来根据layout注解处理相关组装页面的
```

/**
 * thymeleaf样式拦截器  实现类似 jsp的sitemesh 装饰器功能
 *
 * @author ming
 * @date 2017-08-28 11点
 */
public class ThymeleafLayoutInterceptor extends HandlerInterceptorAdapter {

    private static final String DEFAULT_LAYOUT = "layouts/default";
    private static final String DEFAULT_VIEW_ATTRIBUTE_NAME = "view";

    private String defaultLayout = DEFAULT_LAYOUT;
    private String viewAttributeName = DEFAULT_VIEW_ATTRIBUTE_NAME;

    public void setDefaultLayout(String defaultLayout) {
        Assert.hasLength(defaultLayout, "默认样式不存在!");
        this.defaultLayout = defaultLayout;
    }

    public void setViewAttributeName(String viewAttributeName) {
        Assert.hasLength(defaultLayout, "默认样式不存在!");
        this.viewAttributeName = viewAttributeName;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView == null || !modelAndView.hasView()) {
            return;
        }

        String originalViewName = modelAndView.getViewName();
        if (isRedirectOrForward(originalViewName)) {
            return;
        }
        String layoutName = getLayoutName(handler);
        if (Layout.none.equals(layoutName)) {
            return;
        }
        modelAndView.setViewName(layoutName);
        modelAndView.addObject(this.viewAttributeName, originalViewName);
    }

    private boolean isRedirectOrForward(String viewName) {
        return viewName.startsWith("redirect:") || viewName.startsWith("forward:");
    }

    private String getLayoutName(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Layout layout = getMethodOrTypeAnnotation(handlerMethod);
            if (layout != null) {
                return layout.value();
            }
        }
        return this.defaultLayout;
    }

    private Layout getMethodOrTypeAnnotation(HandlerMethod handlerMethod) {
        Layout layout = handlerMethod.getMethodAnnotation(Layout.class);
        if (layout == null) {
            return handlerMethod.getBeanType().getAnnotation(Layout.class);
        }
        return layout;
    }
}

```
3:建立样式装饰页面 
在项目静态页面路径下 创建 layouts文件夹
在文件夹目录下建立 装饰配置html 例如default.html
```
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <meta charset="utf-8"/>
    <title>ming thymeleaf site mesh</title>

</head>
<body>
    <div id="menu-container">
        <div class="container" th:replace="${view} :: content">加载中。。。。。</div>
    </div>
</body>
</html>

```
4:在controller中应用 
layout默认是使用default的 这个看layout实现即可
这样 index.html就会嵌入到default.html中 那个th:replace=“{view}”那个位置 
```
import com.ming.base.annotations.Layout;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页 控制器
 *
 * @author ming
 * @date 2017-11-08 10:33
 */
@Controller
@Layout
public class IndexController {

    @GetMapping(value = {"", "/", "/index"})
    public String index() {
        return "index";
    }

}
```
####总结：thymeleaf 总的来说 还行 毕竟简单粗暴 但是有几个比较坑的地方  1:必须是标在的xml结构的html 也就是必须符合xml规范 2：由于技术用的人可能不太多 编辑器支持不是很好 不能做到 jsp 那样可以直接追踪到controller中
